package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.MinecraftClient;
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

    @Shadow private float progress;
    @Shadow private long reloadCompleteTime;
    @Unique private float smoothedProgress = 0.0f;
    @Unique private long fadeOutStartMs = -1L;

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void banglaclient$renderSmoothTransitionSplashOverlay(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        int cx = sw / 2;
        int cy = sh / 2;

        float target = MathUtils.clamp(this.progress, 0.02f, 1.0f);
        smoothedProgress = MathUtils.lerp(smoothedProgress, target, 0.25f);

        // Calculate smooth fade-out progress once loading completes
        float fadeOut = 0.0f;
        if (this.reloadCompleteTime > -1L || smoothedProgress >= 0.992f) {
            if (fadeOutStartMs < 0L) {
                fadeOutStartMs = System.currentTimeMillis();
            }
            float rawFade = MathUtils.clamp((System.currentTimeMillis() - fadeOutStartMs) / 650.0f, 0.0f, 1.0f);
            fadeOut = Easing.EASE_IN_OUT_CUBIC.apply(rawFade);
        } else {
            fadeOutStartMs = -1L;
        }

        float opacity = 1.0f - fadeOut;
        if (opacity <= 0.01f) {
            return;
        }

        int bgAlpha = Math.round(255.0f * opacity);
        context.fill(0, 0, sw, sh, (bgAlpha << 24) | 0x040507);

        int textAlpha = Math.max(5, Math.round(255.0f * opacity));
        int textColor = (textAlpha << 24) | 0xFFFFFF;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.textRenderer != null) {
            float titleScale = 3.0f + (0.38f * fadeOut);
            float titleSlideY = -16.0f - (10.0f * fadeOut);
            Matrix3x2fStack matrices = context.getMatrices();
            matrices.pushMatrix();
            matrices.translate(cx, cy + titleSlideY);
            matrices.scale(titleScale, titleScale);
            String title = "BANGLA CLIENT";
            int tw = mc.textRenderer.getWidth(title);
            context.drawTextWithShadow(mc.textRenderer, title, -tw / 2, 0, textColor);
            matrices.popMatrix();
        } else {
            drawVectorTitle(context, cx - 78, cy - 12, textColor);
        }

        // Bottom loading bar smoothly slides down as it fades out
        int barW = Math.min(340, sw - 48);
        int barH = 6;
        int barX = cx - barW / 2;
        int barY = sh - 34 + Math.round(18.0f * fadeOut);

        int trackAlpha = Math.round(52.0f * opacity);
        int borderAlpha = Math.round(105.0f * opacity);
        context.fill(barX, barY, barX + barW, barY + barH, (trackAlpha << 24) | 0xFFFFFF);
        context.fill(barX, barY, barX + barW, barY + 1, (borderAlpha << 24) | 0xFFFFFF);
        context.fill(barX, barY + barH - 1, barX + barW, barY + barH, (borderAlpha << 24) | 0xFFFFFF);

        int fillW = Math.max(4, Math.round((barW - 2) * smoothedProgress));
        context.fill(barX + 1, barY + 1, barX + 1 + fillW, barY + barH - 1, textColor);
    }

    @Unique
    private static void drawVectorTitle(DrawContext c, int x, int y, int col) {
        for (int i = 0; i < 12; i++) {
            int lx = x + i * 13;
            c.fill(lx, y, lx + 9, y + 3, col);
            c.fill(lx, y + 7, lx + 8, y + 10, col);
            c.fill(lx, y + 15, lx + 9, y + 18, col);
            c.fill(lx, y, lx + 3, y + 18, col);
        }
    }
}
