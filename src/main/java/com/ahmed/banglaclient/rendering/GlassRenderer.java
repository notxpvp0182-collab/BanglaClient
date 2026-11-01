package com.ahmed.banglaclient.rendering;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;

/**
 * Core rendering engine for the BANGLA CLIENT Liquid Glass aesthetic.
 * Renders beveled rounded glass panels, top-lit specular rim highlights, diagonal surface sheen,
 * and interactive accent badges matching the supplied visual reference dashboard.
 */
public final class GlassRenderer {

    private GlassRenderer() {
    }

    public static Theme getTheme() {
        BanglaClient client = BanglaClient.getInstance();
        if (client != null && client.getThemeManager() != null) {
            return client.getThemeManager().getActiveTheme();
        }
        return new Theme(
                "fallback", "Fallback",
                ThemeTokens.DEFAULT_BG_PRIMARY,
                ThemeTokens.DEFAULT_BG_SECONDARY,
                ThemeTokens.DEFAULT_GLASS_SURFACE,
                ThemeTokens.DEFAULT_GLASS_ELEVATED,
                ThemeTokens.DEFAULT_SPECULAR_TOP,
                ThemeTokens.DEFAULT_SPECULAR_BOTTOM,
                ThemeTokens.DEFAULT_ACCENT_PRIMARY,
                ThemeTokens.DEFAULT_ACCENT_SECONDARY,
                ThemeTokens.DEFAULT_TEXT_PRIMARY,
                ThemeTokens.DEFAULT_TEXT_SECONDARY,
                ThemeTokens.DEFAULT_TEXT_MUTED,
                ThemeTokens.DEFAULT_BORDER_SUBTLE,
                ThemeTokens.DEFAULT_BORDER_STRONG
        );
    }

    /**
     * Draws a primary Liquid Glass panel with chamfered/rounded corners, diagonal specular sheen,
     * and top-lit directional rim borders.
     */
    public static void drawGlassPanel(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            boolean elevated,
            boolean hovered,
            boolean activeAccentBorder
    ) {
        Theme theme = getTheme();
        int baseColor = elevated ? theme.getGlassElevated() : theme.getGlassSurface();
        drawCustomGlassPanel(context, x, y, width, height, baseColor, hovered, activeAccentBorder, true);
    }

    /**
     * Draws a customizable Liquid Glass panel (used by both Click GUI cards and customizable HUD widgets).
     */
    public static void drawCustomGlassPanel(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int surfaceArgb,
            boolean hovered,
            boolean activeAccentBorder,
            boolean drawBorder
    ) {
        if (context == null || width < 4 || height < 4) {
            return;
        }
        Theme theme = getTheme();

        // 1. Optional Ambient Shadow
        ShadowRenderer.drawAmbientShadow(context, x, y, width, height, 4, 0.45f);

        // 2. Main Beveled Rounded Fill (1px corner inset for crisp anti-aliased rounded appearance)
        int topFill = hovered ? ColorUtils.scaleBrightness(surfaceArgb, 1.22f) : ColorUtils.scaleBrightness(surfaceArgb, 1.10f);
        int bottomFill = surfaceArgb;

        // Center body
        context.fillGradient(x + 1, y + 1, x + width - 1, y + height - 1, topFill, bottomFill);
        // Left & right vertical edges (inset by 2px vertically for rounded corners)
        context.fillGradient(x, y + 2, x + 1, y + height - 2, topFill, bottomFill);
        context.fillGradient(x + width - 1, y + 2, x + width, y + height - 2, topFill, bottomFill);
        // Top & bottom horizontal caps (inset by 2px horizontally)
        context.fill(x + 2, y, x + width - 2, y + 1, topFill);
        context.fill(x + 2, y + height - 1, x + width - 2, y + height, bottomFill);

        // 3. Top-Center Specular Liquid Reflection (replicates the KPI card sheen from the reference image)
        int sheenHeight = Math.min(height / 2, 26);
        if (sheenHeight > 3 && width > 12) {
            int specularStart = hovered
                    ? ColorUtils.multiplyAlpha(theme.getSpecularTop(), 1.35f)
                    : theme.getSpecularTop();
            int specularEnd = 0x00FFFFFF;
            context.fillGradient(x + 2, y + 1, x + width - 2, y + 1 + sheenHeight, specularStart, specularEnd);
        }

        // 4. Directional Rim Border (Brighter along the top edge, subtle along sides and bottom)
        if (drawBorder) {
            int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                    ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                    : theme.getAccentPrimary();

            int topBorderColor;
            int sideBorderColor;
            int bottomBorderColor;

            if (activeAccentBorder) {
                topBorderColor = ColorUtils.withAlpha(accent, 0.72f);
                sideBorderColor = ColorUtils.withAlpha(accent, 0.38f);
                bottomBorderColor = ColorUtils.withAlpha(accent, 0.22f);
            } else if (hovered) {
                topBorderColor = theme.getBorderStrong();
                sideBorderColor = theme.getBorderSubtle();
                bottomBorderColor = ColorUtils.multiplyAlpha(theme.getBorderSubtle(), 0.65f);
            } else {
                topBorderColor = ColorUtils.multiplyAlpha(theme.getBorderStrong(), 0.75f);
                sideBorderColor = theme.getBorderSubtle();
                bottomBorderColor = ColorUtils.multiplyAlpha(theme.getBorderSubtle(), 0.45f);
            }

            // Top rim
            context.fill(x + 2, y, x + width - 2, y + 1, topBorderColor);
            // Bottom rim
            context.fill(x + 2, y + height - 1, x + width - 2, y + height, bottomBorderColor);
            // Left & right rims
            context.fillGradient(x, y + 2, x + 1, y + height - 2, topBorderColor, bottomBorderColor);
            context.fillGradient(x + width - 1, y + 2, x + width, y + height - 2, topBorderColor, bottomBorderColor);

            // 4 corner bevel pixels
            context.fill(x + 1, y + 1, x + 2, y + 2, topBorderColor);
            context.fill(x + width - 2, y + 1, x + width - 1, y + 2, topBorderColor);
            context.fill(x + 1, y + height - 2, x + 2, y + height - 1, sideBorderColor);
            context.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, sideBorderColor);
        }
    }

    /**
     * Draws a rounded pill badge (e.g., keybind indicator, ON/OFF status, or category count).
     */
    public static void drawPillBadge(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int tintColor,
            boolean filledAccent
    ) {
        if (context == null || width < 4 || height < 4) {
            return;
        }
        int bgFill = filledAccent
                ? ColorUtils.withAlpha(tintColor, 0.24f)
                : ColorUtils.withAlpha(tintColor, 0.12f);
        int borderFill = filledAccent
                ? ColorUtils.withAlpha(tintColor, 0.55f)
                : ColorUtils.withAlpha(tintColor, 0.25f);

        context.fill(x + 1, y, x + width - 1, y + height, bgFill);
        context.fill(x, y + 1, x + 1, y + height - 1, bgFill);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, bgFill);

        // Top & bottom subtle border
        context.fill(x + 1, y, x + width - 1, y + 1, borderFill);
        context.fill(x + 1, y + height - 1, x + width - 1, y + height, ColorUtils.multiplyAlpha(borderFill, 0.5f));
    }

    /**
     * Draws a crisp 1px horizontal divider with faded ends.
     */
    public static void drawHorizontalDivider(DrawContext context, int x, int y, int width) {
        if (context == null || width <= 4) {
            return;
        }
        Theme theme = getTheme();
        context.fill(x, y, x + width, y + 1, theme.getBorderSubtle());
    }
}
