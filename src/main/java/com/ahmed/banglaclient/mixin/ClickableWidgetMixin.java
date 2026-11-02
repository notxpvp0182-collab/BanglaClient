package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.ColorUtils;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClickableWidget.class)
public abstract class ClickableWidgetMixin {

    @Shadow public abstract int getX();
    @Shadow public abstract int getY();
    @Shadow public abstract int getWidth();
    @Shadow public abstract int getHeight();
    @Shadow public abstract Text getMessage();
    @Shadow public boolean active;
    @Shadow public boolean visible;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void banglaclient$renderPureLiquidGlassButton(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!this.visible || !((Object) this instanceof PressableWidget)) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null) return;

        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        if (w < 10 || h < 10) return;

        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : 0xFFEF4444;

        boolean hov = this.active && MathUtils.isHovered(mouseX, mouseY, x, y, w, h);
        String label = getMessage() != null ? getMessage().getString() : "";

        // 1. Bottom-Right Bangla SMP Card
        if ("__BANGLA_SMP__".equals(label)) {
            drawGlassCapsule(context, x, y, w, h, 0x4E10131C, 0x68080A10, hov ? 0xFFEF4444 : 0x88FFFFFF, hov ? 0x55EF4444 : 0x22EF4444);
            context.drawTextWithShadow(mc.textRenderer, "BANGLA SMP", x + 10, y + 5, 0xFFEF4444);
            context.drawTextWithShadow(mc.textRenderer, "play.banglasmp.net", x + 10, y + 16, 0xFFFFFFFF);
            ci.cancel();
            return;
        }

        // 2. Top-Right Account Display Pill
        if (label.startsWith("__USER__:")) {
            String uname = label.substring(9);
            drawGlassCapsule(context, x, y, w, h, 0x48141824, 0x600A0D14, hov ? 0xFFFFFFFF : 0x88FFFFFF, 0x22FFFFFF);
            context.drawTextWithShadow(mc.textRenderer, "• " + uname, x + 7, y + (h - 8) / 2, 0xFFFFFFFF);
            ci.cancel();
            return;
        }

        // 3. Left-Side Dynamic Mod Feature Pills (Image 3 style: Red icon on left + label on right)
        if (label.startsWith("__LEFT_")) {
            int sep = label.indexOf(':');
            String type = sep > 0 ? label.substring(0, sep) : label;
            String text = sep > 0 ? label.substring(sep + 1) : "Mod";
            int bgTop = hov ? ColorUtils.withAlpha(accent, 0.42f) : 0x48141824;
            int bgBot = hov ? ColorUtils.withAlpha(accent, 0.22f) : 0x62090C12;
            int border = hov ? 0xFFFFFFFF : 0x77FFFFFF;
            drawGlassCapsule(context, x, y, w, h, bgTop, bgBot, border, hov ? 0x44EF4444 : 0x18EF4444);

            int ix = x + 5, iy = y + (h - 14) / 2;
            switch (type) {
                case "__LEFT_IAS__" -> drawUserIcon(context, ix, iy, 0xFFEF4444);
                case "__LEFT_REPLAY__" -> drawPlayCircleIcon(context, ix, iy, 0xFFEF4444);
                case "__LEFT_FLASHBACK__" -> drawRewindIcon(context, ix, iy, 0xFFEF4444);
                default -> drawPuzzleIcon(context, ix, iy, 0xFFEF4444);
            }
            String trimmed = mc.textRenderer.trimToWidth(text, w - 24);
            context.drawTextWithShadow(mc.textRenderer, trimmed, x + 22, y + (h - 8) / 2, 0xFFFFFFFF);
            ci.cancel();
            return;
        }

        // 4. Top-Right Compact Square Toolbar Icons
        if (label.startsWith("__ICON_")) {
            int bgTop = hov ? ColorUtils.withAlpha(accent, 0.48f) : 0x48141824;
            int bgBot = hov ? ColorUtils.withAlpha(accent, 0.26f) : 0x62090C12;
            int border = hov ? 0xFFFFFFFF : 0x80FFFFFF;
            drawGlassCapsule(context, x, y, w, h, bgTop, bgBot, border, hov ? 0x44EF4444 : 0x18FFFFFF);
            int icCol = hov ? accent : 0xFFFFFFFF;
            int ix = x + (w - 16) / 2;
            int iy = y + (h - 16) / 2;
            switch (label) {
                case "__ICON_GUI__" -> drawGridGuiIcon(context, ix, iy, accent);
                case "__ICON_HUD__" -> drawHudReticleIcon(context, ix, iy, icCol);
                case "__ICON_COLOR__" -> drawPaletteIcon(context, ix, iy, accent, hov);
                case "__ICON_OPT__" -> drawGearIcon(context, ix, iy, icCol);
                case "__ICON_COSM__" -> drawShirtIcon(context, ix, iy, icCol);
                case "__ICON_PACKS__" -> drawFolderIcon(context, ix, iy, icCol);
                case "__ICON_INFO__" -> drawInfoIcon(context, ix, iy, icCol);
                default -> drawGearIcon(context, ix, iy, icCol);
            }
            ci.cancel();
            return;
        }

        // 5. Pause Menu Buttons with Left-Aligned Vector Icons (Image 4 style)
        String pauseIcon = null;
        if (label.startsWith("__PAUSE_")) {
            int sep = label.indexOf(':');
            if (sep > 0) {
                pauseIcon = label.substring(8, sep);
                label = label.substring(sep + 1);
            }
        }

        boolean isRedQuitBtn = label.contains("Quit") || label.contains("Disconnect");
        if (isRedQuitBtn && pauseIcon == null) {
            int bgTop = hov ? 0xEEEF4444 : 0xD8DC2626;
            int bgBot = hov ? 0xEEB91C1C : 0xD8991B1B;
            drawGlassCapsule(context, x, y, w, h, bgTop, bgBot, hov ? 0xFFFFFFFF : 0xBBFFFFFF, 0x55EF4444);
        } else if (pauseIcon != null) {
            // Image 4 Red-and-White glowing Liquid Glass Pause button
            int bgTop = hov ? 0xB8EF4444 : 0x78991B1B;
            int bgBot = hov ? 0x98991B1B : 0x68450A0A;
            drawGlassCapsule(context, x, y, w, h, bgTop, bgBot, hov ? 0xFFFFFFFF : 0xAAFCA5A5, 0x44EF4444);
        } else {
            // Image 3 Frosted Translucent Liquid Glass Main Menu button
            int bgTop = hov ? ColorUtils.withAlpha(accent, 0.44f) : 0x44222634;
            int bgBot = hov ? ColorUtils.withAlpha(accent, 0.24f) : 0x5A10131C;
            int border = hov ? 0xFFFFFFFF : 0x80FFFFFF;
            drawGlassCapsule(context, x, y, w, h, bgTop, bgBot, border, hov ? 0x44EF4444 : 0x18FFFFFF);
        }

        if (!label.isEmpty()) {
            TextRenderer tr = mc.textRenderer;
            String trimmed = tr.trimToWidth(label, w - (pauseIcon != null ? 26 : 10));
            int tw = tr.getWidth(trimmed);
            int txtCol = !this.active ? theme.getTextMuted() : 0xFFFFFFFF;
            if (pauseIcon != null) {
                int totalW = 16 + 5 + tw;
                int startX = x + (w - totalW) / 2;
                drawPauseVectorIcon(context, pauseIcon, startX, y + (h - 14) / 2);
                context.drawTextWithShadow(tr, trimmed, startX + 20, y + (h - 8) / 2, txtCol);
            } else {
                context.drawTextWithShadow(tr, trimmed, x + (w - tw) / 2, y + (h - 8) / 2, txtCol);
            }
        }
        ci.cancel();
    }

    @Unique
    private static void drawGlassCapsule(DrawContext context, int x, int y, int w, int h, int bgTop, int bgBot, int borderCol, int outerGlow) {
        // Subtle outer glow rim
        context.fill(x - 1, y + 1, x + w + 1, y + h - 1, outerGlow);
        // Rounded capsule fill
        context.fillGradient(x + 2, y, x + w - 2, y + h, bgTop, bgBot);
        context.fillGradient(x + 1, y + 1, x + 2, y + h - 1, bgTop, bgBot);
        context.fillGradient(x + w - 2, y + 1, x + w - 1, y + h - 1, bgTop, bgBot);
        context.fill(x, y + 2, x + 1, y + h - 2, bgTop);
        context.fill(x + w - 1, y + 2, x + w, y + h - 2, bgTop);
        // Top specular glass highlight
        context.fill(x + 3, y + 1, x + w - 3, y + 2, 0x55FFFFFF);
        GlassRenderer.drawOutlineBorder(context, x, y, w, h, borderCol);
    }

    @Unique
    private static void drawPauseVectorIcon(DrawContext context, String type, int x, int y) {
        switch (type) {
            case "PLAY" -> {
                context.fill(x + 4, y + 2, x + 6, y + 12, 0xFFFFFFFF);
                context.fill(x + 6, y + 4, x + 8, y + 10, 0xFFFFFFFF);
                context.fill(x + 8, y + 6, x + 10, y + 8, 0xFFFFFFFF);
            }
            case "TROPHY" -> {
                context.fill(x + 3, y + 2, x + 11, y + 7, 0xFFFBBF24);
                context.fill(x + 6, y + 7, x + 8, y + 10, 0xFFF59E0B);
                context.fill(x + 4, y + 10, x + 10, y + 12, 0xFFFBBF24);
            }
            case "STATS" -> {
                context.fill(x + 2, y + 8, x + 5, y + 12, 0xFFFFFFFF);
                context.fill(x + 6, y + 3, x + 9, y + 12, 0xFFFFFFFF);
                context.fill(x + 10, y + 6, x + 13, y + 12, 0xFFFFFFFF);
            }
            case "MODS" -> drawPuzzleIcon(context, x, y - 1, 0xFFFFFFFF);
            case "GUI" -> {
                context.fill(x + 3, y + 2, x + 5, y + 11, 0xFFFFFFFF);
                context.fill(x + 5, y + 4, x + 7, y + 9, 0xFFFFFFFF);
                context.fill(x + 7, y + 6, x + 10, y + 12, 0xFFFFFFFF);
            }
            case "OPT" -> drawGearIcon(context, x - 1, y - 1, 0xFFFFFFFF);
            case "LAN" -> drawUserIcon(context, x - 1, y - 1, 0xFFFFFFFF);
            case "EXIT" -> {
                context.fill(x + 2, y + 2, x + 8, y + 3, 0xFFFFFFFF);
                context.fill(x + 2, y + 2, x + 3, y + 12, 0xFFFFFFFF);
                context.fill(x + 2, y + 11, x + 8, y + 12, 0xFFFFFFFF);
                context.fill(x + 6, y + 6, x + 12, y + 8, 0xFFFFFFFF);
                context.fill(x + 9, y + 4, x + 11, y + 10, 0xFFFFFFFF);
            }
        }
    }

    @Unique
    private static void drawGridGuiIcon(DrawContext context, int x, int y, int accent) {
        context.fill(x + 3, y + 3, x + 7, y + 7, 0xFFEF4444);
        context.fill(x + 9, y + 3, x + 13, y + 7, 0xFF38BDF8);
        context.fill(x + 3, y + 9, x + 7, y + 13, 0xFF10B981);
        context.fill(x + 9, y + 9, x + 13, y + 13, 0xFFFBBF24);
    }

    @Unique
    private static void drawPuzzleIcon(DrawContext context, int x, int y, int col) {
        context.fill(x + 3, y + 3, x + 7, y + 7, col);
        context.fill(x + 9, y + 3, x + 13, y + 7, col);
        context.fill(x + 3, y + 9, x + 7, y + 13, col);
        context.fill(x + 9, y + 9, x + 13, y + 13, col);
        context.fill(x + 6, y + 5, x + 10, y + 11, col);
    }

    @Unique
    private static void drawUserIcon(DrawContext context, int x, int y, int col) {
        context.fill(x + 5, y + 2, x + 9, y + 6, col);
        context.fill(x + 2, y + 7, x + 12, y + 12, col);
    }

    @Unique
    private static void drawPlayCircleIcon(DrawContext context, int x, int y, int col) {
        context.fill(x + 2, y + 2, x + 12, y + 12, col);
        context.fill(x + 3, y + 3, x + 11, y + 11, 0xFF141824);
        context.fill(x + 5, y + 4, x + 9, y + 10, col);
    }

    @Unique
    private static void drawRewindIcon(DrawContext context, int x, int y, int col) {
        context.fill(x + 2, y + 2, x + 12, y + 12, col);
        context.fill(x + 4, y + 4, x + 10, y + 10, 0xFF141824);
        context.fill(x + 6, y + 5, x + 9, y + 9, col);
    }

    @Unique
    private static void drawPaletteIcon(DrawContext context, int x, int y, int accent, boolean hov) {
        context.fill(x + 3, y + 3, x + 13, y + 13, hov ? accent : 0xFFFFFFFF);
        context.fill(x + 4, y + 4, x + 12, y + 12, 0xFF141A24);
        context.fill(x + 5, y + 5, x + 7, y + 7, 0xFFEF4444);
        context.fill(x + 9, y + 5, x + 11, y + 7, 0xFF38BDF8);
        context.fill(x + 5, y + 9, x + 7, y + 11, 0xFF10B981);
        context.fill(x + 9, y + 9, x + 11, y + 11, 0xFFFBBF24);
    }

    @Unique
    private static void drawHudReticleIcon(DrawContext context, int x, int y, int col) {
        context.fill(x + 2, y + 3, x + 6, y + 4, col);
        context.fill(x + 2, y + 3, x + 3, y + 7, col);
        context.fill(x + 10, y + 3, x + 14, y + 4, col);
        context.fill(x + 13, y + 3, x + 14, y + 7, col);
        context.fill(x + 2, y + 12, x + 6, y + 13, col);
        context.fill(x + 2, y + 9, x + 3, y + 13, col);
        context.fill(x + 10, y + 12, x + 14, y + 13, col);
        context.fill(x + 13, y + 9, x + 14, y + 13, col);
        context.fill(x + 6, y + 6, x + 10, y + 10, col);
    }

    @Unique
    private static void drawShirtIcon(DrawContext context, int x, int y, int col) {
        context.fill(x + 3, y + 3, x + 13, y + 6, col);
        context.fill(x + 5, y + 6, x + 11, y + 13, col);
        context.fill(x + 6, y + 3, x + 10, y + 5, 0xFF141A24);
    }

    @Unique
    private static void drawFolderIcon(DrawContext context, int x, int y, int col) {
        context.fill(x + 2, y + 3, x + 6, y + 5, col);
        context.fill(x + 2, y + 5, x + 14, y + 13, col);
        context.fill(x + 4, y + 7, x + 12, y + 11, 0xFF141A24);
    }

    @Unique
    private static void drawInfoIcon(DrawContext context, int x, int y, int col) {
        context.fill(x + 7, y + 3, x + 9, y + 5, col);
        context.fill(x + 7, y + 6, x + 9, y + 12, col);
        context.fill(x + 5, y + 12, x + 11, y + 13, col);
    }

    @Unique
    private static void drawGearIcon(DrawContext context, int x, int y, int col) {
        context.fill(x + 6, y + 2, x + 10, y + 14, col);
        context.fill(x + 2, y + 6, x + 14, y + 10, col);
        context.fill(x + 4, y + 4, x + 12, y + 12, col);
        context.fill(x + 6, y + 6, x + 10, y + 10, 0xFF141A24);
    }
}
