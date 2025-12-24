package com.zuomagai.bubble.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bubble.segment")
public class IdGeneratorProperties {

    /**
     * Minimal step size allowed when adjusting segment size.
     */
    private int minStep = 1000;

    /**
     * Maximum step size allowed when adjusting segment size.
     */
    private int maxStep = 100000;

    /**
     * When the last segment is consumed quicker than this threshold (in seconds), the service will try to increase
     * the step size.
     */
    private int fastConsumeThresholdSeconds = 60;

    /**
     * When the last segment is consumed slower than this threshold (in seconds), the service will try to decrease
     * the step size.
     */
    private int slowConsumeThresholdSeconds = 600;

    /**
     * Usage ratio that triggers asynchronous prefetch of the next segment.
     */
    private double prefetchThreshold = 0.8;

    public int getMinStep() {
        return minStep;
    }

    public void setMinStep(int minStep) {
        this.minStep = minStep;
    }

    public int getMaxStep() {
        return maxStep;
    }

    public void setMaxStep(int maxStep) {
        this.maxStep = maxStep;
    }

    public int getFastConsumeThresholdSeconds() {
        return fastConsumeThresholdSeconds;
    }

    public void setFastConsumeThresholdSeconds(int fastConsumeThresholdSeconds) {
        this.fastConsumeThresholdSeconds = fastConsumeThresholdSeconds;
    }

    public int getSlowConsumeThresholdSeconds() {
        return slowConsumeThresholdSeconds;
    }

    public void setSlowConsumeThresholdSeconds(int slowConsumeThresholdSeconds) {
        this.slowConsumeThresholdSeconds = slowConsumeThresholdSeconds;
    }

    public double getPrefetchThreshold() {
        return prefetchThreshold;
    }

    public void setPrefetchThreshold(double prefetchThreshold) {
        this.prefetchThreshold = prefetchThreshold;
    }
}
