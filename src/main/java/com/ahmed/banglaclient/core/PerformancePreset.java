package com.ahmed.banglaclient.core;

/**
 * Hardware-adaptive rendering, blur, shadow, and animation quality levels for BANGLA CLIENT.
 * Supports LOW (FPS_BOOST), MEDIUM (BALANCED), HIGH (VISUAL_QUALITY), and ULTRA (CUSTOM).
 */
public enum PerformancePreset {
    FPS_BOOST("LOW (FPS Boost)", "LOW", false, false, 0, 0, 0.92f, 0.65f, true, 0.25f),
    BALANCED("MEDIUM (Balanced)", "MEDIUM", true, true, 1, 2, 0.86f, 1.0f, false, 0.75f),
    VISUAL_QUALITY("HIGH (Aura Quality)", "HIGH", true, true, 2, 4, 0.80f, 1.0f, false, 1.0f),
    CUSTOM("ULTRA (Maximum Glass)", "ULTRA", true, true, 3, 5, 0.76f, 1.0f, false, 1.0f);

    private final String displayName;
    private final String shortQualityTag;
    private final boolean blurEnabled;
    private final boolean ambientShadowsEnabled;
    private final int blurQualityTier;
    private final int shadowSpread;
    private final float baseSurfaceAlpha;
    private final float animationSpeedMultiplier;
    private final boolean optimizeParticles;
    private final float weatherParticleMultiplier;

    PerformancePreset(
            String displayName,
            String shortQualityTag,
            boolean blurEnabled,
            boolean ambientShadowsEnabled,
            int blurQualityTier,
            int shadowSpread,
            float baseSurfaceAlpha,
            float animationSpeedMultiplier,
            boolean optimizeParticles,
            float weatherParticleMultiplier
    ) {
        this.displayName = displayName;
        this.shortQualityTag = shortQualityTag;
        this.blurEnabled = blurEnabled;
        this.ambientShadowsEnabled = ambientShadowsEnabled;
        this.blurQualityTier = blurQualityTier;
        this.shadowSpread = shadowSpread;
        this.baseSurfaceAlpha = baseSurfaceAlpha;
        this.animationSpeedMultiplier = animationSpeedMultiplier;
        this.optimizeParticles = optimizeParticles;
        this.weatherParticleMultiplier = weatherParticleMultiplier;
    }

    public String getDisplayName() { return displayName; }
    public String getShortQualityTag() { return shortQualityTag; }
    public boolean isBlurEnabled() { return blurEnabled; }
    public boolean isAmbientShadowsEnabled() { return ambientShadowsEnabled; }
    public int getBlurQualityTier() { return blurQualityTier; }
    public int getShadowSpread() { return shadowSpread; }
    public float getBaseSurfaceAlpha() { return baseSurfaceAlpha; }
    public float getAnimationSpeedMultiplier() { return animationSpeedMultiplier; }
    public boolean isOptimizeParticles() { return optimizeParticles; }
    public float getWeatherParticleMultiplier() { return weatherParticleMultiplier; }

    public PerformancePreset next() {
        PerformancePreset[] vals = values();
        return vals[(this.ordinal() + 1) % vals.length];
    }
}
