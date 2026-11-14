package com.ahmed.banglaclient.rendering;

import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;

/**
 * Ultra-fast, zero-lag 2D Aura Dark screen backdrop renderer.
 */
public final class BlurRenderer {
    private BlurRenderer() {}

    public static void beginFrame() {}

    public static void renderScreenBackdrop(DrawContext context, int screenWidth, int screenHeight, float openProgress) {
        if (context == null || screenWidth <= 0 || screenHeight <= 0) return;
        float p = Math.max(0.0f, Math.min(1.0f, openProgress));
        int fill = ColorUtils.withAlpha(0x060709, 0.62f * p);
        context.fill(0, 0, screenWidth, screenHeight, fill);
    }

    public static void renderFrostedRegion(DrawContext context, int x, int y, int width, int height, int baseGlassColor, float opacityMultiplier) {
        if (context == null || width <= 0 || height <= 0) return;
        int fill = ColorUtils.withAlpha(baseGlassColor, 0.85f * opacityMultiplier);
        context.fill(x, y, x + width, y + height, fill);
    }
}
