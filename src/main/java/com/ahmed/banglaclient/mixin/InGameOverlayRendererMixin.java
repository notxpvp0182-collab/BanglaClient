package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.ClearWaterModule;
import com.ahmed.banglaclient.module.visual.CustomFireOverlayModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Exact Yarn 1.21.11 signatures for InGameOverlayRenderer:
 * - renderFireOverlay(MatrixStack, VertexConsumerProvider)V
 * - renderUnderwaterOverlay(MinecraftClient, MatrixStack, VertexConsumerProvider)V
 */
@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {

    @Inject(
            method = "renderFireOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void banglaclient$onRenderFireOverlayHead(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return;

        var fireOpt = bangla.getModuleManager().getModule(CustomFireOverlayModule.class);
        if (fireOpt.isPresent() && fireOpt.get().isEnabled()) {
            CustomFireOverlayModule mod = fireOpt.get();
            if (mod.shouldHideOverlayCompletely()) {
                ci.cancel();
                return;
            }
            matrices.push();
            matrices.translate(0.0f, mod.getVerticalOffset(), 0.0f);
            float scale = mod.getOpacityScale();
            matrices.scale(1.0f, scale, 1.0f);
        }
    }

    @Inject(
            method = "renderFireOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
            at = @At("RETURN"),
            require = 0
    )
    private static void banglaclient$onRenderFireOverlayReturn(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return;

        var fireOpt = bangla.getModuleManager().getModule(CustomFireOverlayModule.class);
        if (fireOpt.isPresent() && fireOpt.get().isEnabled() && !fireOpt.get().shouldHideOverlayCompletely()) {
            matrices.pop();
        }
    }

    @Inject(
            method = "renderUnderwaterOverlay(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void banglaclient$onRenderUnderwaterOverlay(MinecraftClient client, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return;

        var waterOpt = bangla.getModuleManager().getModule(ClearWaterModule.class);
        if (waterOpt.isPresent() && waterOpt.get().shouldSoftenWaterOverlay()) {
            ci.cancel();
        }
    }
}
