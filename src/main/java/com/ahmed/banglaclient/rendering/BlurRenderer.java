package com.ahmed.banglaclient.rendering;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.core.PerformancePreset;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Hardware-adaptive background blur and smoked-glass backdrop renderer.
 * Uses Minecraft's native blur pass in HIGH/BALANCED modes and falls back to
 * zero-cost translucent smoked charcoal in LOW mode.
 */
public final class BlurRenderer {
    private static long lastScreenBlurFrame = -1L;
    private static long frameCounter = 0L;

    private BlurRenderer() {
    }

    public static void beginFrame() {
        frameCounter++;
    }

    /**
     * Renders full-screen background blur for modal screens (Click GUI, HUD Editor, Settings)
     * at most once per frame, plus a dark obsidian vignette overlay matching the reference image.
     */
    public static void renderScreenBackdrop(DrawContext context, int screenWidth, int screenHeight, float openProgress) {
        if (context == null || screenWidth <= 0 || screenHeight <= 0) {
            return;
        }
        BanglaClient bangla = BanglaClient.getInstance();
        PerformancePreset preset = bangla != null ? bangla.getPerformancePreset() : PerformancePreset.BALANCED;
        Theme theme = bangla != null && bangla.getThemeManager() != null
                ? bangla.getThemeManager().getActiveTheme()
                : null;

        if (preset.isBlurEnabled() && lastScreenBlurFrame != frameCounter) {
            lastScreenBlurFrame = frameCounter;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world != null && mc.gameRenderer != null) {
                try {
                    mc.gameRenderer.renderBlur();
                } catch (Throwable ignored) {
                    // Safe fallback if a third-party shader mod overrides gameRenderer blur
                }
            }
        }

        int baseColor = theme != null ? theme.getBackgroundPrimary() : 0xFF080A0F;
        float targetAlpha = preset == PerformancePreset.LOW ? 0.22f : 0.14f;
        int topTint = ColorUtils.withAlpha(baseColor, targetAlpha * openProgress * 0.90f);
        int bottomTint = ColorUtils.withAlpha(baseColor, targetAlpha * openProgress);

        context.fillGradient(0, 0, screenWidth, screenHeight, topTint, bottomTint);
    }

    /**
     * Renders a localized frosted glass surface tint for individual HUD widgets or cards.
     */
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
        int topColor = ColorUtils.withAlpha(ColorUtils.scaleBrightness(baseGlassColor, 1.18f), effectiveAlpha);
        int bottomColor = ColorUtils.withAlpha(baseGlassColor, Math.min(0.96f, effectiveAlpha * 1.06f));

        context.fillGradient(x, y, x + width, y + height, topColor, bottomColor);
    }
}
