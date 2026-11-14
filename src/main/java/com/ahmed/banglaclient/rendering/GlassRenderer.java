package com.ahmed.banglaclient.rendering;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;

/**
 * Core rendering engine for the BANGLA CLIENT Aura-styled Frosted Glass & PYES structural cards.
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
                "aura_dark", "Aura Dark",
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

        // 1. Soft Layered Ambient Shadow
        ShadowRenderer.drawAmbientShadow(context, x, y, width, height, 3, 0.24f);

        // 2. Anti-aliased 1px chamfered rounded glass fill
        int topFill = hovered ? ColorUtils.scaleBrightness(surfaceArgb, 1.18f) : ColorUtils.scaleBrightness(surfaceArgb, 1.06f);
        int bottomFill = surfaceArgb;

        context.fillGradient(x + 1, y + 1, x + width - 1, y + height - 1, topFill, bottomFill);
        context.fillGradient(x, y + 1, x + 1, y + height - 1, topFill, bottomFill);
        context.fillGradient(x + width - 1, y + 1, x + width, y + height - 1, topFill, bottomFill);
        context.fill(x + 1, y, x + width - 1, y + 1, topFill);
        context.fill(x + 1, y + height - 1, x + width - 1, y + height, bottomFill);

        // 3. Restrained Aura Top Inner Specular Sheen
        int sheenHeight = Math.min(height / 3, 16);
        if (sheenHeight > 2 && width > 10) {
            int specTop = hovered
                    ? ColorUtils.multiplyAlpha(theme.getSpecularTop(), 1.25f)
                    : theme.getSpecularTop();
            context.fillGradient(x + 1, y + 1, x + width - 1, y + 1 + sheenHeight, specTop, 0x00FFFFFF);
        }

        // 4. Crisp 1px Aura Border
        if (drawBorder) {
            int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                    ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                    : theme.getAccentPrimary();

            int topBorder;
            int bottomBorder;

            if (activeAccentBorder) {
                topBorder = ColorUtils.withAlpha(accent, 0.58f);
                bottomBorder = ColorUtils.withAlpha(accent, 0.24f);
            } else if (hovered) {
                topBorder = theme.getBorderStrong();
                bottomBorder = theme.getBorderSubtle();
            } else {
                topBorder = theme.getBorderSubtle();
                bottomBorder = ColorUtils.multiplyAlpha(theme.getBorderSubtle(), 0.55f);
            }

            context.fill(x + 1, y, x + width - 1, y + 1, topBorder);
            context.fill(x + 1, y + height - 1, x + width - 1, y + height, bottomBorder);
            context.fillGradient(x, y + 1, x + 1, y + height - 1, topBorder, bottomBorder);
            context.fillGradient(x + width - 1, y + 1, x + width, y + height - 1, topBorder, bottomBorder);
        }
    }

    /**
     * Draws a PYES-style subtle top-left corner bracket accent (`┌`) in restrained Aura silver/white.
     */
    public static void drawCornerBracket(DrawContext context, int x, int y, int length, int color) {
        if (context == null || length <= 1) return;
        context.fill(x, y, x + length, y + 1, color);
        context.fill(x, y + 1, x + 1, y + length, color);
    }

    /**
     * Draws crisp monochrome geometric vector icons for categories and module cards (zero blurry bitmaps).
     */
    public static void drawVectorIcon(DrawContext context, int x, int y, int size, String iconType, int color) {
        if (context == null || size < 6) return;
        int cx = x + size / 2;
        int cy = y + size / 2;

        switch (iconType == null ? "" : iconType.toUpperCase()) {
            case "DASHBOARD", "HOME" -> {
                int half = (size - 3) / 2;
                drawOutlineBorder(context, x, y, half, half, color);
                drawOutlineBorder(context, x + half + 2, y, half, half, color);
                drawOutlineBorder(context, x, y + half + 2, half, half, color);
                drawOutlineBorder(context, x + half + 2, y + half + 2, half, half, color);
            }
            case "HUD" -> {
                drawOutlineBorder(context, x, y + 1, size, size - 2, color);
                context.fill(x + 2, y + 3, x + size - 2, y + 4, color);
                context.fill(x + 2, y + 6, x + size / 2, y + 7, color);
            }
            case "COMBAT", "PVP" -> {
                // Shield / Crosshair vector
                drawOutlineBorder(context, x + 1, y + 1, size - 2, size - 3, color);
                context.fill(cx - 1, y + 3, cx + 1, y + size - 4, color);
                context.fill(x + 3, cy - 1, x + size - 3, cy, color);
            }
            case "VISUAL" -> {
                // Sparkle / Wand vector
                context.fill(cx, y + 1, cx + 1, y + size - 1, color);
                context.fill(x + 1, cy, x + size - 1, cy + 1, color);
                context.fill(cx - 2, cy - 2, cx + 3, cy + 3, color);
            }
            case "INVENTORY" -> {
                drawOutlineBorder(context, x + 1, y + 2, size - 2, size - 3, color);
                context.fill(x + 3, y, x + size - 3, y + 2, color);
                context.fill(x + 3, cy, x + size - 3, cy + 1, color);
            }
            case "MOVEMENT" -> {
                // Double chevrons (>>)
                context.fill(x + 2, cy - 3, x + 4, cy - 1, color);
                context.fill(x + 4, cy - 1, x + 6, cy + 1, color);
                context.fill(x + 2, cy + 1, x + 4, cy + 3, color);
                context.fill(x + 6, cy - 3, x + 8, cy - 1, color);
                context.fill(x + 8, cy - 1, x + 10, cy + 1, color);
                context.fill(x + 6, cy + 1, x + 8, cy + 3, color);
            }
            case "WORLD" -> {
                drawOutlineBorder(context, x + 1, y + 1, size - 2, size - 2, color);
                context.fill(x + 2, cy, x + size - 2, cy + 1, color);
                context.fill(cx, y + 2, cx + 1, y + size - 2, color);
            }
            case "CHAT" -> {
                drawOutlineBorder(context, x + 1, y + 1, size - 2, size - 4, color);
                context.fill(x + 3, y + 3, x + size - 3, y + 4, color);
                context.fill(x + 3, y + 5, x + size - 5, y + 6, color);
                context.fill(x + 2, y + size - 3, x + 4, y + size - 1, color);
            }
            case "AUDIO" -> {
                context.fill(x + 2, cy - 2, x + 5, cy + 2, color);
                context.fill(x + 5, y + 2, x + 7, y + size - 2, color);
                context.fill(x + 9, cy - 3, x + 10, cy + 3, color);
            }
            case "PERFORMANCE" -> {
                context.fill(x + 1, y + size - 3, x + 3, y + size - 1, color);
                context.fill(x + 4, y + size - 6, x + 6, y + size - 1, color);
                context.fill(x + 7, y + size - 9, x + 9, y + size - 1, color);
            }
            case "SETTINGS", "UTILITY" -> {
                drawOutlineBorder(context, x + 2, y + 2, size - 4, size - 4, color);
                context.fill(cx - 1, y, cx + 1, y + 2, color);
                context.fill(cx - 1, y + size - 2, cx + 1, y + size, color);
                context.fill(x, cy - 1, x + 2, cy + 1, color);
                context.fill(x + size - 2, cy - 1, x + size, cy + 1, color);
            }
            default -> {
                drawOutlineBorder(context, x + 1, y + 1, size - 2, size - 2, color);
                context.fill(cx - 1, cy - 1, cx + 1, cy + 1, color);
            }
        }
    }

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
                ? ColorUtils.withAlpha(tintColor, 0.20f)
                : ColorUtils.withAlpha(tintColor, 0.08f);
        int borderFill = filledAccent
                ? ColorUtils.withAlpha(tintColor, 0.50f)
                : ColorUtils.withAlpha(tintColor, 0.22f);

        context.fill(x + 1, y, x + width - 1, y + height, bgFill);
        context.fill(x, y + 1, x + 1, y + height - 1, bgFill);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, bgFill);

        context.fill(x + 1, y, x + width - 1, y + 1, borderFill);
        context.fill(x + 1, y + height - 1, x + width - 1, y + height, ColorUtils.multiplyAlpha(borderFill, 0.65f));
    }

    public static void drawOutlineBorder(DrawContext c, int x, int y, int w, int h, int col) {
        if (c == null || w <= 0 || h <= 0) return;
        c.fill(x, y, x + w, y + 1, col);
        c.fill(x, y + h - 1, x + w, y + h, col);
        c.fill(x, y + 1, x + 1, y + h - 1, col);
        c.fill(x + w - 1, y + 1, x + w, y + h - 1, col);
    }

    public static void drawHorizontalDivider(DrawContext context, int x, int y, int width) {
        if (context == null || width <= 4) {
            return;
        }
        Theme theme = getTheme();
        context.fill(x, y, x + width, y + 1, theme.getBorderSubtle());
    }
}
