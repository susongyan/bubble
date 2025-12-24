package com.zuomagai.bubble.model;

public class BubbleAlloc {
    private String bizTag;
    private long maxId;
    private int step;
    private int version;

    public BubbleAlloc() {
    }

    public BubbleAlloc(String bizTag, long maxId, int step, int version) {
        this.bizTag = bizTag;
        this.maxId = maxId;
        this.step = step;
        this.version = version;
    }

    public String getBizTag() {
        return bizTag;
    }

    public void setBizTag(String bizTag) {
        this.bizTag = bizTag;
    }

    public long getMaxId() {
        return maxId;
    }

    public void setMaxId(long maxId) {
        this.maxId = maxId;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
