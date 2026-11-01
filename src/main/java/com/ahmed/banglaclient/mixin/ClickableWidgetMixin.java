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
        if (w < 12 || h < 10) return;

        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : 0xFFEF4444;

        boolean hov = this.active && MathUtils.isHovered(mouseX, mouseY, x, y, w, h);
        String label = getMessage() != null ? getMessage().getString() : "";
        boolean isPrimaryAccentBtn = label.contains("Quit") || label.contains("Click GUI") || label.contains("Play Selected") || label.contains("Join Server");

        if (isPrimaryAccentBtn) {
            int fill = hov ? ColorUtils.withAlpha(accent, 0.72f) : ColorUtils.withAlpha(accent, 0.45f);
            context.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, ColorUtils.scaleBrightness(fill, 1.15f), fill);
            GlassRenderer.drawOutlineBorder(context, x, y, w, h, ColorUtils.withAlpha(0xFFFFFFFF, hov ? 0.75f : 0.35f));
        } else {
            GlassRenderer.drawGlassPanel(context, x, y, w, h, true, hov, hov);
        }

        if (!label.isEmpty()) {
            TextRenderer tr = mc.textRenderer;
            String trimmed = tr.trimToWidth(label, w - 8);
            int tw = tr.getWidth(trimmed);
            int txtCol = !this.active ? theme.getTextMuted() : (hov ? 0xFFFFFFFF : theme.getTextPrimary());
            context.drawTextWithShadow(tr, trimmed, x + (w - tw) / 2, y + (h - 8) / 2, txtCol);
        }
        ci.cancel();
    }
}
