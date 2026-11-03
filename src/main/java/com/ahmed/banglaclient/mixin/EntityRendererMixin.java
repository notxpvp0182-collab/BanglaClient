package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.identity.BcIdentityManager;
import com.ahmed.banglaclient.module.visual.NametagsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(
            method = "renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("TAIL"),
            require = 0
    )
    private void banglaclient$renderBcIdentityIconBesideNametag(
            T entity,
            Text text,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        if (!(entity instanceof PlayerEntity player)) return;
        if (!BcIdentityManager.isVerifiedBcUser(player.getUuid())) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null) return;

        matrices.push();
        int textW = mc.textRenderer.getWidth(text);
        float iconX = -textW / 2.0f - 11.0f;
        float iconY = -1.0f;

        Matrix4f pos = matrices.peek().getPositionMatrix();
        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayers.entityCutout(BcIdentityManager.IDENTITY_ICON));

        vc.vertex(pos, iconX, iconY + 9, 0.0f).color(255, 255, 255, 255).texture(0.0f, 1.0f).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(pos, iconX + 9, iconY + 9, 0.0f).color(255, 255, 255, 255).texture(1.0f, 1.0f).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(pos, iconX + 9, iconY, 0.0f).color(255, 255, 255, 255).texture(1.0f, 0.0f).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(pos, iconX, iconY, 0.0f).color(255, 255, 255, 255).texture(0.0f, 0.0f).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
        matrices.pop();
    }

    @Mixin(LivingEntityRenderer.class)
    public static abstract class LivingNametagHookMixin<T extends LivingEntity> {

        @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true, require = 0)
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
