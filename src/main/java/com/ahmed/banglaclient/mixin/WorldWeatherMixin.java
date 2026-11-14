package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.CustomSkyModule;
import com.ahmed.banglaclient.module.visual.CustomWeatherModule;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldWeatherMixin {

    @Shadow public abstract boolean isClient();

    @Inject(method = "getRainGradient", at = @At("RETURN"), cancellable = true, require = 0)
    private void banglaclient$overrideRainGradient(float delta, CallbackInfoReturnable<Float> cir) {
        if (!this.isClient()) return;
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var weatherOpt = bangla.getModuleManager().getModule(CustomWeatherModule.class);
            if (weatherOpt.isPresent() && weatherOpt.get().isEnabled()) {
                cir.setReturnValue(weatherOpt.get().getOverrideRainGradient(cir.getReturnValue()));
            }
        }
    }

    @Inject(method = "getThunderGradient", at = @At("RETURN"), cancellable = true, require = 0)
    private void banglaclient$overrideThunderGradient(float delta, CallbackInfoReturnable<Float> cir) {
        if (!this.isClient()) return;
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var weatherOpt = bangla.getModuleManager().getModule(CustomWeatherModule.class);
            if (weatherOpt.isPresent() && weatherOpt.get().isEnabled()) {
                cir.setReturnValue(weatherOpt.get().getOverrideThunderGradient(cir.getReturnValue()));
            }
        }
    }

    @Inject(method = "getTimeOfDay", at = @At("RETURN"), cancellable = true, require = 0)
    private void banglaclient$overrideClientVisualTime(CallbackInfoReturnable<Long> cir) {
        if (!this.isClient()) return;
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var skyOpt = bangla.getModuleManager().getModule(CustomSkyModule.class);
            if (skyOpt.isPresent() && skyOpt.get().isVisualTimeLocked()) {
                cir.setReturnValue(skyOpt.get().getVisualTimeTicks());
            }
        }
    }
}
