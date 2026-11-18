package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.CustomFireOverlayModule;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Directly scales Ground Fire (FireBlock & SoulFireBlock) height in the 3D world mesh!
 */
@Mixin(BlockRenderManager.class)
public abstract class BlockRenderManagerMixin {

    @Inject(
            method = "renderBlock",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void banglaclient$scaleGroundAndSoulFireHead(
            BlockState state,
            BlockPos pos,
            BlockRenderView world,
            MatrixStack matrices,
            VertexConsumer vertexConsumer,
            boolean cull,
            Random random,
            CallbackInfo ci
    ) {
        if (state == null || !(state.getBlock() instanceof AbstractFireBlock)) return;
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return;

        var fireOpt = bangla.getModuleManager().getModule(CustomFireOverlayModule.class);
        if (fireOpt.isPresent() && fireOpt.get().shouldScaleGroundFire()) {
            float hScale = fireOpt.get().getGroundFireHeightScale();
            if (hScale <= 0.05f) {
                ci.cancel();
                return;
            }
            matrices.push();
            matrices.scale(1.0f, hScale, 1.0f);
        }
    }

    @Inject(
            method = "renderBlock",
            at = @At("RETURN"),
            require = 0
    )
    private void banglaclient$scaleGroundAndSoulFireReturn(
            BlockState state,
            BlockPos pos,
            BlockRenderView world,
            MatrixStack matrices,
            VertexConsumer vertexConsumer,
            boolean cull,
            Random random,
            CallbackInfo ci
    ) {
        if (state == null || !(state.getBlock() instanceof AbstractFireBlock)) return;
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return;

        var fireOpt = bangla.getModuleManager().getModule(CustomFireOverlayModule.class);
        if (fireOpt.isPresent() && fireOpt.get().shouldScaleGroundFire() && fireOpt.get().getGroundFireHeightScale() > 0.05f) {
            matrices.pop();
        }
    }
}
