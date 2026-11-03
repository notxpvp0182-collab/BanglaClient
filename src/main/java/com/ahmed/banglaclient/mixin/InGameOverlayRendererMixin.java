package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.CustomFireOverlayModule;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {

    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private static void banglaclient$pushFireOverlayOffset(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            Sprite sprite,
            CallbackInfo ci
    ) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) {
            return;
        }
        var fireOpt = bangla.getModuleManager().getModule(CustomFireOverlayModule.class);
        if (fireOpt.isPresent() && fireOpt.get().isEnabled()) {
            if (fireOpt.get().shouldHideOverlayCompletely()) {
                ci.cancel();
                return;
            }
            matrices.translate(0.0f, fireOpt.get().getVerticalOffset(), 0.0f);
            float scale = fireOpt.get().getOpacityScale();
            matrices.scale(1.0f, scale, 1.0f);
        }
    }
}
