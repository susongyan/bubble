package com.zuomagai.bubble.model;

import java.util.concurrent.atomic.AtomicLong;

public class Segment {
    private final AtomicLong value = new AtomicLong();
    private volatile long max;
    private volatile int step;
    private volatile long valueBase;

    public long getAndIncrement() {
        return value.getAndIncrement();
    }

    public long getValue() {
        return value.get();
    }

    public void setValue(long value) {
        this.value.set(value);
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public long getValueBase() {
        return valueBase;
    }

    public void setValueBase(long valueBase) {
        this.valueBase = valueBase;
    }
}
