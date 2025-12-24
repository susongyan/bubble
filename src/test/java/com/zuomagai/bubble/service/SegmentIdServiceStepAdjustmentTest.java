package com.zuomagai.bubble.service;

import com.zuomagai.bubble.config.IdGeneratorProperties;
import com.zuomagai.bubble.model.BubbleAlloc;
import com.zuomagai.bubble.model.SegmentBuffer;
import com.zuomagai.bubble.repository.BubbleAllocRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SegmentIdServiceStepAdjustmentTest {

    private SegmentIdService segmentIdService;

    @BeforeEach
    void setUp() {
        BubbleAllocRepository repository = mock(BubbleAllocRepository.class);
        IdGeneratorProperties properties = new IdGeneratorProperties();
        properties.setMinStep(1000);
        properties.setMaxStep(8000);
        properties.setFastConsumeThresholdSeconds(10);
        properties.setSlowConsumeThresholdSeconds(100);
        segmentIdService = new SegmentIdService(repository, properties);
    }

    @Test
    void doublesStepWhenConsumedQuickly() {
        SegmentBuffer buffer = new SegmentBuffer();
        buffer.getInit().set(true);
        buffer.setLastUpdateTimestamp(System.currentTimeMillis() - 5000);
        BubbleAlloc alloc = new BubbleAlloc("order", 0, 1000, 0);

        int step = ReflectionTestUtils.invokeMethod(segmentIdService, "adjustStep", buffer, alloc);
        assertEquals(2000, step);
    }

    @Test
    void halvesStepWhenConsumedSlowly() {
        SegmentBuffer buffer = new SegmentBuffer();
        buffer.getInit().set(true);
        buffer.setLastUpdateTimestamp(System.currentTimeMillis() - 200_000);
        BubbleAlloc alloc = new BubbleAlloc("order", 0, 4000, 0);

        int step = ReflectionTestUtils.invokeMethod(segmentIdService, "adjustStep", buffer, alloc);
        assertEquals(2000, step);
    }
}
