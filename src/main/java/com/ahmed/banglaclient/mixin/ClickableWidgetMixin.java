package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.cosmetics.PlayerPreviewRenderer;
import com.ahmed.banglaclient.cosmetics.SkinCapeManager;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
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

/**
 * Renders all Minecraft buttons in Crisp 2D Aura Dark & White Glass.
 */
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
    private void banglaclient$render2DAuraButton(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!this.visible || !((Object) this instanceof PressableWidget)) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null) return;

        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        if (w < 10 || h < 10) return;

        Theme theme = GlassRenderer.getTheme();
        boolean hov = this.active && MathUtils.isHovered(mouseX, mouseY, x, y, w, h);
        String label = getMessage() != null ? getMessage().getString() : "";

        // 1. Bottom-Right Bangla SMP Card
        if ("__BANGLA_SMP__".equals(label)) {
            draw2DAuraBox(context, x, y, w, h, hov);
            int t1 = mc.textRenderer.getWidth("BANGLA SMP");
            int t2 = mc.textRenderer.getWidth("play.banglasmp.net");
            context.drawTextWithShadow(mc.textRenderer, "BANGLA SMP", x + (w - t1) / 2, y + 4, 0xFFFFFFFF);
            context.drawTextWithShadow(mc.textRenderer, "play.banglasmp.net", x + (w - t2) / 2, y + 14, theme.getTextSecondary());
            ci.cancel();
            return;
        }

        // 2. Top-Right Account Pill: [HEAD] [NAME]
        if (label.startsWith("__USER__:")) {
            String uname = label.substring(9);
            draw2DAuraBox(context, x, y, w, h, hov);
            int headSize = 12;
            int pad = 5;
            PlayerPreviewRenderer.drawPlayerHead(context, SkinCapeManager.getInstance().getActiveSkinTextures(), x + pad, y + (h - headSize) / 2, headSize);
            int textX = x + pad + headSize + 4;
            String trimmed = mc.textRenderer.trimToWidth(uname, Math.max(12, w - (textX - x) - 4));
            context.drawTextWithShadow(mc.textRenderer, trimmed, textX, y + (h - 8) / 2, 0xFFFFFFFF);
            ci.cancel();
            return;
        }

        // 3. Left-Side Icon-Only Mod Dock Buttons (__MODICON__:ICONKEY)
        if (label.startsWith("__MODICON__:")) {
            String iconKey = label.substring(12);
            draw2DAuraBox(context, x, y, w, h, hov);
            int iconCol = hov ? 0xFFFFFFFF : 0xFFE2E8F0;
            int iconSize = 12;
            GlassRenderer.drawVectorIcon(context, x + (w - iconSize) / 2, y + (h - iconSize) / 2, iconSize, iconKey, iconCol);
            ci.cancel();
            return;
        }

        // 4. Top-Right 2D Aura Toolbar Icons (Larger & Clearer 12x12 Icons)
        if (label.startsWith("__ICON_")) {
            draw2DAuraBox(context, x, y, w, h, hov);
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

        // 5. Standard Center & Menu Buttons (Premium 2D Aura Dark & White)
        draw2DAuraBox(context, x, y, w, h, hov);
        if (!label.isEmpty()) {
            TextRenderer tr = mc.textRenderer;
            String trimmed = tr.trimToWidth(label, w - 8);
            int tw = tr.getWidth(trimmed);
            int txtCol = !this.active ? theme.getTextMuted() : (hov ? 0xFFFFFFFF : 0xFFE2E8F0);
            context.drawTextWithShadow(tr, trimmed, x + (w - tw) / 2, y + (h - 8) / 2, txtCol);
        }
        ci.cancel();
    }

    @Unique
    private static void draw2DAuraBox(DrawContext context, int x, int y, int w, int h, boolean hov) {
        int bg = hov ? 0xEA181B22 : 0xDC0D0F14;
        int border = hov ? 0xCCFFFFFF : 0x36FFFFFF;
        context.fill(x + 1, y, x + w - 1, y + h, bg);
        context.fill(x, y + 1, x + 1, y + h - 1, bg);
        context.fill(x + w - 1, y + 1, x + w, y + h - 1, bg);
        if (hov) {
            context.fill(x + 2, y + 1, x + w - 2, y + 2, 0x28FFFFFF);
        }
        GlassRenderer.drawOutlineBorder(context, x, y, w, h, border);
    }
}
