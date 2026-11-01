package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.CustomSkyModule;
import com.ahmed.banglaclient.module.visual.CustomWeatherModule;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Connects `CustomSkyModule` and `CustomWeatherModule` to `ClientWorld` for client-side visual
 * sky color, visual time-of-day, and precipitation overrides without mutating server world state.
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void banglaclient$overrideSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Integer> cir) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var skyOpt = bangla.getModuleManager().getModule(CustomSkyModule.class);
            if (skyOpt.isPresent() && skyOpt.get().isCustomSkyColorActive()) {
                cir.setReturnValue(skyOpt.get().getSkyColorRgb());
            }
        }
    }

    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void banglaclient$overrideVisualTimeOfDay(CallbackInfoReturnable<Long> cir) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var skyOpt = bangla.getModuleManager().getModule(CustomSkyModule.class);
            if (skyOpt.isPresent() && skyOpt.get().isVisualTimeLocked()) {
                cir.setReturnValue(skyOpt.get().getVisualTimeTicks());
            }
        }
    }

    /**
     * Hooks `World.getRainGradient` inherited by `ClientWorld` to apply client-side visual weather presets.
     */
    @Mixin(World.class)
    public static abstract class WorldWeatherHookMixin {

        @Inject(method = "getRainGradient", at = @At("RETURN"), cancellable = true)
        private void banglaclient$overrideRainGradient(float delta, CallbackInfoReturnable<Float> cir) {
            if (!((Object) this instanceof ClientWorld)) {
                return;
            }
            BanglaClient bangla = BanglaClient.getInstance();
            if (bangla != null && bangla.getModuleManager() != null) {
                var weatherOpt = bangla.getModuleManager().getModule(CustomWeatherModule.class);
                if (weatherOpt.isPresent() && weatherOpt.get().isEnabled()) {
                    cir.setReturnValue(weatherOpt.get().getOverrideRainGradient(cir.getReturnValue()));
                }
            }
        }

        @Inject(method = "getThunderGradient", at = @At("RETURN"), cancellable = true)
        private void banglaclient$overrideThunderGradient(float delta, CallbackInfoReturnable<Float> cir) {
            if (!((Object) this instanceof ClientWorld)) {
                return;
            }
            BanglaClient bangla = BanglaClient.getInstance();
            if (bangla != null && bangla.getModuleManager() != null) {
                var weatherOpt = bangla.getModuleManager().getModule(CustomWeatherModule.class);
                if (weatherOpt.isPresent() && weatherOpt.get().isEnabled()) {
                    cir.setReturnValue(weatherOpt.get().getOverrideThunderGradient(cir.getReturnValue()));
                }
            }
        }
    }
}
