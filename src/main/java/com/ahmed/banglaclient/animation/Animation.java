package com.ahmed.banglaclient.animation;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.util.MathUtils;

/**
 * Delta-time-based, interruptible tween animation supporting stagger delays and reduced motion.
 */
public final class Animation {
    private float startValue;
    private float targetValue;
    private float currentValue;
    private long startTimeNanos;
    private long delayNanos;
    private final long durationMillis;
    private final Easing easing;

    public Animation(float initialValue, long durationMillis, Easing easing) {
        this.startValue = initialValue;
        this.targetValue = initialValue;
        this.currentValue = initialValue;
        this.durationMillis = Math.max(1L, durationMillis);
        this.easing = easing != null ? easing : Easing.EASE_OUT_CUBIC;
        this.startTimeNanos = System.nanoTime();
        this.delayNanos = 0L;
    }

    public Animation(float initialValue, long durationMillis) {
        this(initialValue, durationMillis, Easing.EASE_OUT_CUBIC);
    }

    public void animateTo(float newTarget) {
        animateTo(newTarget, 0L);
    }

    public void animateTo(float newTarget, long delayMs) {
        if (Math.abs(this.targetValue - newTarget) < 0.0001f && delayMs <= 0L) {
            return;
        }
        this.startValue = getValue();
        this.targetValue = newTarget;
        this.startTimeNanos = System.nanoTime();
        this.delayNanos = Math.max(0L, delayMs) * 1_000_000L;
    }

    public void resetCascade(float fromValue, float toValue, long delayMs) {
        this.startValue = fromValue;
        this.currentValue = fromValue;
        this.targetValue = toValue;
        this.startTimeNanos = System.nanoTime();
        this.delayNanos = Math.max(0L, delayMs) * 1_000_000L;
    }

    public void snapTo(float value) {
        this.startValue = value;
        this.targetValue = value;
        this.currentValue = value;
        this.startTimeNanos = System.nanoTime();
        this.delayNanos = 0L;
    }

    public float getValue() {
        if (isReducedMotionActive()) {
            this.currentValue = this.targetValue;
            return this.currentValue;
        }
        if (Math.abs(currentValue - targetValue) < 0.0001f && delayNanos == 0L) {
            this.currentValue = this.targetValue;
            return this.currentValue;
        }

        long rawElapsed = System.nanoTime() - startTimeNanos;
        if (rawElapsed < delayNanos) {
            return this.startValue;
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

        long elapsedAfterDelay = Math.max(0L, rawElapsed - delayNanos);
        float progress = MathUtils.clamp((float) elapsedAfterDelay / (float) effectiveDurationNanos, 0.0f, 1.0f);
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
