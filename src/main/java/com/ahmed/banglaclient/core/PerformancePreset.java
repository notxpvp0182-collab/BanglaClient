package com.ahmed.banglaclient.core;

/**
 * Defines hardware-adaptive rendering, animation, and visual quality presets for BANGLA CLIENT.
 * Supports: FPS Boost, Balanced, Visual Quality, and Custom.
 */
public enum PerformancePreset {
    FPS_BOOST("FPS Boost", false, false, 0.90f, 0.0f, true, 0.25f),
    BALANCED("Balanced", true, false, 0.82f, 1.0f, false, 0.75f),
    VISUAL_QUALITY("Visual Quality", true, true, 0.72f, 1.0f, false, 1.0f),
    CUSTOM("Custom", true, false, 0.80f, 1.0f, false, 0.50f);

    private final String displayName;
    private final boolean blurEnabled;
    private final boolean ambientShadowsEnabled;
    private final float baseSurfaceAlpha;
    private final float animationSpeedMultiplier;
    private final boolean optimizeParticles;
    private final float weatherParticleMultiplier;

    PerformancePreset(
            String displayName,
            boolean blurEnabled,
            boolean ambientShadowsEnabled,
            float baseSurfaceAlpha,
            float animationSpeedMultiplier,
            boolean optimizeParticles,
            float weatherParticleMultiplier
    ) {
        this.displayName = displayName;
        this.blurEnabled = blurEnabled;
        this.ambientShadowsEnabled = ambientShadowsEnabled;
        this.baseSurfaceAlpha = baseSurfaceAlpha;
        this.animationSpeedMultiplier = animationSpeedMultiplier;
        this.optimizeParticles = optimizeParticles;
        this.weatherParticleMultiplier = weatherParticleMultiplier;
    }

    public String getDisplayName() { return displayName; }
    public boolean isBlurEnabled() { return blurEnabled; }
    public boolean isAmbientShadowsEnabled() { return ambientShadowsEnabled; }
    public float getBaseSurfaceAlpha() { return baseSurfaceAlpha; }
    public float getAnimationSpeedMultiplier() { return animationSpeedMultiplier; }
    public boolean isOptimizeParticles() { return optimizeParticles; }
    public float getWeatherParticleMultiplier() { return weatherParticleMultiplier; }

    public PerformancePreset next() {
        PerformancePreset[] vals = values();
        return vals[(this.ordinal() + 1) % vals.length];
    }
}
