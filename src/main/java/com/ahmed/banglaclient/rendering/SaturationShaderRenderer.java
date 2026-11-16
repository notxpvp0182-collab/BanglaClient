package com.ahmed.banglaclient.rendering;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.ColorSaturationModule;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;

import java.lang.reflect.Method;
import java.util.OptionalInt;

public final class SaturationShaderRenderer {
    public static final int VERTEX_COUNT = 3;
    private static final int BLOCK_SIZE = new Std140SizeCalculator()
            .putFloat()
            .putFloat()
            .putFloat()
            .putFloat()
            .get();

    private static RenderPipeline saturationPipeline = null;
    private static GpuBuffer vertexBuffer = null;
    private static GpuBuffer uboBuffer = null;
    private static SimpleFramebuffer tempTarget = null;
    private static int prevWidth = -1;
    private static int prevHeight = -1;

    private static float uploadedSaturation = Float.NaN;
    private static float uploadedContrast = Float.NaN;
    private static float uploadedBrightness = Float.NaN;
    private static float uploadedHue = Float.NaN;
    private static Method cachedSamplerMethod = null;
    private static Method cachedBindTexMethod = null;

    private SaturationShaderRenderer() {}

    public static GpuBuffer getVertexBuffer() {
        if (vertexBuffer == null) {
            BufferAllocator storage = new BufferAllocator(VertexFormats.POSITION.getVertexSize() * VERTEX_COUNT);
            BufferBuilder builder = new BufferBuilder(
                    storage,
                    VertexFormat.DrawMode.TRIANGLES,
                    VertexFormats.POSITION
            );
            builder.vertex(-1.0f, -1.0f, 0.0f);
            builder.vertex(3.0f, -1.0f, 0.0f);
            builder.vertex(-1.0f, 3.0f, 0.0f);

            try (BuiltBuffer mesh = builder.end()) {
                vertexBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "ColorSaturation fullscreen triangle",
                        GpuBuffer.USAGE_VERTEX,
                        mesh.getBuffer()
                );
            } finally {
                storage.close();
            }
        }
        return vertexBuffer;
    }

    public static GpuBuffer getUniformBuffer() {
        if (uboBuffer == null) {
            uboBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "ColorSaturation_UBO",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
                    (long) BLOCK_SIZE
            );
        }
        return uboBuffer;
    }

    public static void uploadUniforms(float strength, float contrast, float brightness, float hue) {
        if (strength == uploadedSaturation
                && contrast == uploadedContrast
                && brightness == uploadedBrightness
                && hue == uploadedHue
                && uboBuffer != null) {
            return;
        }
        GpuBuffer ubo = getUniformBuffer();
        try (GpuBuffer.MappedView mapped = RenderSystem.getDevice().createCommandEncoder().mapBuffer(ubo, false, true)) {
            mapped.data()
                    .putFloat(strength)
                    .putFloat(contrast)
                    .putFloat(brightness)
                    .putFloat(hue);
        }
        uploadedSaturation = strength;
        uploadedContrast = contrast;
        uploadedBrightness = brightness;
        uploadedHue = hue;
    }

    private static RenderPipeline getPipeline() {
        if (saturationPipeline == null) {
            RenderPipeline.Builder builder = RenderPipeline.builder()
                    .withLocation(Identifier.of(BanglaClient.MOD_ID, "saturation_pipeline"))
                    .withVertexShader(Identifier.of(BanglaClient.MOD_ID, "core/fullscreen_triangle"))
                    .withFragmentShader(Identifier.of(BanglaClient.MOD_ID, "post/color_saturation"))
                    .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.TRIANGLES)
                    .withDepthWrite(false)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withColorWrite(true, true)
                    .withSampler("DiffuseSampler");

            try {
                for (Method m : builder.getClass().getMethods()) {
                    if (m.getName().equals("withUniform") && m.getParameterCount() == 2) {
                        Class<?> paramType = m.getParameterTypes()[1];
                        Object[] constants = paramType.getEnumConstants();
                        if (constants != null && constants.length > 0) {
                            Object chosen = constants[0];
                            for (Object c : constants) {
                                if (c.toString().toUpperCase().contains("UNIFORM")) {
                                    chosen = c;
                                    break;
                                }
                            }
                            m.invoke(builder, "SaturationConfig", chosen);
                        }
                        break;
                    }
                }
            } catch (Throwable ignored) {}

            saturationPipeline = builder.build();
        }
        return saturationPipeline;
    }

    private static void updateTargetSize(Framebuffer mainFb) {
        int width = mainFb.textureWidth;
        int height = mainFb.textureHeight;
        if (width == prevWidth && height == prevHeight && tempTarget != null) {
            return;
        }
        if (tempTarget != null) {
            try { tempTarget.delete(); } catch (Throwable ignored) {}
        }
        tempTarget = new SimpleFramebuffer("ColorSaturation_Temp", width, height, false);
        prevWidth = width;
        prevHeight = height;
    }

    private static void bindDiffuseSampler(RenderPass pass, GpuTextureView inputView) {
        try {
            if (cachedBindTexMethod == null) {
                for (Method m : RenderPass.class.getMethods()) {
                    if (m.getName().equals("bindTexture")) {
                        cachedBindTexMethod = m;
                        break;
                    }
                }
            }
            if (cachedBindTexMethod != null) {
                if (cachedBindTexMethod.getParameterCount() == 2) {
                    cachedBindTexMethod.invoke(pass, "DiffuseSampler", inputView);
                } else if (cachedBindTexMethod.getParameterCount() == 3) {
                    Method samplerGetter = RenderSystem.class.getMethod("getSamplerCache");
                    Object cache = samplerGetter.invoke(null);
                    if (cachedSamplerMethod == null) {
                        for (Method m : cache.getClass().getMethods()) {
                            if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == FilterMode.class) {
                                cachedSamplerMethod = m;
                                break;
                            }
                        }
                    }
                    Object sampler = cachedSamplerMethod != null ? cachedSamplerMethod.invoke(cache, FilterMode.NEAREST) : null;
                    cachedBindTexMethod.invoke(pass, "DiffuseSampler", inputView, sampler);
                }
            }
        } catch (Throwable ignored) {}
    }

    public static boolean renderWorldPass() {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return false;
        var modOpt = bangla.getModuleManager().getModule(ColorSaturationModule.class);
        if (modOpt.isEmpty() || !modOpt.get().isActiveNonIdentity()) return false;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null || mc.getFramebuffer() == null) return false;

        Framebuffer renderTarget = mc.getFramebuffer();
        if (renderTarget.getColorAttachment() == null || renderTarget.getColorAttachmentView() == null) {
            return false;
        }

        ColorSaturationModule mod = modOpt.get();
        try {
            updateTargetSize(renderTarget);
            if (tempTarget == null || tempTarget.getColorAttachment() == null || tempTarget.getColorAttachmentView() == null) {
                return false;
            }

            uploadUniforms(mod.getSaturation(), mod.getContrast(), mod.getBrightness(), mod.getHue());

            RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(
                    renderTarget.getColorAttachment(),
                    tempTarget.getColorAttachment(),
                    0, 0, 0, 0, 0,
                    renderTarget.textureWidth,
                    renderTarget.textureHeight
            );

            drawPass(renderTarget.getColorAttachmentView(), tempTarget.getColorAttachmentView());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static void drawPass(GpuTextureView outputView, GpuTextureView inputView) {
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        GpuBuffer vbo = getVertexBuffer();

        try (RenderPass renderPass = commandEncoder.createRenderPass(
                () -> "ColorSaturation/Saturation",
                outputView,
                OptionalInt.empty()
        )) {
            renderPass.setPipeline(getPipeline());
            renderPass.setVertexBuffer(0, vbo);
            bindDiffuseSampler(renderPass, inputView);
            renderPass.setUniform("SaturationConfig", getUniformBuffer());
            renderPass.draw(0, VERTEX_COUNT);
        }
    }

    public static boolean isShaderUnavailable() {
        return false;
    }
}
