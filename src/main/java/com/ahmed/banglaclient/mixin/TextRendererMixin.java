package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.identity.BcIdentityManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Renders the Full-HD BANGLA CLIENT Identity Icon right beside the player's 3D Overhead Nametag
 * during the actual text-rendering pass (after the background plate is drawn!).
 */
@Mixin(TextRenderer.class)
public abstract class TextRendererMixin {

    @Inject(
            method = "draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I",
            at = @At("RETURN"),
            require = 0
    )
    private void banglaclient$drawBcIconOnTextNametag(
            Text text,
            float x,
            float y,
            int color,
            boolean shadow,
            Matrix4f matrix,
            VertexConsumerProvider vertexConsumers,
            TextRenderer.TextLayerType layerType,
            int backgroundColor,
            int light,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (text == null || shadow) return;
        String raw = text.getString();
        if (raw.startsWith("    ") && BcIdentityManager.isVerifiedBcUserByName(raw)) {
            draw3DIconQuad(matrix, vertexConsumers, x + 1.0f, y - 2.5f, 12.0f, light);
        }
    }

    @Inject(
            method = "draw(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I",
            at = @At("RETURN"),
            require = 0
    )
    private void banglaclient$drawBcIconOnOrderedTextNametag(
            OrderedText text,
            float x,
            float y,
            int color,
            boolean shadow,
            Matrix4f matrix,
            VertexConsumerProvider vertexConsumers,
            TextRenderer.TextLayerType layerType,
            int backgroundColor,
            int light,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (text == null || shadow) return;
        StringBuilder sb = new StringBuilder();
        text.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        String raw = sb.toString();
        if (raw.startsWith("    ") && BcIdentityManager.isVerifiedBcUserByName(raw)) {
            draw3DIconQuad(matrix, vertexConsumers, x + 1.0f, y - 2.5f, 12.0f, light);
        }
    }

    @Unique
    private static void draw3DIconQuad(Matrix4f pos, VertexConsumerProvider vcp, float ix, float iy, float size, int light) {
        try {
            VertexConsumer vc = vcp.getBuffer(RenderLayers.entityTranslucent(BcIdentityManager.IDENTITY_ICON));
            float z = 0.03f;
            // Front & back winding so negative Y scale never culls the quad
            vc.vertex(pos, ix, iy, z).color(255, 255, 255, 255).texture(0.0f, 0.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(pos, ix + size, iy, z).color(255, 255, 255, 255).texture(1.0f, 0.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(pos, ix + size, iy + size, z).color(255, 255, 255, 255).texture(1.0f, 1.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(pos, ix, iy + size, z).color(255, 255, 255, 255).texture(0.0f, 1.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);

            vc.vertex(pos, ix, iy + size, z).color(255, 255, 255, 255).texture(0.0f, 1.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(pos, ix + size, iy + size, z).color(255, 255, 255, 255).texture(1.0f, 1.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(pos, ix + size, iy, z).color(255, 255, 255, 255).texture(1.0f, 0.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(pos, ix, iy, z).color(255, 255, 255, 255).texture(0.0f, 0.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.0f, 1.0f, 0.0f);
        } catch (Throwable ignored) {}
    }
}
