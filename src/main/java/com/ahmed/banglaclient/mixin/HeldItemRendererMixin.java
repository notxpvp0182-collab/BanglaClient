package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.combat.ShieldOptimizerModule;
import com.ahmed.banglaclient.module.visual.SmallHeldItemModule;
import com.ahmed.banglaclient.module.visual.SmallItemsSystemModule;
import com.ahmed.banglaclient.module.visual.ZoomModule;
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

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Inject(method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true, require = 0)
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
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return;

        var zoomOpt = bangla.getModuleManager().getModule(ZoomModule.class);
        if (zoomOpt.isPresent() && zoomOpt.get().shouldHideHandWhileZooming()) {
            ci.cancel();
            return;
        }

        if (item == null || item.isEmpty()) return;
        var sysOpt = bangla.getModuleManager().getModule(SmallItemsSystemModule.class);
        if (sysOpt.isPresent() && sysOpt.get().isEnabled()) {
            float s = sysOpt.get().getScaleForStack(item);
            if (Math.abs(s - 1.0f) > 0.01f) matrices.scale(s, s, s);
        }
        if (item.isOf(Items.TOTEM_OF_UNDYING)) {
            bangla.getModuleManager().getModule(com.ahmed.banglaclient.module.catalog.SmallTotemModule.class).ifPresent(m -> { if (m.isEnabled()) matrices.scale(m.getLevelMultiplier(), m.getLevelMultiplier(), m.getLevelMultiplier()); });
            bangla.getModuleManager().getModule(com.ahmed.banglaclient.module.catalog.TotemResizerModule.class).ifPresent(m -> { if (m.isEnabled()) matrices.scale(m.getLevelMultiplier(), m.getLevelMultiplier(), m.getLevelMultiplier()); });
        } else if (item.isOf(Items.SHIELD)) {
            bangla.getModuleManager().getModule(com.ahmed.banglaclient.module.catalog.SmallShieldModule.class).ifPresent(m -> { if (m.isEnabled()) matrices.scale(m.getLevelMultiplier(), m.getLevelMultiplier(), m.getLevelMultiplier()); });
        } else if (item.isIn(net.minecraft.registry.tag.ItemTags.SWORDS)) {
            bangla.getModuleManager().getModule(com.ahmed.banglaclient.module.catalog.SmallSwordModule.class).ifPresent(m -> { if (m.isEnabled()) matrices.scale(m.getLevelMultiplier(), m.getLevelMultiplier(), m.getLevelMultiplier()); });
        } else if (item.isIn(net.minecraft.registry.tag.ItemTags.AXES)) {
            bangla.getModuleManager().getModule(com.ahmed.banglaclient.module.catalog.SmallAxeModule.class).ifPresent(m -> { if (m.isEnabled()) matrices.scale(m.getLevelMultiplier(), m.getLevelMultiplier(), m.getLevelMultiplier()); });
        } else if (item.isIn(net.minecraft.registry.tag.ItemTags.PICKAXES)) {
            bangla.getModuleManager().getModule(com.ahmed.banglaclient.module.catalog.SmallPickaxeModule.class).ifPresent(m -> { if (m.isEnabled()) matrices.scale(m.getLevelMultiplier(), m.getLevelMultiplier(), m.getLevelMultiplier()); });
        } else if (item.isIn(net.minecraft.registry.tag.ItemTags.SHOVELS)) {
            bangla.getModuleManager().getModule(com.ahmed.banglaclient.module.catalog.SmallShovelModule.class).ifPresent(m -> { if (m.isEnabled()) matrices.scale(m.getLevelMultiplier(), m.getLevelMultiplier(), m.getLevelMultiplier()); });
        } else if (item.contains(net.minecraft.component.DataComponentTypes.FOOD)) {
            bangla.getModuleManager().getModule(com.ahmed.banglaclient.module.catalog.SmallFoodModule.class).ifPresent(m -> { if (m.isEnabled()) matrices.scale(m.getLevelMultiplier(), m.getLevelMultiplier(), m.getLevelMultiplier()); });
        } else if (item.getItem() instanceof net.minecraft.item.BlockItem) {
            bangla.getModuleManager().getModule(com.ahmed.banglaclient.module.catalog.SmallBlockModule.class).ifPresent(m -> { if (m.isEnabled()) matrices.scale(m.getLevelMultiplier(), m.getLevelMultiplier(), m.getLevelMultiplier()); });
        }

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
