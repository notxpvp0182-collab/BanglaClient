package com.ahmed.banglaclient.rendering;

/**
 * Coordinates Color Saturation world pass with safe fallback on OpenGL ES 3 / LTW mobile GPUs.
 */
public final class SaturationShaderRenderer {
    private SaturationShaderRenderer() {}

    public static boolean renderWorldPass() {
        return false;
    }

    public static boolean isShaderUnavailable() {
        return true;
    }
}
