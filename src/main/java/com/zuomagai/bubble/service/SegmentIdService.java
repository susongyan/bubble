package com.zuomagai.bubble.service;

import com.zuomagai.bubble.config.IdGeneratorProperties;
import com.zuomagai.bubble.model.BubbleAlloc;
import com.zuomagai.bubble.model.Segment;
import com.zuomagai.bubble.model.SegmentBuffer;
import com.zuomagai.bubble.repository.BubbleAllocRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SegmentIdService {

    private static final Logger log = LoggerFactory.getLogger(SegmentIdService.class);

    private final BubbleAllocRepository repository;
    private final IdGeneratorProperties properties;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Map<String, SegmentBuffer> bufferCache = new ConcurrentHashMap<>();

    public SegmentIdService(BubbleAllocRepository repository, IdGeneratorProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @PreDestroy
    public void destroy() {
        shutdown();
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                log.warn("SegmentIdService executor did not terminate in time; forcing shutdown.");
                executor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    public long nextId(String bizTag) {
        Objects.requireNonNull(bizTag, "bizTag cannot be null");
        SegmentBuffer buffer = bufferCache.computeIfAbsent(bizTag, this::initBuffer);
        return getIdFromSegment(bizTag, buffer);
    }

    private SegmentBuffer initBuffer(String bizTag) {
        SegmentBuffer buffer = new SegmentBuffer();
        loadSegment(bizTag, buffer.getCurrent(), buffer);
        buffer.getInit().set(true);
        return buffer;
    }

    private long getIdFromSegment(String bizTag, SegmentBuffer buffer) {
        while (true) {
            Segment current = buffer.getCurrent();
            long value = current.getAndIncrement();
            if (value <= current.getMax()) {
                triggerPrefetchIfNeeded(bizTag, buffer, current, value);
                return value;
            }

            synchronized (buffer) {
                if (value <= current.getMax()) {
                    triggerPrefetchIfNeeded(bizTag, buffer, current, value);
                    return value;
                }
                if (buffer.isNextReady()) {
                    buffer.switchPos();
                    buffer.setNextReady(false);
                    continue;
                }
                loadSegment(bizTag, current, buffer);
                current = buffer.getCurrent();
                if (current.getValue() <= current.getMax()) {
                    return current.getAndIncrement();
                }
            }
        }
    }

    private void triggerPrefetchIfNeeded(String bizTag, SegmentBuffer buffer, Segment current, long value) {
        double used = (double) (value - current.getValueBase()) / current.getStep();
        if (used >= properties.getPrefetchThreshold()
                && !buffer.isNextReady()
                && buffer.getThreadRunning().compareAndSet(false, true)) {
            CompletableFuture.runAsync(() -> {
                try {
                    loadSegment(bizTag, buffer.getNext(), buffer);
                    buffer.setNextReady(true);
                } finally {
                    buffer.getThreadRunning().set(false);
                }
            }, executor);
        }
    }

    private void loadSegment(String bizTag, Segment segment, SegmentBuffer buffer) {
        while (true) {
            BubbleAlloc alloc = repository.findByBizTag(bizTag);
            if (alloc == null) {
                throw new IllegalArgumentException("bizTag not found: " + bizTag);
            }
            int step = adjustStep(buffer, alloc);
            BubbleAlloc updated = repository.updateMaxId(bizTag, alloc.getMaxId(), step, alloc.getVersion());
            if (updated != null) {
                long newMaxId = updated.getMaxId();
                long newStart = newMaxId - updated.getStep() + 1;
                segment.setValueBase(newStart);
                segment.setValue(newStart);
                segment.setMax(newMaxId);
                segment.setStep(updated.getStep());
                buffer.setLastUpdateTimestamp(System.currentTimeMillis());
                return;
            }
        }
    }

    private int adjustStep(SegmentBuffer buffer, BubbleAlloc alloc) {
        int step = alloc.getStep();
        if (buffer.getInit().get()) {
            long durationSeconds = (System.currentTimeMillis() - buffer.getLastUpdateTimestamp()) / 1000;
            if (durationSeconds > 0 && durationSeconds < properties.getFastConsumeThresholdSeconds()) {
                step = Math.min(step * 2, properties.getMaxStep());
            } else if (durationSeconds > properties.getSlowConsumeThresholdSeconds()) {
                step = Math.max(step / 2, properties.getMinStep());
            }
        }
        return Math.max(step, properties.getMinStep());
    }
}
