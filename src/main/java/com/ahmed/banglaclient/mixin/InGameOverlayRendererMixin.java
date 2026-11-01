package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.CustomFireOverlayModule;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Connects `CustomFireOverlayModule` vertical Y-offset to first-person fire overlay rendering.
 */
@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {

    @Inject(method = "renderFireOverlay", at = @At("HEAD"))
    private static void banglaclient$pushFireOverlayOffset(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) {
            return;
        }
        var fireOpt = bangla.getModuleManager().getModule(CustomFireOverlayModule.class);
        if (fireOpt.isPresent() && fireOpt.get().isEnabled()) {
            matrices.translate(0.0f, fireOpt.get().getVerticalOffset(), 0.0f);
        }
    }
}
