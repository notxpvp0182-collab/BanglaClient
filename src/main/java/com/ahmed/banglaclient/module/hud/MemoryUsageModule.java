package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;

/**
 * Unified Memory Usage + Optional Real-Time Heap Graph Module.
 */
public final class MemoryUsageModule extends HudModule {
    private static final int GRAPH_SAMPLES = 48;
    private final BooleanSetting showMaxMemorySetting;
    private final BooleanSetting showProgressBarSetting;
    private final BooleanSetting showGraphSetting;

    private final int[] samples = new int[GRAPH_SAMPLES];
    private int cursor = 0;
    private int tickDivider = 0;

    public MemoryUsageModule() {
        super("memory_usage", "Memory Usage", "Displays JVM heap usage, progress bar, and optional real-time memory graph.",
                ModuleCategory.HUD, 160.0f, 34.0f, 110.0f, 22.0f, false);
        this.showMaxMemorySetting = addSetting(new BooleanSetting("show_max_mb", "Show Max Allocated MB", "Display used/max megabytes", true));
        this.showProgressBarSetting = addSetting(new BooleanSetting("show_bar", "Show Usage Bar", "Render a mini progress bar", true));
        this.showGraphSetting = addSetting(new BooleanSetting("show_graph", "Show Memory Graph", "Render a live RAM usage history curve", false));
        for (int i = 0; i < GRAPH_SAMPLES; i++) samples[i] = 45;
        addAliases("ram", "memorygraph", "heap", "jvm", "mem");
    }

    @Override
    public void onTick() {
        tickDivider++;
        if (tickDivider >= 4) {
            tickDivider = 0;
            Runtime rt = Runtime.getRuntime();
            long max = Math.max(1L, rt.maxMemory());
            long used = rt.totalMemory() - rt.freeMemory();
            samples[cursor] = (int) Math.min(100L, (used * 100L) / max);
            cursor = (cursor + 1) % GRAPH_SAMPLES;
        }
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L);
        long maxMb = Math.max(1L, rt.maxMemory() / (1024L * 1024L));
        int pct = (int) Math.min(100L, (usedMb * 100L) / maxMb);

        String text = showMaxMemorySetting.isEnabled()
                ? String.format("MEM: %d%% (%d/%d MB)", pct, usedMb, maxMb)
                : String.format("MEM: %d%%", pct);

        int pad = paddingSetting.getIntValue();
        int barExtraH = showProgressBarSetting.isEnabled() ? 5 : 0;
        int graphExtraH = showGraphSetting.isEnabled() ? 16 : 0;
        int boxW = Math.max(showGraphSetting.isEnabled() ? (GRAPH_SAMPLES + pad * 2) : 0, client.textRenderer.getWidth(text) + pad * 2);
        int boxH = client.textRenderer.fontHeight + pad * 2 + barExtraH + graphExtraH;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(context, x, y, boxW, boxH, backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled());
        }

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, textColorSetting.getArgb());

        if (showProgressBarSetting.isEnabled()) {
            int barX = x + pad;
            int barY = y + pad + client.textRenderer.fontHeight + 3;
            int barW = boxW - pad * 2;
            context.fill(barX, barY, barX + barW, barY + 2, 0x33FFFFFF);
            int fillW = Math.round((pct / 100.0f) * barW);
            int barColor = pct >= 85 ? ThemeTokens.STATUS_ERROR : (pct >= 70 ? ThemeTokens.STATUS_WARNING : 0xFFFFFFFF);
            if (fillW > 0) context.fill(barX, barY, barX + fillW, barY + 2, barColor);
        }

        if (showGraphSetting.isEnabled()) {
            int gx = x + pad;
            int bottomY = y + boxH - pad;
            for (int i = 0; i < GRAPH_SAMPLES; i++) {
                int val = samples[(cursor + i) % GRAPH_SAMPLES];
                int h = Math.max(1, Math.min(13, Math.round((val / 100.0f) * 13.0f)));
                context.fill(gx + i, bottomY - h, gx + i + 1, bottomY, 0x88FFFFFF);
            }
        }
    }
}
