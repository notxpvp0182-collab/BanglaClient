package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 100: FPS Graph
 * Renders a lightweight 100-sample ring-buffer history graph of frames per second using
 * pre-allocated primitive arrays (zero per-frame heap allocations).
 */
public final class FpsGraphModule extends HudModule {
    private static final int SAMPLE_COUNT = 96;

    private final BooleanSetting showMinMaxAvgSetting;
    private final int[] samples = new int[SAMPLE_COUNT];
    private int writeCursor = 0;

    public FpsGraphModule() {
        super(
                "fps_graph",
                "FPS Graph",
                "Displays a lightweight real-time FPS history graph with min/avg/max metrics.",
                ModuleCategory.PERFORMANCE,
                440.0f,
                130.0f,
                114.0f,
                46.0f,
                false
        );
        this.showMinMaxAvgSetting = addSetting(new BooleanSetting(
                "show_stats", "Show Min / Avg Header", "Display minimum and average FPS above the graph curve", true
        ));
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            samples[i] = 60;
        }
        addAliases("fpschart", "framegraph", "frametimegraph");
    }

    @Override
    public void onTick() {
        int currentFps = client != null ? Math.max(0, client.getCurrentFps()) : 60;
        samples[writeCursor] = currentFps;
        writeCursor = (writeCursor + 1) % SAMPLE_COUNT;
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int pad = paddingSetting.getIntValue();
        int headerExtra = showMinMaxAvgSetting.isEnabled() ? 12 : 0;
        int graphW = SAMPLE_COUNT;
        int graphH = 26;
        int boxW = graphW + pad * 2;
        int boxH = graphH + headerExtra + pad * 2;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());
        Theme theme = GlassRenderer.getTheme();

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        int minFps = Integer.MAX_VALUE;
        int maxFps = 1;
        long sumFps = 0L;
        for (int sample : samples) {
            minFps = Math.min(minFps, sample);
            maxFps = Math.max(maxFps, sample);
            sumFps += sample;
        }
        int avgFps = (int) (sumFps / SAMPLE_COUNT);
        int ceiling = Math.max(60, maxFps + 10);

        if (showMinMaxAvgSetting.isEnabled()) {
            String stats = String.format("FPS • Avg:%d Min:%d", avgFps, minFps == Integer.MAX_VALUE ? 0 : minFps);
            context.drawTextWithShadow(client.textRenderer, stats, x + pad, y + pad, textColorSetting.getArgb());
        }

        int gx = x + pad;
        int gy = y + pad + headerExtra;
        int bottomY = gy + graphH;

        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : ThemeTokens.DEFAULT_ACCENT_PRIMARY;
        int fillTint = ColorUtils.withAlpha(accent, 0.25f);

        // Baseline
        context.fill(gx, bottomY - 1, gx + graphW, bottomY, theme.getBorderSubtle());

        for (int i = 0; i < SAMPLE_COUNT; i++) {
            int idx = (writeCursor + i) % SAMPLE_COUNT;
            int val = samples[idx];
            float ratio = Math.min(1.0f, Math.max(0.04f, (float) val / (float) ceiling));
            int colH = Math.round(ratio * (graphH - 2));
            int colX = gx + i;
            int topY = bottomY - colH;

            context.fill(colX, topY, colX + 1, bottomY - 1, fillTint);
            int pointColor = val < 30 ? ThemeTokens.STATUS_ERROR : accent;
            context.fill(colX, topY, colX + 1, topY + 1, pointColor);
        }
    }
}
