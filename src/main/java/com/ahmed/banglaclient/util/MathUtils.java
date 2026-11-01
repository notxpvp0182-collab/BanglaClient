package com.ahmed.banglaclient.util;

/**
 * Fast mathematical and interpolation helpers for GUI layout, hit testing, and animations.
 */
public final class MathUtils {

    private MathUtils() {
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float lerp(float start, float end, float delta) {
        return start + (end - start) * clamp(delta, 0.0f, 1.0f);
    }

    public static double lerp(double start, double end, double delta) {
        return start + (end - start) * clamp(delta, 0.0, 1.0);
    }

    /**
     * Frame-rate independent exponential smoothing step.
     */
    public static float damp(float current, float target, float speed, float deltaSeconds) {
        return lerp(current, target, 1.0f - (float) Math.exp(-speed * Math.max(0.0f, deltaSeconds)));
    }

    public static boolean isHovered(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= (x + width) && mouseY >= y && mouseY <= (y + height);
    }

    public static double roundToStep(double value, double step) {
        if (step <= 0.0) {
            return value;
        }
        return Math.round(value / step) * step;
    }
}
