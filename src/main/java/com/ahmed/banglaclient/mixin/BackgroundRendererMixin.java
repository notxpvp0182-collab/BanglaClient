package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.ClearWaterModule;
import com.ahmed.banglaclient.module.visual.CustomFogModule;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Connects `CustomFogModule` and `ClearWaterModule` to Minecraft 1.21.11's `BackgroundRenderer.applyFog`
 * while strictly respecting server-side Blindness and Darkness status effects.
 */
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

    @Inject(method = "applyFog", at = @At("RETURN"), cancellable = true)
    private static void banglaclient$customizeFogParameters(
            Camera camera,
            BackgroundRenderer.FogType fogType,
            Vector4f color,
            float viewDistance,
            boolean thickFog,
            float tickDelta,
            CallbackInfoReturnable<Fog> cir
    ) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null || camera == null) {
            return;
        }

        // Never modify fog if the player has Blindness or Darkness
        Entity focused = camera.getFocusedEntity();
        if (focused instanceof LivingEntity living) {
            if (living.hasStatusEffect(StatusEffects.BLINDNESS) || living.hasStatusEffect(StatusEffects.DARKNESS)) {
                return;
            }
        }

        Fog original = cir.getReturnValue();
        if (original == null) {
            return;
        }

        CameraSubmersionType submersion = camera.getSubmersionType();
        if (submersion == CameraSubmersionType.WATER) {
            var clearWaterOpt = bangla.getModuleManager().getModule(ClearWaterModule.class);
            if (clearWaterOpt.isPresent() && clearWaterOpt.get().isEnabled()) {
                float mult = clearWaterOpt.get().getUnderwaterFogMultiplier();
                cir.setReturnValue(new Fog(
                        original.start(),
                        original.end() * mult,
                        original.shape(),
                        original.red(),
                        original.green(),
                        original.blue(),
                        original.alpha()
                ));
            }
            return;
        }

        if (submersion == CameraSubmersionType.NONE) {
            var fogModOpt = bangla.getModuleManager().getModule(CustomFogModule.class);
            if (fogModOpt.isPresent() && fogModOpt.get().isEnabled()) {
                CustomFogModule fogMod = fogModOpt.get();
                float newStart = original.start() * fogMod.getFogStartMultiplier();
                float newEnd = Math.max(newStart + 4.0f, original.end() * fogMod.getFogEndMultiplier());

                float r = original.red();
                float g = original.green();
                float b = original.blue();
                if (fogMod.isCustomFogColorEnabled()) {
                    int rgb = fogMod.getCustomFogColorRgb();
                    r = ((rgb >>> 16) & 0xFF) / 255.0f;
                    g = ((rgb >>> 8) & 0xFF) / 255.0f;
                    b = (rgb & 0xFF) / 255.0f;
                }

                cir.setReturnValue(new Fog(
                        newStart,
                        newEnd,
                        original.shape(),
                        r,
                        g,
                        b,
                        original.alpha()
                ));
            }
        }
    }
}
