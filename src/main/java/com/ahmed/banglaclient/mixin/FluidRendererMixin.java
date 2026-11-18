package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.ClearWaterModule;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes water crystal-clear when viewed FROM ABOVE the water surface by brightening water vertices
 * and maximizing underwater lightmap transmission.
 */
@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {

    @Inject(
            method = "vertex(Lnet/minecraft/client/render/VertexConsumer;FFFFFFFFI)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void banglaclient$makeWaterCrystalClearFromAbove(
            VertexConsumer consumer,
            float x, float y, float z,
            float red, float green, float blue,
            float u, float v,
            int light,
            CallbackInfo ci
    ) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return;

        var waterOpt = bangla.getModuleManager().getModule(ClearWaterModule.class);
        if (waterOpt.isPresent() && waterOpt.get().shouldEnhanceSurfaceClarity()) {
            float clarity = waterOpt.get().getSurfaceClarityFactor(); // 0.1f to 1.0f
            // Boost water vertex brightness & reduce murky blue/green absorption when looking from above!
            float clearR = Math.min(1.0f, red + (1.0f - red) * 0.45f * clarity);
            float clearG = Math.min(1.0f, green + (1.0f - green) * 0.55f * clarity);
            float clearB = Math.min(1.0f, blue + (1.0f - blue) * 0.60f * clarity);
            int fullLight = 0xF000F0;
            int blendedLight = clarity >= 0.5f ? fullLight : light;
            int alpha = Math.max(45, Math.round(215 - (clarity * 145.0f)));

            consumer.vertex(x, y, z)
                    .color((int) (clearR * 255), (int) (clearG * 255), (int) (clearB * 255), alpha)
                    .texture(u, v)
                    .light(blendedLight)
                    .normal(0.0f, 1.0f, 0.0f);
            ci.cancel();
        }
    }
}
