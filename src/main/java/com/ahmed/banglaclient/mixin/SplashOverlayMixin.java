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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {

    @Shadow
    private float progress;

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void banglaclient$renderBanglaSplashLogo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null) return;
        TextRenderer tr = mc.textRenderer;
        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        int cx = sw / 2;
        int cy = sh / 2;

        // 1. Cover the ENTIRE screen so "MOJANG STUDIOS" and the vanilla bar are 100% replaced
        context.fillGradient(0, 0, sw, sh, 0xFF080A0F, 0xFF12151E);

        int accent = 0xFFEF4444;
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null) {
            accent = BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary();
        }

        Matrix3x2fStack matrices = context.getMatrices();

        // 2. Large 3D Extruded "Bangla" (Smooth Premium Red, Capital B + lowercase)
        matrices.pushMatrix();
        matrices.translate(cx, cy - 34);
        matrices.scale(3.2f, 3.2f);
        String titleTop = "Bangla";
        int topW = tr.getWidth(titleTop);
        context.drawText(tr, titleTop, -topW / 2 + 2, 2, 0xFF450A0A, false);
        context.drawText(tr, titleTop, -topW / 2 + 1, 1, 0xFF991B1B, false);
        context.drawText(tr, titleTop, -topW / 2, 0, 0xFFEF4444, false);
        matrices.popMatrix();

        // 3. 3D Extruded "Client" Positioned Directly Below "Bangla" (Clean Bold White)
        matrices.pushMatrix();
        matrices.translate(cx, cy - 2);
        matrices.scale(1.8f, 1.8f);
        String titleSub = "Client";
        int subW = tr.getWidth(titleSub);
        context.drawText(tr, titleSub, -subW / 2 + 1, 1, 0xFF334155, false);
        context.drawText(tr, titleSub, -subW / 2, 0, 0xFFFFFFFF, false);
        matrices.popMatrix();

        // 4. Custom Liquid Glass Progress Bar Below the Logo
        int barW = Math.min(220, sw - 60);
        int barH = 10;
        int barX = cx - barW / 2;
        int barY = cy + 32;

        GlassRenderer.drawGlassPanel(context, barX, barY, barW, barH, true, false, true);
        float clampedProgress = MathUtils.clamp(this.progress, 0.02f, 1.0f);
        int fillW = Math.max(4, Math.round((barW - 4) * clampedProgress));
        context.fillGradient(barX + 2, barY + 2, barX + 2 + fillW, barY + barH - 2,
                ColorUtils.scaleBrightness(accent, 1.15f), accent);

        String pctText = Math.round(clampedProgress * 100.0f) + "%";
        int pw = tr.getWidth(pctText);
        context.drawTextWithShadow(tr, pctText, cx - pw / 2, barY + 14, 0xFFA5AFBC);
    }
}
