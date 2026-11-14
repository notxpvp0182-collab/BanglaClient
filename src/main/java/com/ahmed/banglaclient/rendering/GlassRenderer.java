package com.ahmed.banglaclient.rendering;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;

/**
 * Pure 2D Aura Dark & White Glass Renderer (Zero 3D shading, Zero lag, Crisp 1px borders).
 */
public final class GlassRenderer {

    private GlassRenderer() {}

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
        if (context == null || width < 2 || height < 2) return;
        Theme theme = getTheme();

        int fill = hovered ? ColorUtils.scaleBrightness(surfaceArgb, 1.15f) : surfaceArgb;

        // Clean 2D flat rectangle with 1px anti-aliased corners
        context.fill(x + 1, y, x + width - 1, y + height, fill);
        context.fill(x, y + 1, x + 1, y + height - 1, fill);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, fill);

        if (drawBorder) {
            int borderCol;
            if (activeAccentBorder) {
                borderCol = 0x99FFFFFF;
            } else if (hovered) {
                borderCol = theme.getBorderStrong();
            } else {
                borderCol = theme.getBorderSubtle();
            }
            drawOutlineBorder(context, x, y, width, height, borderCol);
        }
    }

    public static void drawVectorIcon(DrawContext context, int x, int y, int size, String iconType, int color) {
        if (context == null || size < 6) return;
        int cx = x + size / 2;
        int cy = y + size / 2;

        switch (iconType == null ? "" : iconType.toUpperCase()) {
            case "DASHBOARD", "HOME" -> {
                int half = (size - 2) / 2;
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
                drawOutlineBorder(context, x + 1, y + 1, size - 2, size - 2, color);
                context.fill(cx, y + 2, cx + 1, y + size - 2, color);
                context.fill(x + 2, cy, x + size - 2, cy + 1, color);
            }
            case "VISUAL" -> {
                context.fill(cx, y + 1, cx + 1, y + size - 1, color);
                context.fill(x + 1, cy, x + size - 1, cy + 1, color);
                context.fill(cx - 1, cy - 1, cx + 2, cy + 2, color);
            }
            case "INVENTORY" -> {
                drawOutlineBorder(context, x + 1, y + 2, size - 2, size - 3, color);
                context.fill(x + 3, y, x + size - 3, y + 2, color);
                context.fill(x + 3, cy, x + size - 3, cy + 1, color);
            }
            case "MOVEMENT" -> {
                context.fill(x + 1, cy - 2, x + 3, cy, color);
                context.fill(x + 3, cy, x + 5, cy + 1, color);
                context.fill(x + 1, cy + 1, x + 3, cy + 3, color);
                context.fill(x + 5, cy - 2, x + 7, cy, color);
                context.fill(x + 7, cy, x + 9, cy + 1, color);
                context.fill(x + 5, cy + 1, x + 7, cy + 3, color);
            }
            case "WORLD" -> {
                drawOutlineBorder(context, x + 1, y + 1, size - 2, size - 2, color);
                context.fill(x + 2, cy, x + size - 2, cy + 1, color);
                context.fill(cx, y + 2, cx + 1, y + size - 2, color);
            }
            case "CHAT" -> {
                drawOutlineBorder(context, x + 1, y + 1, size - 2, size - 3, color);
                context.fill(x + 3, y + 3, x + size - 3, y + 4, color);
                context.fill(x + 2, y + size - 2, x + 4, y + size, color);
            }
            case "AUDIO" -> {
                context.fill(x + 1, cy - 2, x + 4, cy + 2, color);
                context.fill(x + 4, y + 2, x + 6, y + size - 2, color);
                context.fill(x + 8, cy - 2, x + 9, cy + 2, color);
            }
            case "PERFORMANCE" -> {
                context.fill(x + 1, y + size - 3, x + 3, y + size - 1, color);
                context.fill(x + 4, y + size - 6, x + 6, y + size - 1, color);
                context.fill(x + 7, y + size - 9, x + 9, y + size - 1, color);
            }
            case "UTILITY" -> {
                drawOutlineBorder(context, x + 1, y + 1, size - 2, size - 2, color);
                context.fill(x + 3, y + 3, x + size - 3, y + size - 3, color);
            }
            case "SETTINGS" -> {
                drawOutlineBorder(context, x + 2, y + 2, size - 4, size - 4, color);
                context.fill(cx, y, cx + 1, y + 2, color);
                context.fill(cx, y + size - 2, cx + 1, y + size, color);
                context.fill(x, cy, x + 2, cy + 1, color);
                context.fill(x + size - 2, cy, x + size, cy + 1, color);
            }
            case "ABOUT", "INFO" -> {
                drawOutlineBorder(context, x + 1, y + 1, size - 2, size - 2, color);
                context.fill(cx, y + 3, cx + 1, y + 4, color);
                context.fill(cx, y + 5, cx + 1, y + size - 2, color);
            }
            case "ACCOUNT" -> {
                context.fill(cx - 2, y + 1, cx + 2, y + 5, color);
                context.fill(x + 2, y + 6, x + size - 2, y + size - 1, color);
            }
            case "REPLAY" -> {
                drawOutlineBorder(context, x + 1, y + 1, size - 2, size - 2, color);
                context.fill(x + 4, y + 3, x + 6, y + size - 3, color);
                context.fill(x + 6, y + 4, x + 8, y + size - 4, color);
            }
            case "PACKS" -> {
                context.fill(x + 1, y + 2, x + 5, y + 3, color);
                drawOutlineBorder(context, x + 1, y + 3, size - 2, size - 4, color);
            }
            default -> {
                drawOutlineBorder(context, x + 1, y + 1, size - 2, size - 2, color);
                context.fill(cx - 1, cy - 1, cx + 2, cy + 2, color);
            }
        }
    }

    public static void drawPillBadge(DrawContext context, int x, int y, int width, int height, int tintColor, boolean filledAccent) {
        if (context == null || width < 3 || height < 3) return;
        int bgFill = filledAccent ? ColorUtils.withAlpha(tintColor, 0.22f) : ColorUtils.withAlpha(0x0E1015, 0.82f);
        int borderFill = filledAccent ? 0x88FFFFFF : 0x26FFFFFF;

        context.fill(x + 1, y, x + width - 1, y + height, bgFill);
        context.fill(x, y + 1, x + 1, y + height - 1, bgFill);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, bgFill);
        drawOutlineBorder(context, x, y, width, height, borderFill);
    }

    public static void drawOutlineBorder(DrawContext c, int x, int y, int w, int h, int col) {
        if (c == null || w <= 0 || h <= 0) return;
        c.fill(x + 1, y, x + w - 1, y + 1, col);
        c.fill(x + 1, y + h - 1, x + w - 1, y + h, col);
        c.fill(x, y + 1, x + 1, y + h - 1, col);
        c.fill(x + w - 1, y + 1, x + w, y + h - 1, col);
    }

    public static void drawHorizontalDivider(DrawContext context, int x, int y, int width) {
        if (context == null || width <= 2) return;
        context.fill(x, y, x + width, y + 1, ThemeTokens.DEFAULT_BORDER_SUBTLE);
    }
}
