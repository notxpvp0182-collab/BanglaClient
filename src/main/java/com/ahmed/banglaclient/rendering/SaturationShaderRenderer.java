package com.ahmed.banglaclient.rendering;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.ColorSaturationModule;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;

import java.util.OptionalInt;

/**
 * Hardware-accelerated Blaze3D GPU Shader Pipeline for Color Saturation, Contrast, Brightness, and Hue rotation.
 */
public final class SaturationShaderRenderer {
    private static final int VERTEX_COUNT = 3;
    private static final int UBO_SIZE = new Std140SizeCalculator().putFloat().putFloat().putFloat().putFloat().get();

    private static RenderPipeline pipeline = null;
    private static GpuBuffer vertexBuffer = null;
    private static GpuBuffer uniformBuffer = null;
    private static SimpleFramebuffer tempTarget = null;
    private static int prevWidth = -1;
    private static int prevHeight = -1;
    private static float uploadedSat = Float.NaN;
    private static float uploadedCon = Float.NaN;
    private static float uploadedBri = Float.NaN;
    private static float uploadedHue = Float.NaN;
    private static boolean shaderUnavailable = false;

    private SaturationShaderRenderer() {}

    public static boolean renderWorldPass() {
        if (shaderUnavailable) return false;
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return false;
        var modOpt = bangla.getModuleManager().getModule(ColorSaturationModule.class);
        if (modOpt.isEmpty() || !modOpt.get().isActiveNonIdentity()) return false;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null || mc.getFramebuffer() == null) return false;

        Framebuffer mainFb = mc.getFramebuffer();
        if (mainFb.getColorAttachment() == null || mainFb.getColorAttachmentView() == null) return false;

        ColorSaturationModule mod = modOpt.get();
        try {
            ensureTempTarget(mainFb.textureWidth, mainFb.textureHeight);
            if (tempTarget == null || tempTarget.getColorAttachment() == null || tempTarget.getColorAttachmentView() == null) {
                return false;
            }

            uploadUniforms(mod.getSaturation(), mod.getContrast(), mod.getBrightness(), mod.getHue());

            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            encoder.copyTextureToTexture(
                    mainFb.getColorAttachment(),
                    tempTarget.getColorAttachment(),
                    0, 0, 0, 0, 0,
                    mainFb.textureWidth,
                    mainFb.textureHeight
            );

            try (RenderPass pass = encoder.createRenderPass(
                    () -> "BanglaClient/ColorSaturation",
                    mainFb.getColorAttachmentView(),
                    OptionalInt.empty()
            )) {
                pass.setPipeline(getOrCreatePipeline());
                pass.setVertexBuffer(0, getOrCreateVertexBuffer());
                pass.bindTexture(
                        "DiffuseSampler",
                        tempTarget.getColorAttachmentView(),
                        RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)
                );
                pass.setUniform("SaturationConfig", getOrCreateUniformBuffer());
                pass.draw(0, VERTEX_COUNT);
            }
            return true;
        } catch (Throwable t) {
            shaderUnavailable = true;
            return false;
        }
    }

    private static RenderPipeline getOrCreatePipeline() {
        if (pipeline == null) {
            pipeline = RenderPipeline.builder()
                    .withLocation(Identifier.of(BanglaClient.MOD_ID, "saturation_pipeline"))
                    .withVertexShader(Identifier.of(BanglaClient.MOD_ID, "core/fullscreen_triangle"))
                    .withFragmentShader(Identifier.of(BanglaClient.MOD_ID, "post/color_saturation"))
                    .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.TRIANGLES)
                    .withDepthWrite(false)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withColorWrite(true, true)
                    .withUniform("SaturationConfig", UniformType.UNIFORM_BUFFER)
                    .withSampler("DiffuseSampler")
                    .build();
        }
        return pipeline;
    }

    private static GpuBuffer getOrCreateVertexBuffer() {
        if (vertexBuffer == null) {
            BufferAllocator allocator = new BufferAllocator(VertexFormats.POSITION.getVertexSize() * VERTEX_COUNT);
            BufferBuilder builder = new BufferBuilder(allocator, VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);
            builder.vertex(-1.0f, -1.0f, 0.0f);
            builder.vertex(3.0f, -1.0f, 0.0f);
            builder.vertex(-1.0f, 3.0f, 0.0f);
            try (BuiltBuffer mesh = builder.end()) {
                vertexBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "BanglaClient_FullscreenTriangle",
                        GpuBuffer.USAGE_VERTEX,
                        mesh.getBuffer()
                );
            } finally {
                allocator.close();
            }
        }
        return vertexBuffer;
    }

    private static GpuBuffer getOrCreateUniformBuffer() {
        if (uniformBuffer == null) {
            uniformBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "BanglaClient_SaturationUBO",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
                    (long) UBO_SIZE
            );
        }
        return uniformBuffer;
    }

    private static void uploadUniforms(float sat, float con, float bri, float hue) {
        if (sat == uploadedSat && con == uploadedCon && bri == uploadedBri && hue == uploadedHue && uniformBuffer != null) {
            return;
        }
        GpuBuffer ubo = getOrCreateUniformBuffer();
        try (GpuBuffer.MappedView mapped = RenderSystem.getDevice().createCommandEncoder().mapBuffer(ubo, false, true)) {
            mapped.data().putFloat(sat).putFloat(con).putFloat(bri).putFloat(hue);
        }
        uploadedSat = sat;
        uploadedCon = con;
        uploadedBri = bri;
        uploadedHue = hue;
    }

    private static void ensureTempTarget(int w, int h) {
        if (w <= 0 || h <= 0) return;
        if (w == prevWidth && h == prevHeight && tempTarget != null) return;
        if (tempTarget != null) {
            try { tempTarget.delete(); } catch (Throwable ignored) {}
        }
        tempTarget = new SimpleFramebuffer("BanglaClient_SaturationTarget", w, h, false);
        prevWidth = w;
        prevHeight = h;
    }

    public static boolean isShaderUnavailable() {
        return shaderUnavailable;
    }
}
