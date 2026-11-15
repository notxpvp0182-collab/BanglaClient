package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.ClearWaterModule;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Eliminates 100% of underwater fog when ClearWaterModule's Crystal Clear mode is active.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Inject(method = "getSubmersionType", at = @At("RETURN"), cancellable = true, require = 0)
    private void banglaclient$crystalClearUnderwaterNoFog(CallbackInfoReturnable<CameraSubmersionType> cir) {
        if (cir.getReturnValue() == CameraSubmersionType.WATER) {
            BanglaClient bangla = BanglaClient.getInstance();
            if (bangla != null && bangla.getModuleManager() != null) {
                var waterOpt = bangla.getModuleManager().getModule(ClearWaterModule.class);
                if (waterOpt.isPresent() && waterOpt.get().shouldBypassUnderwaterFog()) {
                    cir.setReturnValue(CameraSubmersionType.NONE);
                }
            }
        }
    }
}
