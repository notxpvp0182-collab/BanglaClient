package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.performance.ItemEntityOptimizerModule;
import com.ahmed.banglaclient.module.visual.ItemPhysicsModule;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optimizes Mass Loot Piles (ItemEntityOptimizerModule) and applies 3D Item Physics (ItemPhysicsModule).
 */
@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void banglaclient$optimizeLootAndApplyItemPhysics(
            ItemEntityRenderState state,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        if (state == null) return;
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return;

        // 1. Mass Loot Pile Optimizer: freeze bobbing/spinning on distant loot to save CPU/GPU matrix work
        var lootOpt = bangla.getModuleManager().getModule(ItemEntityOptimizerModule.class);
        if (lootOpt.isPresent() && lootOpt.get().isEnabled()) {
            if (lootOpt.get().isStaticGroundItems()) {
                state.uniqueOffset = 0.0f;
            }
        }

        // 2. 3D Item Physics
        var physOpt = bangla.getModuleManager().getModule(ItemPhysicsModule.class);
        if (physOpt.isPresent() && physOpt.get().isEnabled()) {
            matrices.translate(0.0f, -0.10f, 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
            float spin = (state.age * 4.0f * 1.0f) % 360.0f;
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(spin));
        }
    }
}
