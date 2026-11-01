package com.ahmed.banglaclient.core;

/**
 * Defines hardware-adaptive rendering and animation presets for BANGLA CLIENT.
 */
public enum PerformancePreset {
    LOW("Low", false, false, 0.88f, 0.5f),
    BALANCED("Balanced", true, false, 0.82f, 1.0f),
    HIGH("High", true, true, 0.76f, 1.0f);

    private final String displayName;
    private final boolean blurEnabled;
    private final boolean ambientShadowsEnabled;
    private final float baseSurfaceAlpha;
    private final float animationSpeedMultiplier;

    PerformancePreset(
            String displayName,
            boolean blurEnabled,
            boolean ambientShadowsEnabled,
            float baseSurfaceAlpha,
            float animationSpeedMultiplier
    ) {
        this.displayName = displayName;
        this.blurEnabled = blurEnabled;
        this.ambientShadowsEnabled = ambientShadowsEnabled;
        this.baseSurfaceAlpha = baseSurfaceAlpha;
        this.animationSpeedMultiplier = animationSpeedMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isBlurEnabled() {
        return blurEnabled;
    }

    public boolean isAmbientShadowsEnabled() {
        return ambientShadowsEnabled;
    }

    public float getBaseSurfaceAlpha() {
        return baseSurfaceAlpha;
    }

    public float getAnimationSpeedMultiplier() {
        return animationSpeedMultiplier;
    }

    public PerformancePreset next() {
        PerformancePreset[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
