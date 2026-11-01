package com.ahmed.banglaclient.animation;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.util.MathUtils;

/**
 * Delta-time-based, interruptible animation value that respects reduced-motion and low-end presets.
 */
public final class Animation {
    private float startValue;
    private float targetValue;
    private float currentValue;
    private long startTimeNanos;
    private final long durationMillis;
    private final Easing easing;

    public Animation(float initialValue, long durationMillis, Easing easing) {
        this.startValue = initialValue;
        this.targetValue = initialValue;
        this.currentValue = initialValue;
        this.durationMillis = Math.max(1L, durationMillis);
        this.easing = easing != null ? easing : Easing.EASE_OUT_CUBIC;
        this.startTimeNanos = System.nanoTime();
    }

    public Animation(float initialValue, long durationMillis) {
        this(initialValue, durationMillis, Easing.EASE_OUT_CUBIC);
    }

    /**
     * Smoothly redirects the animation toward a new target value without state snapping.
     */
    public void animateTo(float newTarget) {
        if (Math.abs(this.targetValue - newTarget) < 0.0001f) {
            return;
        }
        this.startValue = getValue();
        this.targetValue = newTarget;
        this.startTimeNanos = System.nanoTime();
    }

    /**
     * Immediately snaps the animation to the specified value.
     */
    public void snapTo(float value) {
        this.startValue = value;
        this.targetValue = value;
        this.currentValue = value;
        this.startTimeNanos = System.nanoTime();
    }

    /**
     * Evaluates and returns the current interpolated value based on elapsed wall-clock nanoseconds.
     */
    public float getValue() {
        if (isReducedMotionActive()) {
            this.currentValue = this.targetValue;
            return this.currentValue;
        }
        if (Math.abs(currentValue - targetValue) < 0.0001f) {
            this.currentValue = this.targetValue;
            return this.currentValue;
        }

        float speedMultiplier = 1.0f;
        BanglaClient client = BanglaClient.getInstance();
        if (client != null && client.getPerformancePreset() != null) {
            speedMultiplier = client.getPerformancePreset().getAnimationSpeedMultiplier();
        }

        long effectiveDurationNanos = (long) ((durationMillis * 1_000_000L) * speedMultiplier);
        if (effectiveDurationNanos <= 0L) {
            this.currentValue = this.targetValue;
            return this.currentValue;
        }

        long elapsedNanos = Math.max(0L, System.nanoTime() - startTimeNanos);
        float progress = MathUtils.clamp((float) elapsedNanos / (float) effectiveDurationNanos, 0.0f, 1.0f);
        float eased = easing.apply(progress);
        this.currentValue = startValue + (targetValue - startValue) * eased;
        return this.currentValue;
    }

    public float getTargetValue() {
        return targetValue;
    }

    public boolean isFinished() {
        return Math.abs(getValue() - targetValue) < 0.001f;
    }

    private static boolean isReducedMotionActive() {
        BanglaClient client = BanglaClient.getInstance();
        return client != null
                && client.getConfigManager() != null
                && client.getConfigManager().isReducedMotion();
    }
}
