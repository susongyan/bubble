package com.zuomagai.bubble.service;

import com.zuomagai.bubble.model.BubbleAlloc;
import com.zuomagai.bubble.repository.BubbleAllocRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class SegmentIdServiceIntegrationTest {

    @Autowired
    private SegmentIdService segmentIdService;

    @Autowired
    private BubbleAllocRepository repository;

    @Test
    void generatesSequentialIdsAndUpdatesDatabase() {
        long first = segmentIdService.nextId("order");
        long second = segmentIdService.nextId("order");
        long third = segmentIdService.nextId("order");

        assertEquals(1L, first);
        assertEquals(2L, second);
        assertEquals(3L, third);

        BubbleAlloc alloc = repository.findByBizTag("order");
        assertEquals(1000L, alloc.getMaxId());
        assertEquals(1000, alloc.getStep());
        assertEquals(1, alloc.getVersion());
    }

    @Test
    void throwsWhenBizTagIsMissing() {
        assertThrows(IllegalArgumentException.class, () -> segmentIdService.nextId("missing"));
    }

    @Test
    void includesSegmentUpperBoundWhenStepIsThousand() {
        long lastId = 0;
        for (int i = 0; i < 1000; i++) {
            lastId = segmentIdService.nextId("order");
        }

        assertEquals(1000L, lastId);
    }
}
