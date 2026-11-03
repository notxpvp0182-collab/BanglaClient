package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.core.PerformancePreset;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.EnumSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;

import java.util.concurrent.CompletableFuture;

/**
 * Module 108: Performance Monitor.
 * Displays unified real-time client performance telemetry and provides safe memory optimization.
 */
public final class PerformanceMonitorModule extends HudModule {
    private final EnumSetting<PerformancePreset> presetSetting;
    private final BooleanSetting optimizeMemoryActionSetting;

    public PerformanceMonitorModule() {
        super(
                "performance_monitor",
                "Performance Monitor",
                "Displays real-time FPS, frame time, memory metrics, and preset controls.",
                ModuleCategory.PERFORMANCE,
                440.0f,
                338.0f,
                148.0f,
                56.0f,
                false
        );
        this.presetSetting = addSetting(new EnumSetting<>(
                "active_preset", "Performance Preset", "Active client performance optimization preset", PerformancePreset.BALANCED
        ));
        this.presetSetting.onChange(p -> {
            if (p != null && BanglaClient.getInstance() != null) {
                BanglaClient.getInstance().setPerformancePreset(p);
            }
        });

        this.optimizeMemoryActionSetting = addSetting(new BooleanSetting(
                "optimize_memory", "Action: Optimize Memory", "Safely requests asynchronous garbage collection to free unused heap", false
        ));
        this.optimizeMemoryActionSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val)) {
                CompletableFuture.runAsync(() -> {
                    System.gc();
                    if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
                        BanglaClient.getInstance().getNotificationManager().pushSuccess("Memory Optimized", "Garbage collection completed.");
                    }
                });
                optimizeMemoryActionSetting.setValue(false);
            }
        });
        addAliases("telemetry", "perfmonitor", "systeminfo", "profiler", "fpsboost");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int fps = client != null ? Math.max(1, client.getCurrentFps()) : 60;
        float frameMs = 1000.0f / (float) fps;

        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L);
        long maxMb = Math.max(1L, rt.maxMemory() / (1024L * 1024L));
        int memPct = (int) Math.min(100L, (usedMb * 100L) / maxMb);

        int chunks = client != null && client.world != null
                ? client.world.getChunkManager().getLoadedChunkCount()
                : 289;
        String preset = BanglaClient.getInstance() != null
                ? BanglaClient.getInstance().getPerformancePreset().getDisplayName()
                : "Balanced";

        String[] lines = {
                String.format("FPS: %d (%.1f ms)", fps, frameMs),
                String.format("Heap: %d / %d MB (%d%%)", usedMb, maxMb, memPct),
                "Loaded Chunks: " + chunks,
                "Preset: " + preset
        };

        int pad = paddingSetting.getIntValue();
        int maxW = 112;
        for (String line : lines) {
            maxW = Math.max(maxW, client.textRenderer.getWidth(line));
        }

        int boxW = maxW + pad * 2;
        int boxH = lines.length * 11 + pad * 2;
        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());
        Theme theme = GlassRenderer.getTheme();

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(context, x, y, boxW, boxH, backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled());
        }

        int drawY = y + pad + 1;
        for (int i = 0; i < lines.length; i++) {
            int color = (i == 0)
                    ? (fps < 30 ? ThemeTokens.STATUS_ERROR : textColorSetting.getArgb())
                    : theme.getTextSecondary();
            context.drawTextWithShadow(client.textRenderer, lines[i], x + pad, drawY, color);
            drawY += 11;
        }
    }
}
