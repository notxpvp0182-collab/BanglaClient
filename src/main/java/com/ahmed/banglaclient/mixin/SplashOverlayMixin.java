package com.ahmed.banglaclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void banglaclient$renderBanglaSplashLogo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null) return;
        TextRenderer tr = mc.textRenderer;
        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        int cx = sw / 2;
        int cy = sh / 2;

        // Cover the center Mojang Studios text region while keeping the bottom progress bar intact
        context.fill(0, 0, sw, Math.max(20, cy + 18), 0xFF080A0F);

        Matrix3x2fStack matrices = context.getMatrices();
        // 1. 3D Extruded Red "বাংলা" Logo
        matrices.pushMatrix();
        matrices.translate(cx, cy - 22);
        matrices.scale(3.0f, 3.0f);
        String bn = "বাংলা";
        int bnw = tr.getWidth(bn);
        context.drawText(tr, bn, -bnw / 2 + 2, 2, 0xFF450A0A, false);
        context.drawText(tr, bn, -bnw / 2 + 1, 1, 0xFF991B1B, false);
        context.drawText(tr, bn, -bnw / 2, 0, 0xFFEF4444, false);
        matrices.popMatrix();

        // 2. 3D Extruded White "CLIENT" Subtitle
        matrices.pushMatrix();
        matrices.translate(cx, cy + 4);
        matrices.scale(1.5f, 1.5f);
        String cl = "C L I E N T";
        int clw = tr.getWidth(cl);
        context.drawText(tr, cl, -clw / 2 + 1, 1, 0xFF334155, false);
        context.drawText(tr, cl, -clw / 2, 0, 0xFFFFFFFF, false);
        matrices.popMatrix();
    }
}
