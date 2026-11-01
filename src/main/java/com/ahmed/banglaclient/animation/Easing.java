package com.ahmed.banglaclient.animation;

import com.ahmed.banglaclient.util.MathUtils;

/**
 * Standard mathematical easing curves for frame-rate-independent UI animations.
 */
public enum Easing {
    LINEAR {
        @Override
        public float apply(float t) {
            return MathUtils.clamp(t, 0.0f, 1.0f);
        }
    },
    EASE_OUT_QUAD {
        @Override
        public float apply(float t) {
            float x = MathUtils.clamp(t, 0.0f, 1.0f);
            return 1.0f - (1.0f - x) * (1.0f - x);
        }
    },
    EASE_OUT_CUBIC {
        @Override
        public float apply(float t) {
            float x = MathUtils.clamp(t, 0.0f, 1.0f);
            float inv = 1.0f - x;
            return 1.0f - (inv * inv * inv);
        }
    },
    EASE_IN_OUT_CUBIC {
        @Override
        public float apply(float t) {
            float x = MathUtils.clamp(t, 0.0f, 1.0f);
            return x < 0.5f
                    ? 4.0f * x * x * x
                    : 1.0f - (float) Math.pow(-2.0f * x + 2.0f, 3) / 2.0f;
        }
    },
    EASE_OUT_QUART {
        @Override
        public float apply(float t) {
            float x = MathUtils.clamp(t, 0.0f, 1.0f);
            float inv = 1.0f - x;
            return 1.0f - (inv * inv * inv * inv);
        }
    },
    EASE_OUT_BACK {
        @Override
        public float apply(float t) {
            float x = MathUtils.clamp(t, 0.0f, 1.0f);
            float c1 = 1.70158f;
            float c3 = c1 + 1.0f;
            float xm1 = x - 1.0f;
            return 1.0f + c3 * (xm1 * xm1 * xm1) + c1 * (xm1 * xm1);
        }
    };

    public abstract float apply(float normalizedTime);
}
