package com.ahmed.banglaclient.mixin;

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

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void banglaclient$renderSolidSplashAndHandOffToMainMenu(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        int cx = sw / 2;
        int cy = sh / 2;

        float target = MathUtils.clamp(this.progress, 0.02f, 1.0f);
        smoothedProgress = MathUtils.lerp(smoothedProgress, target, 0.28f);

        // Always 100% opaque so vanilla Mojang Studios screen underneath NEVER flickers through!
        context.fill(0, 0, sw, sh, 0xFF040507);

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.textRenderer != null) {
            Matrix3x2fStack matrices = context.getMatrices();
            matrices.pushMatrix();
            matrices.translate(cx, cy - 16);
            matrices.scale(3.0f, 3.0f);
            String title = "BANGLA CLIENT";
            int tw = mc.textRenderer.getWidth(title);
            context.drawTextWithShadow(mc.textRenderer, title, -tw / 2, 0, 0xFFFFFFFF);
            matrices.popMatrix();
        } else {
            drawVectorTitle(context, cx - 78, cy - 12, 0xFFFFFFFF);
        }

        int barW = Math.min(340, sw - 48);
        int barH = 6;
        int barX = cx - barW / 2;
        int barY = sh - 34;

        context.fill(barX, barY, barX + barW, barY + barH, 0x33FFFFFF);
        context.fill(barX, barY, barX + barW, barY + 1, 0x66FFFFFF);
        context.fill(barX, barY + barH - 1, barX + barW, barY + barH, 0x66FFFFFF);

        int fillW = Math.max(4, Math.round((barW - 2) * smoothedProgress));
        context.fill(barX + 1, barY + 1, barX + 1 + fillW, barY + barH - 1, 0xFFFFFFFF);

        // As soon as resource reload finishes, immediately close SplashOverlay so TitleScreenMixin's smooth entrance animation takes over with zero Mojang glitch!
        if (this.reloadCompleteTime > -1L && mc != null) {
            mc.setOverlay(null);
        }
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
