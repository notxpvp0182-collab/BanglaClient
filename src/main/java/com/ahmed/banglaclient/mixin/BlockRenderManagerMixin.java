package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.CustomFireOverlayModule;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BlockRenderManager.class)
public abstract class BlockRenderManagerMixin {

    @Inject(
            method = "renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/List;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void banglaclient$scaleGroundFireBlockHead(
            BlockState state,
            BlockPos pos,
            BlockRenderView world,
            MatrixStack matrices,
            VertexConsumer vertexConsumer,
            boolean cull,
            List<?> parts,
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
            method = "renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/List;)V",
            at = @At("RETURN"),
            require = 0
    )
    private void banglaclient$scaleGroundFireBlockReturn(
            BlockState state,
            BlockPos pos,
            BlockRenderView world,
            MatrixStack matrices,
            VertexConsumer vertexConsumer,
            boolean cull,
            List<?> parts,
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
