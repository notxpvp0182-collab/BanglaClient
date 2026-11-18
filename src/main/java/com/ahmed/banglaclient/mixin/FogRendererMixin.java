package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.ClearLavaModule;
import com.ahmed.banglaclient.module.visual.ClearWaterModule;
import com.ahmed.banglaclient.module.visual.CustomFogModule;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks Minecraft 1.21.11's new net.minecraft.client.render.fog.FogRenderer class
 * for Clear Water, Clear Lava, and Custom/No Fog.
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Inject(
            method = "getCameraSubmersionType(Lnet/minecraft/client/render/Camera;Z)Lnet/minecraft/block/enums/CameraSubmersionType;",
            at = @At("RETURN"),
            cancellable = true,
            require = 0
    )
    private void banglaclient$bypassWaterAndLavaSubmersionFog(Camera camera, boolean thick, CallbackInfoReturnable<CameraSubmersionType> cir) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return;

        CameraSubmersionType type = cir.getReturnValue();
        if (type == CameraSubmersionType.WATER) {
            var waterOpt = bangla.getModuleManager().getModule(ClearWaterModule.class);
            if (waterOpt.isPresent() && waterOpt.get().shouldBypassUnderwaterFog()) {
                cir.setReturnValue(CameraSubmersionType.NONE);
            }
        } else if (type == CameraSubmersionType.LAVA) {
            var lavaOpt = bangla.getModuleManager().getModule(ClearLavaModule.class);
            if (lavaOpt.isPresent() && lavaOpt.get().shouldBypassLavaFog()) {
                cir.setReturnValue(CameraSubmersionType.NONE);
            }
        }
    }

    @ModifyVariable(
            method = "applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true,
            require = 0
    )
    private float banglaclient$pushEnvironmentalFogStart(float environmentalStart) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var fogOpt = bangla.getModuleManager().getModule(CustomFogModule.class);
            if (fogOpt.isPresent() && fogOpt.get().shouldDisableTerrainFog()) {
                return 10000.0f;
            }
            var waterOpt = bangla.getModuleManager().getModule(ClearWaterModule.class);
            if (waterOpt.isPresent() && waterOpt.get().isEnabled()) {
                return Math.max(environmentalStart, 256.0f);
            }
        }
        return environmentalStart;
    }

    @ModifyVariable(
            method = "applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true,
            require = 0
    )
    private float banglaclient$pushEnvironmentalFogEnd(float environmentalEnd) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var fogOpt = bangla.getModuleManager().getModule(CustomFogModule.class);
            if (fogOpt.isPresent() && fogOpt.get().shouldDisableTerrainFog()) {
                return 20000.0f;
            }
            var waterOpt = bangla.getModuleManager().getModule(ClearWaterModule.class);
            if (waterOpt.isPresent() && waterOpt.get().isEnabled()) {
                return Math.max(environmentalEnd * waterOpt.get().getUnderwaterFogMultiplier(), 512.0f);
            }
        }
        return environmentalEnd;
    }
}
