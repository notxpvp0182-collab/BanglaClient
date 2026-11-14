package com.ahmed.banglaclient.rendering;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.core.PerformancePreset;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Hardware-adaptive Aura-style atmospheric frosted backdrop renderer.
 * Performs a single shared blur capture per frame with graceful fallback on low-end or shader failure.
 */
public final class BlurRenderer {
    private static long lastScreenBlurFrame = -1L;
    private static long frameCounter = 0L;
    private static boolean blurFailedOnce = false;

    private BlurRenderer() {
    }

    public static void beginFrame() {
        frameCounter++;
    }

    public static void renderScreenBackdrop(DrawContext context, int screenWidth, int screenHeight, float openProgress) {
        if (context == null || screenWidth <= 0 || screenHeight <= 0) {
            return;
        }
        float clampedProgress = Math.max(0.0f, Math.min(1.0f, openProgress));
        BanglaClient bangla = BanglaClient.getInstance();
        PerformancePreset preset = bangla != null ? bangla.getPerformancePreset() : PerformancePreset.BALANCED;
        Theme theme = bangla != null && bangla.getThemeManager() != null
                ? bangla.getThemeManager().getActiveTheme()
                : null;

        // Shared once-per-frame blur pass (skipped in FPS_BOOST / LOW mode)
        if (preset.isBlurEnabled() && !blurFailedOnce && lastScreenBlurFrame != frameCounter) {
            lastScreenBlurFrame = frameCounter;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.world != null && mc.gameRenderer != null) {
                try {
                    mc.gameRenderer.renderBlur();
                } catch (Throwable t) {
                    blurFailedOnce = true;
                    BanglaClient.LOGGER.warn("Shader blur pass unavailable, falling back to translucent charcoal backdrop.");
                }
            }
        }

        // Layer 1: Deep Aura Charcoal base
        int baseColor = theme != null ? theme.getBackgroundPrimary() : 0xEE08090C;
        float baseAlpha = preset == PerformancePreset.FPS_BOOST ? 0.82f : 0.72f;
        int topTint = ColorUtils.withAlpha(baseColor, baseAlpha * clampedProgress);
        int bottomTint = ColorUtils.withAlpha(baseColor, Math.min(0.92f, (baseAlpha + 0.12f) * clampedProgress));
        context.fillGradient(0, 0, screenWidth, screenHeight, topTint, bottomTint);

        // Layer 2: Subtle Aura atmospheric top mist & vignette (skipped in FPS_BOOST mode)
        if (preset != PerformancePreset.FPS_BOOST && clampedProgress > 0.05f) {
            int auroraMistTop = ColorUtils.withAlpha(0xCBD5E1, 0.045f * clampedProgress);
            context.fillGradient(0, 0, screenWidth, Math.max(40, screenHeight / 3), auroraMistTop, 0x00FFFFFF);
            int bottomVignette = ColorUtils.withAlpha(0x000000, 0.35f * clampedProgress);
            context.fillGradient(0, screenHeight - Math.max(40, screenHeight / 4), screenWidth, screenHeight, 0x00000000, bottomVignette);
        }
    }

    public static void renderFrostedRegion(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int baseGlassColor,
            float opacityMultiplier
    ) {
        if (context == null || width <= 0 || height <= 0) {
            return;
        }
        BanglaClient bangla = BanglaClient.getInstance();
        PerformancePreset preset = bangla != null ? bangla.getPerformancePreset() : PerformancePreset.BALANCED;

        float effectiveAlpha = preset.getBaseSurfaceAlpha() * opacityMultiplier;
        int topColor = ColorUtils.withAlpha(ColorUtils.scaleBrightness(baseGlassColor, 1.12f), effectiveAlpha);
        int bottomColor = ColorUtils.withAlpha(baseGlassColor, Math.min(0.96f, effectiveAlpha * 1.04f));

        context.fillGradient(x, y, x + width, y + height, topColor, bottomColor);
    }
}
