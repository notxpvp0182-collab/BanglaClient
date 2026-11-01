package com.ahmed.banglaclient.rendering;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;

/**
 * Renders soft multi-step ambient drop shadows around floating Liquid Glass panels.
 * Automatically skips rendering when PerformancePreset is LOW orBALANCED without ambient shadows.
 */
public final class ShadowRenderer {

    private ShadowRenderer() {
    }

    /**
     * Renders a layered ambient elevation shadow around the specified rectangle.
     */
    public static void drawAmbientShadow(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int spread,
            float baseAlpha
    ) {
        if (context == null || width <= 0 || height <= 0 || spread <= 0 || baseAlpha <= 0.01f) {
            return;
        }
        BanglaClient client = BanglaClient.getInstance();
        if (client != null && !client.getPerformancePreset().isAmbientShadowsEnabled()) {
            return;
        }

        int steps = Math.min(spread, 6);
        for (int i = 1; i <= steps; i++) {
            float progress = (float) i / (float) (steps + 1);
            float falloff = (1.0f - progress) * (1.0f - progress);
            int shadowColor = ColorUtils.withAlpha(0x000000, baseAlpha * falloff * 0.42f);

            int sx1 = x - i;
            int sy1 = y - i + 1;
            int sx2 = x + width + i;
            int sy2 = y + height + i + 1;

            // Top & Bottom strips
            context.fill(sx1 + 1, sy1, sx2 - 1, sy1 + 1, shadowColor);
            context.fill(sx1 + 1, sy2 - 1, sx2 - 1, sy2, shadowColor);
            // Left & Right strips
            context.fill(sx1, sy1 + 1, sx1 + 1, sy2 - 1, shadowColor);
            context.fill(sx2 - 1, sy1 + 1, sx2, sy2 - 1, shadowColor);
        }
    }
}
