package com.zuomagai.bubble.repository;

import com.zuomagai.bubble.model.BubbleAlloc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
class BubbleAllocRepositoryTest {

    @Autowired
    private BubbleAllocRepository repository;

    @Test
    void preventsUpdateWhenVersionIsStale() {
        BubbleAlloc alloc = repository.findByBizTag("order");
        BubbleAlloc updated = repository.updateMaxId("order", alloc.getMaxId(), alloc.getStep(), alloc.getVersion());
        assertNotNull(updated);

        BubbleAlloc stale = repository.updateMaxId("order", alloc.getMaxId(), alloc.getStep(), alloc.getVersion());
        assertNull(stale);
    }
}
