package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.CustomSkyModule;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true, require = 0)
    private void banglaclient$applyCustomSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Integer> cir) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var skyOpt = bangla.getModuleManager().getModule(CustomSkyModule.class);
            if (skyOpt.isPresent() && skyOpt.get().isCustomSkyColorActive()) {
                cir.setReturnValue(skyOpt.get().getSkyColorRgb());
            }
        }
    }
}
