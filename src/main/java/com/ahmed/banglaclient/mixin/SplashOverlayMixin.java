package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {

    @Shadow
    private float progress;

    @Unique
    private float smoothedProgress = 0.0f;

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void banglaclient$render2DAuraSplashOverlay(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null) return;
        TextRenderer tr = mc.textRenderer;
        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        int cx = sw / 2;
        int cy = sh / 2;

        float target = MathUtils.clamp(this.progress, 0.02f, 1.0f);
        smoothedProgress = MathUtils.lerp(smoothedProgress, target, 0.22f);

        context.fill(0, 0, sw, sh, 0xFF07080B);

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(cx, cy - 18);
        matrices.scale(2.2f, 2.2f);
        String title = "Bangla Client";
        int topW = tr.getWidth(title);
        context.drawTextWithShadow(tr, title, -topW / 2, 0, 0xFFFFFFFF);
        matrices.popMatrix();

        int barW = Math.min(200, sw - 60);
        int barH = 6;
        int barX = cx - barW / 2;
        int barY = cy + 14;

        GlassRenderer.drawGlassPanel(context, barX, barY, barW, barH, true, false, false);
        int fillW = Math.max(2, Math.round((barW - 2) * smoothedProgress));
        context.fill(barX + 1, barY + 1, barX + 1 + fillW, barY + barH - 1, 0xFFFFFFFF);

        int pct = Math.round(smoothedProgress * 100.0f);
        String statusLine = pct + "%";
        int swText = tr.getWidth(statusLine);
        context.drawTextWithShadow(tr, statusLine, cx - swText / 2, barY + 11, 0xFF94A3B8);
    }
}
