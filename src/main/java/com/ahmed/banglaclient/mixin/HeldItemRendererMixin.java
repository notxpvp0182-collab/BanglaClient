package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.combat.ShieldOptimizerModule;
import com.ahmed.banglaclient.module.visual.SmallHeldItemModule;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies SmallHeldItemModule scaling/offsets and ShieldOptimizerModule lower-shield offset
 * to first-person held items.
 */
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), require = 0)
    private void banglaclient$scaleAndPositionHeldItem(
            AbstractClientPlayerEntity player,
            float tickDelta,
            float pitch,
            Hand hand,
            float swingProgress,
            ItemStack item,
            float equipProgress,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        if (item == null || item.isEmpty()) return;
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return;

        if (item.isOf(Items.SHIELD)) {
            var shieldOpt = bangla.getModuleManager().getModule(ShieldOptimizerModule.class);
            if (shieldOpt.isPresent() && shieldOpt.get().isEnabled()) {
                matrices.translate(0.0f, shieldOpt.get().getShieldVerticalOffset(), 0.0f);
            }
        }

        var smallOpt = bangla.getModuleManager().getModule(SmallHeldItemModule.class);
        if (smallOpt.isPresent() && smallOpt.get().isEnabled()) {
            SmallHeldItemModule mod = smallOpt.get();
            float scale = (hand == Hand.MAIN_HAND) ? mod.getMainHandScale() : mod.getOffHandScale();
            float ox = (hand == Hand.MAIN_HAND) ? mod.getOffsetX() : -mod.getOffsetX();
            matrices.translate(ox, mod.getOffsetY(), mod.getOffsetZ());
            matrices.scale(scale, scale, scale);
        }
    }
}
