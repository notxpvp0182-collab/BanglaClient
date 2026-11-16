package dev.ahmed.liquidglass.ui.animation;

public final class Animation {
    public enum Easing {
        LINEAR, EASE_OUT_CUBIC, EASE_IN_CUBIC, EASE_OUT_QUART, EASE_IN_OUT_CUBIC;

        public float apply(float t) {
            t = Math.max(0.0f, Math.min(1.0f, t));
            return switch (this) {
                case LINEAR -> t;
                case EASE_OUT_CUBIC -> 1.0f - (float) Math.pow(1.0f - t, 3);
                case EASE_IN_CUBIC -> t * t * t;
                case EASE_OUT_QUART -> 1.0f - (float) Math.pow(1.0f - t, 4);
                case EASE_IN_OUT_CUBIC -> t < 0.5f
                    ? 4.0f * t * t * t
                    : 1.0f - (float) Math.pow(-2.0f * t + 2.0f, 3) / 2.0f;
            };
        }
    }

    private float startValue;
    private float targetValue;
    private float currentValue;
    private float durationSec;
    private float elapsedSec;
    private final Easing easing;

    public Animation(float initialValue, float durationSec, Easing easing) {
        this.startValue = initialValue;
        this.targetValue = initialValue;
        this.currentValue = initialValue;
        this.durationSec = Math.max(0.001f, durationSec);
        this.elapsedSec = this.durationSec;
        this.easing = easing;
    }

    public void animateTo(float newTarget) {
        if (Math.abs(this.targetValue - newTarget) > 0.0001f) {
            this.startValue = this.currentValue;
            this.targetValue = newTarget;
            this.elapsedSec = 0.0f;
        }
    }

    public float update(float deltaSeconds) {
        if (elapsedSec < durationSec) {
            elapsedSec = Math.min(durationSec, elapsedSec + Math.max(0.0f, deltaSeconds));
            float progress = easing.apply(elapsedSec / durationSec);
            currentValue = startValue + (targetValue - startValue) * progress;
        } else {
            currentValue = targetValue;
        }
        return currentValue;
    }

    public float value() { return currentValue; }
    public float target() { return targetValue; }
    public boolean isFinished() { return elapsedSec >= durationSec; }
}
