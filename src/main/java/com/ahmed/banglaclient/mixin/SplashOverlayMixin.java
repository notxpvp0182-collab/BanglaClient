package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.util.ColorUtils;
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
    private void banglaclient$renderPremiumSplashOverlay(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null) return;
        TextRenderer tr = mc.textRenderer;
        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        int cx = sw / 2;
        int cy = sh / 2;

        float target = MathUtils.clamp(this.progress, 0.02f, 1.0f);
        smoothedProgress = MathUtils.lerp(smoothedProgress, target, 0.18f);

        // Full-screen dark obsidian-crimson gradient backdrop
        context.fillGradient(0, 0, sw, sh, 0xFF07090E, 0xFF140C12);

        int accent = 0xFFEF4444;
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null) {
            accent = BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary();
        }

        // Golden Crown above "Bangla"
        int crownX = cx - 48, crownY = cy - 52;
        context.fill(crownX + 2, crownY, crownX + 5, crownY + 3, 0xFFF59E0B);
        context.fill(crownX + 9, crownY - 3, crownX + 12, crownY + 3, 0xFFF59E0B);
        context.fill(crownX + 16, crownY, crownX + 19, crownY + 3, 0xFFF59E0B);
        context.fill(crownX, crownY + 3, crownX + 21, crownY + 8, 0xFFFBBF24);

        Matrix3x2fStack matrices = context.getMatrices();

        // 3D Extruded Red "Bangla"
        matrices.pushMatrix();
        matrices.translate(cx, cy - 38);
        matrices.scale(3.2f, 3.2f);
        String top = "Bangla";
        int topW = tr.getWidth(top);
        context.drawText(tr, top, -topW / 2 + 2, 2, 0xFF350606, false);
        context.drawText(tr, top, -topW / 2 + 1, 1, 0xFF991B1B, false);
        context.drawText(tr, top, -topW / 2, 0, 0xFFEF4444, false);
        matrices.popMatrix();

        // 3D Extruded White "Client"
        matrices.pushMatrix();
        matrices.translate(cx, cy - 6);
        matrices.scale(2.1f, 2.1f);
        String sub = "Client";
        int subW = tr.getWidth(sub);
        context.drawText(tr, sub, -subW / 2 + 1, 1, 0xFF334155, false);
        context.drawText(tr, sub, -subW / 2, 0, 0xFFFFFFFF, false);
        matrices.popMatrix();

        // Liquid Glass Progress Bar Container
        int barW = Math.min(230, sw - 60);
        int barH = 10;
        int barX = cx - barW / 2;
        int barY = cy + 28;

        GlassRenderer.drawGlassPanel(context, barX, barY, barW, barH, true, false, true);
        int fillW = Math.max(4, Math.round((barW - 4) * smoothedProgress));
        context.fillGradient(barX + 2, barY + 2, barX + 2 + fillW, barY + barH - 2,
                ColorUtils.scaleBrightness(accent, 1.2f), accent);

        int pct = Math.round(smoothedProgress * 100.0f);
        String stage = pct < 35
                ? "Initializing Core Systems..."
                : (pct < 75 ? "Loading Textures & Shaders..." : (pct < 98 ? "Preparing BANGLA CLIENT Modules..." : "Ready!"));
        String statusLine = stage + " (" + pct + "%)";
        int swText = tr.getWidth(statusLine);
        context.drawTextWithShadow(tr, statusLine, cx - swText / 2, barY + 15, 0xFFD0D7E2);
    }
}
