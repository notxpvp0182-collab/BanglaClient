package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
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
 * Applies client-side dropped-item physics presentation (flat surface settling & tumble rotation)
 * when ItemPhysicsModule is enabled.
 */
@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            require = 0
    )
    private void banglaclient$applyDroppedItemPhysics(
            ItemEntityRenderState state,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null || state == null) return;
        var modOpt = bangla.getModuleManager().getModule(ItemPhysicsModule.class);
        if (modOpt.isEmpty() || !modOpt.get().isEnabled()) return;

        ItemPhysicsModule mod = modOpt.get();
        if (mod.shouldSettleFlat()) {
            matrices.translate(0.0f, mod.getGroundOffset(), 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
            float spin = (state.age * 2.5f * mod.getRotationSpeed()) % 360.0f;
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(spin));
        }
    }
}
