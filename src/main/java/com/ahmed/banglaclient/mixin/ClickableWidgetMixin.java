package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
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
    @Shadow public abstract boolean isHovered();
    @Shadow public boolean active;
    @Shadow public boolean visible;

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void banglaclient$applyLiquidGlassToVanillaButtons(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!this.visible || !((Object) this instanceof PressableWidget)) return;
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getThemeManager() == null) return;

        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        if (w < 12 || h < 10) return;

        int accent = bangla.getThemeManager().getEffectiveAccentPrimary();
        boolean hov = isHovered() && this.active;

        int topTint = hov ? ColorUtils.withAlpha(accent, 0.24f) : 0x22FFFFFF;
        int botTint = hov ? ColorUtils.withAlpha(accent, 0.10f) : 0x08000000;
        context.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, topTint, botTint);

        int borderCol = hov ? ColorUtils.withAlpha(accent, 0.85f) : 0x44FFFFFF;
        GlassRenderer.drawOutlineBorder(context, x, y, w, h, borderCol);
    }
}
