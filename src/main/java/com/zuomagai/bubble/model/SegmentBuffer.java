package com.zuomagai.bubble.model;

import java.util.concurrent.atomic.AtomicBoolean;

public class SegmentBuffer {
    private final Segment[] segments = new Segment[]{new Segment(), new Segment()};
    private volatile int currentPos = 0;
    private volatile boolean nextReady = false;
    private final AtomicBoolean init = new AtomicBoolean(false);
    private final AtomicBoolean threadRunning = new AtomicBoolean(false);
    private volatile long lastUpdateTimestamp = 0L;

    public Segment getCurrent() {
        return segments[currentPos];
    }

    public Segment getNext() {
        return segments[(currentPos + 1) % 2];
    }

    public void switchPos() {
        currentPos = (currentPos + 1) % 2;
    }

    public boolean isNextReady() {
        return nextReady;
    }

    public void setNextReady(boolean nextReady) {
        this.nextReady = nextReady;
    }

    public AtomicBoolean getInit() {
        return init;
    }

    public AtomicBoolean getThreadRunning() {
        return threadRunning;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }
}
