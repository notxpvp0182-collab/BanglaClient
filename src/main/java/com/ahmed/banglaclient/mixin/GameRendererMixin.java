package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.CustomHurtCameraModule;
import com.ahmed.banglaclient.module.visual.ZoomModule;
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
        if (bangla == null || bangla.getModuleManager() == null) {
            return;
        }
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
        if (bangla == null || bangla.getModuleManager() == null) {
            return;
        }
        var hurtCamOpt = bangla.getModuleManager().getModule(CustomHurtCameraModule.class);
        if (hurtCamOpt.isPresent() && hurtCamOpt.get().isEnabled()) {
            if (hurtCamOpt.get().getHurtTiltMultiplier() <= 0.01f) {
                ci.cancel();
            }
        }
    }
}
