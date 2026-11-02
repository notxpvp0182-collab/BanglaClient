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

        // Special Bottom-Right Bangla SMP Card Widget
        if ("__BANGLA_SMP__".equals(label)) {
            drawCapsule(context, x, y, w, h, 0x55080A10, 0x6A080A10, hov ? 0xFFEF4444 : 0x88EF4444);
            context.drawTextWithShadow(mc.textRenderer, "BANGLA SMP", x + 10, y + 5, 0xFFEF4444);
            context.drawTextWithShadow(mc.textRenderer, "play.banglasmp.net", x + 10, y + 16, 0xFFFFFFFF);
            ci.cancel();
            return;
        }

        // Special Top-Right Account Display Pill
        if (label.startsWith("__USER__:")) {
            String uname = label.substring(9);
            drawCapsule(context, x, y, w, h, 0x45121622, 0x5A080A10, 0x66FFFFFF);
            context.drawTextWithShadow(mc.textRenderer, "• " + uname, x + 7, y + (h - 8) / 2, 0xFFFFFFFF);
            ci.cancel();
            return;
        }

        // Special Top-Left / Top-Right Vector Icon Buttons
        if (label.startsWith("__ICON_")) {
            if ("__ICON_BC__".equals(label)) {
                int fill = hov ? ColorUtils.scaleBrightness(accent, 1.2f) : accent;
                context.fill(x, y, x + w, y + h, fill);
                GlassRenderer.drawOutlineBorder(context, x, y, w, h, 0xAAFFFFFF);
                context.drawTextWithShadow(mc.textRenderer, "BC", x + (w - mc.textRenderer.getWidth("BC")) / 2, y + (h - 8) / 2, 0xFFFFFFFF);
            } else {
                int bgTop = hov ? ColorUtils.withAlpha(accent, 0.45f) : 0x45121622;
                int bgBot = hov ? ColorUtils.withAlpha(accent, 0.25f) : 0x5A080A10;
                int border = hov ? 0xFFFFFFFF : 0x77FFFFFF;
                drawCapsule(context, x, y, w, h, bgTop, bgBot, border);
                int icCol = hov ? accent : 0xFFFFFFFF;
                int ix = x + (w - 16) / 2;
                int iy = y + (h - 16) / 2;
                switch (label) {
                    case "__ICON_MODS__" -> drawPuzzleIcon(context, ix, iy, icCol);
                    case "__ICON_IAS__" -> drawUserIcon(context, ix, iy, icCol);
                    case "__ICON_COLOR__" -> drawPaletteIcon(context, ix, iy, accent, hov);
                    case "__ICON_HUD__" -> drawHudEditorIcon(context, ix, iy, icCol);
                    case "__ICON_PACKS__" -> drawFolderIcon(context, ix, iy, icCol);
                    case "__ICON_INFO__" -> drawInfoIcon(context, ix, iy, icCol);
                    default -> drawGearIcon(context, ix, iy, icCol);
                }
            }
            ci.cancel();
            return;
        }

        // Standard Menu / Pause / Vanilla Buttons
        boolean isRedQuitBtn = label.contains("Quit") || label.contains("Disconnect");
        if (isRedQuitBtn) {
            int bgTop = hov ? 0xE8EF4444 : 0xCCDC2626;
            int bgBot = hov ? 0xE8B91C1C : 0xCC991B1B;
            int border = hov ? 0xFFFFFFFF : 0x99FFFFFF;
            drawCapsule(context, x, y, w, h, bgTop, bgBot, border);
        } else {
            int bgTop = hov ? ColorUtils.withAlpha(accent, 0.38f) : 0x42141824;
            int bgBot = hov ? ColorUtils.withAlpha(accent, 0.18f) : 0x580A0D14;
            int border = hov ? ColorUtils.withAlpha(accent, 0.95f) : 0x66FFFFFF;
            drawCapsule(context, x, y, w, h, bgTop, bgBot, border);
        }

        if (!label.isEmpty()) {
            TextRenderer tr = mc.textRenderer;
            String trimmed = tr.trimToWidth(label, w - 10);
            int tw = tr.getWidth(trimmed);
            int txtCol = !this.active ? theme.getTextMuted() : 0xFFFFFFFF;
            context.drawTextWithShadow(tr, trimmed, x + (w - tw) / 2, y + (h - 8) / 2, txtCol);
        }
        ci.cancel();
    }

    @Unique
    private static void drawCapsule(DrawContext context, int x, int y, int w, int h, int bgTop, int bgBot, int borderCol) {
        context.fillGradient(x + 2, y, x + w - 2, y + h, bgTop, bgBot);
        context.fillGradient(x + 1, y + 1, x + 2, y + h - 1, bgTop, bgBot);
        context.fillGradient(x + w - 2, y + 1, x + w - 1, y + h - 1, bgTop, bgBot);
        context.fill(x, y + 2, x + 1, y + h - 2, bgTop);
        context.fill(x + w - 1, y + 2, x + w, y + h - 2, bgTop);
        context.fill(x + 3, y + 1, x + w - 3, y + 2, 0x44FFFFFF);
        GlassRenderer.drawOutlineBorder(context, x, y, w, h, borderCol);
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
        context.fill(x + 6, y + 3, x + 10, y + 7, col);
        context.fill(x + 3, y + 8, x + 13, y + 13, col);
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
    private static void drawHudEditorIcon(DrawContext context, int x, int y, int col) {
        // Crisp HUD layout reticle / brackets icon
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
