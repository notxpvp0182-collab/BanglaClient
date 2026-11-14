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
 * Renders all Minecraft buttons in Pure 2D Aura Dark & White Glass (Zero 3D shading, Zero lag).
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

        // 1. Bottom-Right Bangla SMP 2D Aura Card
        if ("__BANGLA_SMP__".equals(label)) {
            draw2DAuraBox(context, x, y, w, h, hov);
            context.drawTextWithShadow(mc.textRenderer, "BANGLA SMP", x + 8, y + 4, 0xFFFFFFFF);
            context.drawTextWithShadow(mc.textRenderer, "play.banglasmp.net", x + 8, y + 14, theme.getTextSecondary());
            ci.cancel();
            return;
        }

        // 2. Top-Right Account Pill: [HEAD] [NAME]
        if (label.startsWith("__USER__:")) {
            String uname = label.substring(9);
            draw2DAuraBox(context, x, y, w, h, hov);
            int headSize = 12;
            int pad = 4;
            PlayerPreviewRenderer.drawPlayerHead(context, SkinCapeManager.getInstance().getActiveSkinTextures(), x + pad, y + (h - headSize) / 2, headSize);
            int textX = x + pad + headSize + pad;
            String trimmed = mc.textRenderer.trimToWidth(uname, Math.max(12, w - (textX - x) - pad));
            context.drawTextWithShadow(mc.textRenderer, trimmed, textX, y + (h - 8) / 2, 0xFFFFFFFF);
            ci.cancel();
            return;
        }

        // 3. Left-Side Auto-Detected Mod Buttons with Custom 2D Vector Icons
        if (label.startsWith("__MODBTN__:")) {
            String payload = label.substring(11);
            int sep = payload.indexOf(':');
            String iconKey = sep > 0 ? payload.substring(0, sep) : "UTILITY";
            String modTitle = sep > 0 ? payload.substring(sep + 1) : payload;

            draw2DAuraBox(context, x, y, w, h, hov);
            int iconCol = hov ? 0xFFFFFFFF : theme.getTextSecondary();
            GlassRenderer.drawVectorIcon(context, x + 5, y + (h - 10) / 2, 10, iconKey, iconCol);

            if (w > 28) {
                String trimmed = mc.textRenderer.trimToWidth(modTitle, w - 22);
                context.drawTextWithShadow(mc.textRenderer, trimmed, x + 19, y + (h - 8) / 2, hov ? 0xFFFFFFFF : theme.getTextSecondary());
            }
            ci.cancel();
            return;
        }

        // 4. Top-Right 2D Aura Toolbar Icons
        if (label.startsWith("__ICON_")) {
            draw2DAuraBox(context, x, y, w, h, hov);
            int icCol = hov ? 0xFFFFFFFF : theme.getTextSecondary();
            int ix = x + (w - 10) / 2;
            int iy = y + (h - 10) / 2;
            switch (label) {
                case "__ICON_HUD__" -> GlassRenderer.drawVectorIcon(context, ix, iy, 10, "HUD", icCol);
                case "__ICON_PACKS__" -> GlassRenderer.drawVectorIcon(context, ix, iy, 10, "PACKS", icCol);
                case "__ICON_INFO__" -> GlassRenderer.drawVectorIcon(context, ix, iy, 10, "INFO", icCol);
                default -> GlassRenderer.drawVectorIcon(context, ix, iy, 10, "SETTINGS", icCol);
            }
            ci.cancel();
            return;
        }

        // 5. All Standard Minecraft & Client Buttons (Pure 2D Aura Dark & White)
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
        int bg = hov ? 0xE6181A20 : 0xD80E1014;
        int border = hov ? 0x99FFFFFF : 0x28FFFFFF;
        context.fill(x + 1, y, x + w - 1, y + h, bg);
        context.fill(x, y + 1, x + 1, y + h - 1, bg);
        context.fill(x + w - 1, y + 1, x + w, y + h - 1, bg);
        GlassRenderer.drawOutlineBorder(context, x, y, w, h, border);
    }
}
