package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.cosmetics.PlayerPreviewRenderer;
import com.ahmed.banglaclient.cosmetics.SkinCapeManager;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

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
    private void banglaclient$renderAmoledButtonWithCustomIcons(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!this.visible || !((Object) this instanceof PressableWidget)) return;
        // Do NOT override vanilla Inventory / Recipe Book / Container textured buttons!
        if ((Object) this instanceof TexturedButtonWidget) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null) return;
        if (mc.currentScreen instanceof HandledScreen<?>) return;

        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        if (w < 10 || h < 10) return;

        Theme theme = GlassRenderer.getTheme();
        boolean hov = this.active && MathUtils.isHovered(mouseX, mouseY, x, y, w, h);
        String label = getMessage() != null ? getMessage().getString() : "";

        if ("__BANGLA_SMP__".equals(label)) {
            drawAmoledBox(context, x, y, w, h, hov);
            context.fill(x + 7, y + 6, x + 11, y + 10, 0xFF34D399);
            GlassRenderer.drawVectorIcon(context, x + 6, y + 13, 8, "WORLD", 0xFFFFFFFF);
            GlassRenderer.drawSmallCapsText(context, mc.textRenderer, "BANGLA SMP", x + 17, y + 4, 0xFFFFFFFF);
            context.drawTextWithShadow(mc.textRenderer, "play.banglasmp.net", x + 17, y + 14, hov ? 0xFFFFFFFF : 0xFFCBD5E1);
            ci.cancel();
            return;
        }

        if (label.startsWith("__USER__:")) {
            String uname = label.substring(9);
            drawAmoledBox(context, x, y, w, h, hov);
            int headSize = 12;
            int pad = 5;
            PlayerPreviewRenderer.drawPlayerHead(context, SkinCapeManager.getInstance().getActiveSkinTextures(), x + pad, y + (h - headSize) / 2, headSize);
            int textX = x + pad + headSize + 4;
            String trimmed = mc.textRenderer.trimToWidth(uname, Math.max(12, w - (textX - x) - 4));
            context.drawTextWithShadow(mc.textRenderer, trimmed, textX, y + (h - 8) / 2, 0xFFFFFFFF);
            ci.cancel();
            return;
        }

        if (label.startsWith("__MODICON__:")) {
            String iconKey = label.substring(12);
            drawAmoledBox(context, x, y, w, h, hov);
            int iconCol = hov ? 0xFFFFFFFF : 0xFFE2E8F0;
            int iconSize = 12;
            GlassRenderer.drawVectorIcon(context, x + (w - iconSize) / 2, y + (h - iconSize) / 2, iconSize, iconKey, iconCol);
            ci.cancel();
            return;
        }

        if (label.startsWith("__ICON_")) {
            drawAmoledBox(context, x, y, w, h, hov);
            int icCol = hov ? 0xFFFFFFFF : 0xFFE2E8F0;
            int iconSize = 12;
            int ix = x + (w - iconSize) / 2;
            int iy = y + (h - iconSize) / 2;
            switch (label) {
                case "__ICON_BG__" -> GlassRenderer.drawVectorIcon(context, ix, iy, iconSize, "VISUAL", icCol);
                case "__ICON_HUD__" -> GlassRenderer.drawVectorIcon(context, ix, iy, iconSize, "HUD", icCol);
                case "__ICON_PACKS__" -> GlassRenderer.drawVectorIcon(context, ix, iy, iconSize, "PACKS", icCol);
                case "__ICON_INFO__" -> GlassRenderer.drawVectorIcon(context, ix, iy, iconSize, "INFO", icCol);
                default -> GlassRenderer.drawVectorIcon(context, ix, iy, iconSize, "SETTINGS", icCol);
            }
            ci.cancel();
            return;
        }

        drawAmoledBox(context, x, y, w, h, hov);
        if (!label.isEmpty()) {
            TextRenderer tr = mc.textRenderer;
            String iconKey = resolveMenuButtonIcon(label);
            String smallLabel = GlassRenderer.toSmallCaps(label);
            int txtCol = !this.active ? theme.getTextMuted() : (hov ? 0xFFFFFFFF : 0xFFE2E8F0);

            if (iconKey != null && w >= 54) {
                String trimmed = tr.trimToWidth(smallLabel, w - 24);
                int totalContentW = 10 + 5 + tr.getWidth(trimmed);
                int startX = x + (w - totalContentW) / 2;
                GlassRenderer.drawVectorIcon(context, startX, y + (h - 10) / 2, 10, iconKey, txtCol);
                context.drawTextWithShadow(tr, trimmed, startX + 15, y + (h - 8) / 2, txtCol);
            } else {
                String trimmed = tr.trimToWidth(smallLabel, w - 8);
                int tw = tr.getWidth(trimmed);
                context.drawTextWithShadow(tr, trimmed, x + (w - tw) / 2, y + (h - 8) / 2, txtCol);
            }
        }
        ci.cancel();
    }

    @Unique
    private static String resolveMenuButtonIcon(String rawLabel) {
        String lower = rawLabel.toLowerCase(Locale.ROOT);
        if (lower.contains("singleplayer")) return "SINGLEPLAYER";
        if (lower.contains("multiplayer")) return "MULTIPLAYER";
        if (lower.contains("realms")) return "REALMS";
        if (lower.contains("cosmetic")) return "COSMETICS";
        if (lower.contains("option")) return "OPTIONS";
        if (lower.contains("quit") || lower.contains("disconnect")) return "QUIT";
        if (lower.contains("back to game")) return "BACK_GAME";
        if (lower.contains("advancement")) return "ADVANCEMENTS";
        if (lower.contains("statistic")) return "STATISTICS";
        if (lower.contains("mods")) return "MODS";
        if (lower.contains("hud")) return "HUD";
        if (lower.contains("wardrobe")) return "WARDROBE";
        if (lower.contains("lan")) return "WORLD";
        return null;
    }

    @Unique
    private static void drawAmoledBox(DrawContext context, int x, int y, int w, int h, boolean hov) {
        int bg = hov ? 0xF012141B : 0xE606070A;
        int border = hov ? 0xDDFFFFFF : 0x36FFFFFF;
        context.fill(x + 1, y, x + w - 1, y + h, bg);
        context.fill(x, y + 1, x + 1, y + h - 1, bg);
        context.fill(x + w - 1, y + 1, x + w, y + h - 1, bg);
        context.fill(x + 2, y + 1, x + w - 2, y + 2, hov ? 0x32FFFFFF : 0x16FFFFFF);
        GlassRenderer.drawOutlineBorder(context, x, y, w, h, border);
    }
}
