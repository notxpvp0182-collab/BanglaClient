package com.ahmed.banglaclient.util;

/**
 * High-performance ARGB color manipulation utilities for the Liquid Glass design system.
 * Uses primitive bitwise operations to avoid per-frame object allocations.
 */
public final class ColorUtils {

    private ColorUtils() {
    }

    public static int packArgb(int a, int r, int g, int b) {
        return ((clampByte(a) & 0xFF) << 24)
                | ((clampByte(r) & 0xFF) << 16)
                | ((clampByte(g) & 0xFF) << 8)
                | (clampByte(b) & 0xFF);
    }

    public static int getAlpha(int argb) {
        return (argb >>> 24) & 0xFF;
    }

    public static int getRed(int argb) {
        return (argb >>> 16) & 0xFF;
    }

    public static int getGreen(int argb) {
        return (argb >>> 8) & 0xFF;
    }

    public static int getBlue(int argb) {
        return argb & 0xFF;
    }

    /**
     * Replaces the alpha channel of an ARGB color with a float opacity [0.0f - 1.0f].
     */
    public static int withAlpha(int argb, float alphaNormalized) {
        int a = clampByte(Math.round(alphaNormalized * 255.0f));
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    /**
     * Replaces the alpha channel of an ARGB color with a byte opacity [0 - 255].
     */
    public static int withAlpha(int argb, int alphaByte) {
        return (clampByte(alphaByte) << 24) | (argb & 0x00FFFFFF);
    }

    /**
     * Multiplies the existing alpha channel of an ARGB color by a factor [0.0f - 1.0f].
     */
    public static int multiplyAlpha(int argb, float factor) {
        int currentA = getAlpha(argb);
        int newA = clampByte(Math.round(currentA * MathUtils.clamp(factor, 0.0f, 1.0f)));
        return (newA << 24) | (argb & 0x00FFFFFF);
    }

    /**
     * Linearly interpolates between two ARGB colors.
     */
    public static int lerpColor(int startArgb, int endArgb, float progress) {
        float t = MathUtils.clamp(progress, 0.0f, 1.0f);
        int a = Math.round(MathUtils.lerp(getAlpha(startArgb), getAlpha(endArgb), t));
        int r = Math.round(MathUtils.lerp(getRed(startArgb), getRed(endArgb), t));
        int g = Math.round(MathUtils.lerp(getGreen(startArgb), getGreen(endArgb), t));
        int b = Math.round(MathUtils.lerp(getBlue(startArgb), getBlue(endArgb), t));
        return packArgb(a, r, g, b);
    }

    /**
     * Brightens or darkens an ARGB color by a multiplier (>1.0f brightens, <1.0f darkens).
     */
    public static int scaleBrightness(int argb, float factor) {
        int a = getAlpha(argb);
        int r = clampByte(Math.round(getRed(argb) * factor));
        int g = clampByte(Math.round(getGreen(argb) * factor));
        int b = clampByte(Math.round(getBlue(argb) * factor));
        return packArgb(a, r, g, b);
    }

    private static int clampByte(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
