package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.identity.BcIdentityManager;
import com.ahmed.banglaclient.module.visual.NametagsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {

    @Inject(
            method = "getDisplayName(Lnet/minecraft/entity/Entity;)Lnet/minecraft/text/Text;",
            at = @At("RETURN"),
            cancellable = true,
            require = 0
    )
    private void banglaclient$indentNametagForBcIcon(T entity, CallbackInfoReturnable<Text> cir) {
        if (!(entity instanceof PlayerEntity player)) return;
        Text original = cir.getReturnValue();
        if (original == null) return;

        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var nametagsOpt = bangla.getModuleManager().getModule(NametagsModule.class);
            if (nametagsOpt.isPresent() && !nametagsOpt.get().shouldShowBcBadge()) {
                return;
            }
        }

        if (BcIdentityManager.isVerifiedBcUser(player.getUuid())
                || BcIdentityManager.isVerifiedBcUserByName(player.getName().getString())) {
            cir.setReturnValue(BcIdentityManager.createBcPrefixBadge().append(original));
        }
    }

    @Inject(
            method = "renderLabelIfPresent",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"),
            require = 0
    )
    private void banglaclient$drawRealHdIconOnOverheadNametag(
            S state,
            MatrixStack matrices,
            @Coerce Object renderQueue,
            @Coerce Object cameraState,
            CallbackInfo ci
    ) {
        if (state == null || state.displayName == null) return;
        String raw = state.displayName.getString();
        if (!BcIdentityManager.isVerifiedBcUserByName(raw)) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null || mc.getBufferBuilders() == null) return;

        int totalW = mc.textRenderer.getWidth(state.displayName);
        float iconSize = 15.0f;
        float iconX = -(totalW / 2.0f) - 2.0f;
        float iconY = -3.5f;

        Matrix4f pos = matrices.peek().getPositionMatrix();
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vc = immediate.getBuffer(RenderLayers.entityCutout(BcIdentityManager.IDENTITY_ICON));
        int light = 0xF000F0;

        // Emit BOTH front and back winding orders so negative Y scale (-0.025f) never backface-culls the icon!
        for (float zOff : new float[]{0.03f, -0.03f}) {
            // Winding 1 (CCW for negative Y scale)
            vc.vertex(pos, iconX, iconY, zOff).color(255, 255, 255, 255).texture(0.0f, 0.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(pos, iconX + iconSize, iconY, zOff).color(255, 255, 255, 255).texture(1.0f, 0.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(pos, iconX + iconSize, iconY + iconSize, zOff).color(255, 255, 255, 255).texture(1.0f, 1.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(pos, iconX, iconY + iconSize, zOff).color(255, 255, 255, 255).texture(0.0f, 1.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);

            // Winding 2 (CW fallback)
            vc.vertex(pos, iconX, iconY + iconSize, zOff).color(255, 255, 255, 255).texture(0.0f, 1.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(pos, iconX + iconSize, iconY + iconSize, zOff).color(255, 255, 255, 255).texture(1.0f, 1.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(pos, iconX + iconSize, iconY, zOff).color(255, 255, 255, 255).texture(1.0f, 0.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(pos, iconX, iconY, zOff).color(255, 255, 255, 255).texture(0.0f, 0.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
        }
        immediate.draw();
    }

    @Mixin(LivingEntityRenderer.class)
    public static abstract class LivingNametagHookMixin<T extends LivingEntity> {

        @Inject(
                method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z",
                at = @At("HEAD"),
                cancellable = true,
                require = 0
        )
        private void banglaclient$showOwnNametagInThirdPerson(T entity, double squaredDistanceToCamera, CallbackInfoReturnable<Boolean> cir) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.player == null || entity != mc.player) return;
            if (mc.options.getPerspective().isFirstPerson() || !MinecraftClient.isHudEnabled()) return;

            BanglaClient bangla = BanglaClient.getInstance();
            if (bangla != null && bangla.getModuleManager() != null) {
                var nametagsOpt = bangla.getModuleManager().getModule(NametagsModule.class);
                if (nametagsOpt.isPresent() && nametagsOpt.get().shouldShowOwnNametag()) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
