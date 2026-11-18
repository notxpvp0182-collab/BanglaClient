package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.ModuleManager;
import com.ahmed.banglaclient.module.combat.ShieldOptimizerModule;
import com.ahmed.banglaclient.module.visual.*;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Inject(method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER), cancellable = true, require = 0)
    private void banglaclient$applySmallItemsAndViewmodelTransforms(
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
        ModuleManager mm = bangla.getModuleManager();

        var zoomOpt = mm.getModule(ZoomModule.class);
        if (zoomOpt.isPresent() && zoomOpt.get().shouldHideHandWhileZooming()) {
            matrices.pop(); ci.cancel();
            return;
        }

        if (item == null || item.isEmpty()) return;
        boolean isMainHand = (hand == Hand.MAIN_HAND);
        float handSign = isMainHand ? 1.0f : -1.0f;

        // 1. Viewmodel Editor (Global X, Y, Z, RotX, RotY, RotZ, Scale)
        var vmOpt = mm.getModule(ViewmodelEditorModule.class);
        if (vmOpt.isPresent() && vmOpt.get().isEnabled()) {
            ViewmodelEditorModule vm = vmOpt.get();
            matrices.translate(vm.getPosX() * handSign, vm.getPosY(), vm.getPosZ());
            if (Math.abs(vm.getRotX()) > 0.1f) matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(vm.getRotX()));
            if (Math.abs(vm.getRotY()) > 0.1f) matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(vm.getRotY() * handSign));
            if (Math.abs(vm.getRotZ()) > 0.1f) matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(vm.getRotZ() * handSign));
            float s = vm.getScale();
            matrices.scale(s, s, s);
        }

        // 2. General Small Held Item Module
        var smallOpt = mm.getModule(SmallHeldItemModule.class);
        if (smallOpt.isPresent() && smallOpt.get().isEnabled()) {
            SmallHeldItemModule mod = smallOpt.get();
            float scale = isMainHand ? mod.getMainHandScale() : mod.getOffHandScale();
            matrices.translate(mod.getOffsetX() * handSign, mod.getOffsetY(), mod.getOffsetZ());
            matrices.scale(scale, scale, scale);
        }

        // 3. Combined SmallItemsSystemModule
        var sysOpt = mm.getModule(SmallItemsSystemModule.class);
        if (sysOpt.isPresent() && sysOpt.get().isEnabled()) {
            float s = sysOpt.get().getScaleForStack(item);
            if (Math.abs(s - 1.0f) > 0.01f) matrices.scale(s, s, s);
        }

        // 4. Individual Small Item Modules (Sections 11: #211-#219)
        if (item.isOf(Items.TOTEM_OF_UNDYING)) {
            mm.getModule(SmallTotemModule.class).ifPresent(m -> {
                if (m.isEnabled()) {
                    matrices.translate(m.getOffsetX() * handSign, m.getOffsetY(), m.getOffsetZ());
                    matrices.scale(m.getScale(), m.getScale(), m.getScale());
                }
            });
            mm.getModule(TotemResizerModule.class).ifPresent(m -> {
                if (m.isEnabled()) {
                    matrices.translate(0.0f, m.getVerticalOffset(), 0.0f);
                    float ts = m.getScale(isMainHand);
                    matrices.scale(ts, ts, ts);
                }
            });
        } else if (item.isOf(Items.SHIELD)) {
            mm.getModule(ShieldOptimizerModule.class).ifPresent(m -> {
                if (m.isEnabled()) matrices.translate(0.0f, m.getShieldVerticalOffset(), 0.0f);
            });
            mm.getModule(SmallShieldModule.class).ifPresent(m -> {
                if (m.isEnabled()) {
                    matrices.translate(m.getOffsetX() * handSign, m.getOffsetY(), m.getOffsetZ());
                    matrices.scale(m.getScale(), m.getScale(), m.getScale());
                }
            });
        } else if (item.isIn(ItemTags.SWORDS)) {
            mm.getModule(SmallSwordModule.class).ifPresent(m -> {
                if (m.isEnabled()) {
                    matrices.translate(m.getOffsetX() * handSign, m.getOffsetY(), m.getOffsetZ());
                    matrices.scale(m.getScale(), m.getScale(), m.getScale());
                }
            });
        } else if (item.isIn(ItemTags.AXES)) {
            mm.getModule(SmallAxeModule.class).ifPresent(m -> {
                if (m.isEnabled()) {
                    matrices.translate(m.getOffsetX() * handSign, m.getOffsetY(), m.getOffsetZ());
                    matrices.scale(m.getScale(), m.getScale(), m.getScale());
                }
            });
        } else if (item.isIn(ItemTags.PICKAXES)) {
            mm.getModule(SmallPickaxeModule.class).ifPresent(m -> {
                if (m.isEnabled()) {
                    matrices.translate(m.getOffsetX() * handSign, m.getOffsetY(), m.getOffsetZ());
                    matrices.scale(m.getScale(), m.getScale(), m.getScale());
                }
            });
        } else if (item.isIn(ItemTags.SHOVELS)) {
            mm.getModule(SmallShovelModule.class).ifPresent(m -> {
                if (m.isEnabled()) {
                    matrices.translate(m.getOffsetX() * handSign, m.getOffsetY(), m.getOffsetZ());
                    matrices.scale(m.getScale(), m.getScale(), m.getScale());
                }
            });
        } else if (item.contains(DataComponentTypes.FOOD)) {
            mm.getModule(SmallFoodModule.class).ifPresent(m -> {
                if (m.isEnabled()) {
                    matrices.translate(m.getOffsetX() * handSign, m.getOffsetY(), m.getOffsetZ());
                    matrices.scale(m.getScale(), m.getScale(), m.getScale());
                }
            });
        } else if (item.getItem() instanceof BlockItem) {
            mm.getModule(SmallBlockModule.class).ifPresent(m -> {
                if (m.isEnabled()) {
                    matrices.translate(m.getOffsetX() * handSign, m.getOffsetY(), m.getOffsetZ());
                    matrices.scale(m.getScale(), m.getScale(), m.getScale());
                }
            });
        }
    }
}
