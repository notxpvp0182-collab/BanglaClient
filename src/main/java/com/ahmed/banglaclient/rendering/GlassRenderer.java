package com.ahmed.banglaclient.rendering;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;


import org.joml.Matrix3x2fStack;

import java.util.Locale;

/**
 * AMOLED Glass Renderer + Small-Text Font Style Renderer + Per-Module Unique Vector Icon Engine.
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

    public static void playClickSound() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.getSoundManager() != null) {
            try {
                net.minecraft.client.gui.widget.ButtonWidget.builder(net.minecraft.text.Text.empty(), b -> {}).build().playDownSound(mc.getSoundManager());
            } catch (Throwable ignored) {}
        }
    }

    /**
     * Converts standard English text to clean Small-Caps style (e.g., "Singleplayer" -> "ꜱɪɴɢʟᴇᴘʟᴀʏᴇʀ").
     */
    public static String toSmallCaps(String input) {
        if (input == null || input.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (Character.toLowerCase(c)) {
                case 'a' -> sb.append('ᴀ');
                case 'b' -> sb.append('ʙ');
                case 'c' -> sb.append('ᴄ');
                case 'd' -> sb.append('ᴅ');
                case 'e' -> sb.append('ᴇ');
                case 'f' -> sb.append('ꜰ');
                case 'g' -> sb.append('ɢ');
                case 'h' -> sb.append('ʜ');
                case 'i' -> sb.append('ɪ');
                case 'j' -> sb.append('ᴊ');
                case 'k' -> sb.append('ᴋ');
                case 'l' -> sb.append('ʟ');
                case 'm' -> sb.append('ᴍ');
                case 'n' -> sb.append('ɴ');
                case 'o' -> sb.append('ᴏ');
                case 'p' -> sb.append('ᴘ');
                case 'q' -> sb.append('ǫ');
                case 'r' -> sb.append('ʀ');
                case 's' -> sb.append('ꜱ');
                case 't' -> sb.append('ᴛ');
                case 'u' -> sb.append('ᴜ');
                case 'v' -> sb.append('ᴠ');
                case 'w' -> sb.append('ᴡ');
                case 'x' -> sb.append('x');
                case 'y' -> sb.append('ʏ');
                case 'z' -> sb.append('ᴢ');
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    public static void drawSmallCapsText(DrawContext context, TextRenderer tr, String text, int x, int y, int color) {
        if (context == null || tr == null || text == null) return;
        context.drawTextWithShadow(tr, toSmallCaps(text), x, y, color);
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

        int fill = hovered ? ColorUtils.scaleBrightness(surfaceArgb, 1.25f) : surfaceArgb;

        // AMOLED deep dark glass body
        context.fill(x + 1, y, x + width - 1, y + height, fill);
        context.fill(x, y + 1, x + 1, y + height - 1, fill);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, fill);

        // Subtle top inner glass reflection line
        if (width > 6 && height > 6) {
            context.fill(x + 2, y + 1, x + width - 2, y + 2, hovered ? 0x2EFFFFFF : 0x16FFFFFF);
        }

        if (drawBorder) {
            int borderCol = activeAccentBorder ? 0xCCFFFFFF : (hovered ? theme.getBorderStrong() : theme.getBorderSubtle());
            drawOutlineBorder(context, x, y, width, height, borderCol);
        }
    }

    /**
     * Draws a unique custom 2D vector icon specifically tailored to each individual module's ID.
     */
    public static void drawModuleIcon(DrawContext c, int x, int y, int size, String moduleId, int col) {
        if (c == null || moduleId == null) return;
        String id = moduleId.toLowerCase(Locale.ROOT);
        int cx = x + size / 2;
        int cy = y + size / 2;

        if (id.contains("fps")) {
            drawOutlineBorder(c, x, y + 1, size, size - 2, col);
            c.fill(x + 2, y + size - 3, x + 4, y + size - 2, col);
            c.fill(x + 5, y + 4, x + 7, y + size - 2, col);
        } else if (id.contains("cps") || id.contains("click") || id.contains("mouse")) {
            drawOutlineBorder(c, x + 2, y + 1, size - 4, size - 2, col);
            c.fill(cx, y + 2, cx + 1, y + 5, col);
        } else if (id.contains("keystroke") || id.contains("keybind")) {
            drawOutlineBorder(c, x + 3, y + 1, 4, 4, col);
            drawOutlineBorder(c, x, y + 5, 4, 4, col);
            drawOutlineBorder(c, x + 3, y + 5, 4, 4, col);
            drawOutlineBorder(c, x + 6, y + 5, 4, 4, col);
        } else if (id.contains("coord") || id.contains("direction") || id.contains("compass") || id.contains("waypoint") || id.contains("spawn")) {
            drawOutlineBorder(c, x + 1, y + 1, size - 2, size - 2, col);
            c.fill(cx, y, cx + 1, y + size, col);
            c.fill(x, cy, x + size, cy + 1, col);
        } else if (id.contains("ping") || id.contains("latency") || id.contains("server")) {
            c.fill(x + 1, y + size - 3, x + 3, y + size - 1, col);
            c.fill(x + 4, y + size - 5, x + 6, y + size - 1, col);
            c.fill(x + 7, y + 2, x + 9, y + size - 1, col);
        } else if (id.contains("clock") || id.contains("time") || id.contains("session") || id.contains("day") || id.contains("delay")) {
            drawOutlineBorder(c, x + 1, y + 1, size - 2, size - 2, col);
            c.fill(cx, y + 3, cx + 1, cy + 1, col);
            c.fill(cx, cy, cx + 3, cy + 1, col);
        } else if (id.contains("speed") || id.contains("sprint") || id.contains("flight") || id.contains("jump") || id.contains("step")) {
            c.fill(x + 1, cy - 2, x + 4, cy, col);
            c.fill(x + 3, cy, x + 6, cy + 2, col);
            c.fill(x + 5, cy - 2, x + 8, cy, col);
            c.fill(x + 7, cy, x + 10, cy + 2, col);
        } else if (id.contains("armor") || id.contains("shield") || id.contains("equipment")) {
            drawOutlineBorder(c, x + 1, y + 1, size - 2, size - 3, col);
            c.fill(x + 3, y + size - 3, x + size - 3, y + size - 1, col);
        } else if (id.contains("potion") || id.contains("food") || id.contains("health") || id.contains("totem")) {
            c.fill(cx - 2, y + 1, cx + 3, y + 3, col);
            drawOutlineBorder(c, x + 2, y + 3, size - 4, size - 4, col);
            c.fill(x + 3, cy, x + size - 3, y + size - 2, col);
        } else if (id.contains("crosshair") || id.contains("hit") || id.contains("target") || id.contains("combo") || id.contains("attack")) {
            c.fill(cx, y + 1, cx + 1, y + size - 1, col);
            c.fill(x + 1, cy, x + size - 1, cy + 1, col);
            drawOutlineBorder(c, cx - 2, cy - 2, 5, 5, col);
        } else if (id.contains("crystal") || id.contains("anchor") || id.contains("pearl") || id.contains("arrow")) {
            drawOutlineBorder(c, x + 2, y + 2, size - 4, size - 4, col);
            c.fill(cx - 1, cy - 1, cx + 2, cy + 2, col);
        } else if (id.contains("fullbright") || id.contains("light") || id.contains("sky") || id.contains("moon") || id.contains("weather")) {
            c.fill(cx - 2, cy - 2, cx + 3, cy + 3, col);
            c.fill(cx, y, cx + 1, y + 2, col);
            c.fill(cx, y + size - 2, cx + 1, y + size, col);
            c.fill(x, cy, x + 2, cy + 1, col);
            c.fill(x + size - 2, cy, x + size, cy + 1, col);
        } else if (id.contains("zoom") || id.contains("search")) {
            drawOutlineBorder(c, x + 1, y + 1, 6, 6, col);
            c.fill(x + 6, y + 6, x + 9, y + 9, col);
        } else if (id.contains("fire") || id.contains("particle") || id.contains("glint") || id.contains("damage")) {
            c.fill(cx, y + 1, cx + 2, y + size - 1, col);
            c.fill(x + 2, cy, x + size - 2, cy + 3, col);
        } else if (id.contains("chat") || id.contains("auto_gg") || id.contains("message") || id.contains("notify")) {
            drawOutlineBorder(c, x + 1, y + 1, size - 2, size - 3, col);
            c.fill(x + 3, y + 3, x + size - 3, y + 4, col);
            c.fill(x + 2, y + size - 2, x + 4, y + size, col);
        } else if (id.contains("sound") || id.contains("music") || id.contains("ambient") || id.contains("audio")) {
            c.fill(x + 1, cy - 2, x + 4, cy + 2, col);
            c.fill(x + 4, y + 2, x + 6, y + size - 2, col);
            c.fill(x + 8, cy - 2, x + 9, cy + 2, col);
        } else if (id.contains("memory") || id.contains("graph") || id.contains("performance") || id.contains("chunk")) {
            drawOutlineBorder(c, x + 1, y + 1, size - 2, size - 2, col);
            c.fill(x + 3, cy, x + 5, y + size - 2, col);
            c.fill(x + 6, y + 3, x + 8, y + size - 2, col);
        } else {
            drawOutlineBorder(c, x + 1, y + 1, size - 2, size - 2, col);
            c.fill(x + 3, y + 3, x + size - 3, y + size - 3, col);
        }
    }

    public static void drawVectorIcon(DrawContext context, int x, int y, int size, String iconType, int color) {
        if (context == null || size < 6) return;
        int cx = x + size / 2;
        int cy = y + size / 2;

        switch (iconType == null ? "" : iconType.toUpperCase(Locale.ROOT)) {
            case "DASHBOARD", "HOME", "MODS" -> {
                int half = (size - 2) / 2;
                drawOutlineBorder(context, x, y, half, half, color);
                drawOutlineBorder(context, x + half + 2, y, half, half, color);
                drawOutlineBorder(context, x, y + half + 2, half, half, color);
                drawOutlineBorder(context, x + half + 2, y + half + 2, half, half, color);
            }
            case "SINGLEPLAYER" -> {
                context.fill(cx - 2, y + 1, cx + 2, y + 5, color);
                context.fill(x + 2, y + 6, x + size - 2, y + size - 1, color);
            }
            case "MULTIPLAYER" -> {
                context.fill(x + 2, y + 2, x + 5, y + 5, color);
                context.fill(x + 1, y + 6, x + 6, y + size - 1, color);
                context.fill(x + 6, y + 1, x + 9, y + 4, color);
                context.fill(x + 5, y + 5, x + 10, y + size - 1, color);
            }
            case "REALMS", "WORLD" -> {
                drawOutlineBorder(context, x + 1, y + 1, size - 2, size - 2, color);
                context.fill(x + 2, cy, x + size - 2, cy + 1, color);
                context.fill(cx, y + 2, cx + 1, y + size - 2, color);
            }
            case "COSMETICS", "VISUAL" -> {
                context.fill(cx, y + 1, cx + 1, y + size - 1, color);
                context.fill(x + 1, cy, x + size - 1, cy + 1, color);
                context.fill(cx - 1, cy - 1, cx + 2, cy + 2, color);
            }
            case "OPTIONS", "SETTINGS" -> {
                drawOutlineBorder(context, x + 2, y + 2, size - 4, size - 4, color);
                context.fill(cx, y, cx + 1, y + 2, color);
                context.fill(cx, y + size - 2, cx + 1, y + size, color);
                context.fill(x, cy, x + 2, cy + 1, color);
                context.fill(x + size - 2, cy, x + size, cy + 1, color);
            }
            case "QUIT", "DISCONNECT" -> {
                drawOutlineBorder(context, x + 1, y + 1, size - 4, size - 2, color);
                context.fill(cx, cy, x + size, cy + 1, color);
                context.fill(x + size - 2, cy - 2, x + size, cy + 3, color);
            }
            case "BACK_GAME", "REPLAY" -> {
                context.fill(x + 3, y + 2, x + 5, y + size - 2, color);
                context.fill(x + 5, y + 3, x + 7, y + size - 3, color);
                context.fill(x + 7, y + 4, x + 9, y + size - 4, color);
            }
            case "ADVANCEMENTS" -> {
                drawOutlineBorder(context, x + 2, y + 1, size - 4, size - 4, color);
                context.fill(cx - 1, y + size - 3, cx + 2, y + size, color);
            }
            case "STATISTICS", "PERFORMANCE" -> {
                context.fill(x + 1, y + size - 3, x + 3, y + size - 1, color);
                context.fill(x + 4, y + size - 6, x + 6, y + size - 1, color);
                context.fill(x + 7, y + size - 9, x + 9, y + size - 1, color);
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
            case "INVENTORY", "WARDROBE" -> {
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
            case "ABOUT", "INFO" -> {
                drawOutlineBorder(context, x + 1, y + 1, size - 2, size - 2, color);
                context.fill(cx, y + 3, cx + 1, y + 4, color);
                context.fill(cx, y + 5, cx + 1, y + size - 2, color);
            }
            case "ACCOUNT" -> {
                context.fill(cx - 2, y + 1, cx + 2, y + 5, color);
                context.fill(x + 2, y + 6, x + size - 2, y + size - 1, color);
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
        int bgFill = filledAccent ? ColorUtils.withAlpha(tintColor, 0.24f) : ColorUtils.withAlpha(0x07080B, 0.88f);
        int borderFill = filledAccent ? 0xAAFFFFFF : 0x2EFFFFFF;

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
