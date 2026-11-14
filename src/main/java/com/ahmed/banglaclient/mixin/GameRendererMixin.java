package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.ClearWaterModule;
import com.ahmed.banglaclient.module.visual.CustomFogModule;
import com.ahmed.banglaclient.module.visual.CustomHurtCameraModule;
import com.ahmed.banglaclient.module.visual.ZoomModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void banglaclient$applySmoothZoomFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return;
        var zoomOpt = bangla.getModuleManager().getModule(ZoomModule.class);
        if (zoomOpt.isPresent()) {
            float divisor = zoomOpt.get().getFovDivisor();
            if (divisor > 1.001f) {
                cir.setReturnValue(cir.getReturnValue() / divisor);
            }
        }
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true, require = 0)
    private void banglaclient$controlHurtCameraTilt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return;
        var hurtCamOpt = bangla.getModuleManager().getModule(CustomHurtCameraModule.class);
        if (hurtCamOpt.isPresent() && hurtCamOpt.get().isEnabled()) {
            float mult = hurtCamOpt.get().getHurtTiltMultiplier(); if (mult <= 0.01f) { ci.cancel(); } else if (Math.abs(mult - 1.0f) > 0.01f) { matrices.scale(mult, mult, 1.0f); } if (false) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "getViewDistance", at = @At("RETURN"), cancellable = true, require = 0)
    private void banglaclient$applyCustomFogAndWaterDistance(CallbackInfoReturnable<Float> cir) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return;

        float dist = cir.getReturnValue();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.player != null && mc.player.isSubmergedInWater()) {
            var waterOpt = bangla.getModuleManager().getModule(ClearWaterModule.class);
            if (waterOpt.isPresent() && waterOpt.get().isEnabled()) {
                dist *= waterOpt.get().getUnderwaterFogMultiplier();
            }
        }
        var fogOpt = bangla.getModuleManager().getModule(CustomFogModule.class);
        if (fogOpt.isPresent() && fogOpt.get().isEnabled()) {
            dist *= fogOpt.get().getFogEndMultiplier();
        }
        cir.setReturnValue(dist);
    }
}
