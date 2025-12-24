package com.zuomagai.bubble.benchmark;

import com.zuomagai.bubble.config.IdGeneratorProperties;
import com.zuomagai.bubble.model.BubbleAlloc;
import com.zuomagai.bubble.repository.BubbleAllocRepository;
import com.zuomagai.bubble.service.SegmentIdService;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SegmentIdServiceBenchmark {

    @State(Scope.Benchmark)
    public static class ServiceState {
        private SegmentIdService service;

        @Setup(Level.Trial)
        public void setup() {
            BubbleAlloc seed = new BubbleAlloc();
            seed.setBizTag("order");
            seed.setMaxId(0L);
            seed.setStep(1000);
            seed.setVersion(0);

            IdGeneratorProperties properties = new IdGeneratorProperties();
            service = new SegmentIdService(new InMemoryRepository(seed), properties);
        }
    }

    @Benchmark
    public long nextId(ServiceState state) {
        return state.service.nextId("order");
    }

    private static class InMemoryRepository extends BubbleAllocRepository {
        private final AtomicReference<BubbleAlloc> allocRef;

        InMemoryRepository(BubbleAlloc seed) {
            super(null);
            this.allocRef = new AtomicReference<>(seed);
        }

        @Override
        public BubbleAlloc findByBizTag(String bizTag) {
            BubbleAlloc alloc = allocRef.get();
            if (alloc == null || !Objects.equals(alloc.getBizTag(), bizTag)) {
                return null;
            }
            BubbleAlloc copy = new BubbleAlloc();
            copy.setBizTag(alloc.getBizTag());
            copy.setMaxId(alloc.getMaxId());
            copy.setStep(alloc.getStep());
            copy.setVersion(alloc.getVersion());
            return copy;
        }

        @Override
        public BubbleAlloc updateMaxId(String bizTag, long currentMaxId, int step, int expectedVersion) {
            while (true) {
                BubbleAlloc existing = allocRef.get();
                if (existing == null || !Objects.equals(existing.getBizTag(), bizTag)) {
                    return null;
                }
                if (existing.getVersion() != expectedVersion || existing.getMaxId() != currentMaxId) {
                    return null;
                }
                BubbleAlloc updated = new BubbleAlloc();
                updated.setBizTag(bizTag);
                updated.setStep(step);
                updated.setVersion(expectedVersion + 1);
                updated.setMaxId(currentMaxId + step);
                if (allocRef.compareAndSet(existing, updated)) {
                    return updated;
                }
            }
        }
    }
}
