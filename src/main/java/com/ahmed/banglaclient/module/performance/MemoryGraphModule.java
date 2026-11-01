package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 103: Memory Graph
 * Displays a 96-sample JVM heap allocation history curve using pre-allocated primitive arrays.
 */
public final class MemoryGraphModule extends HudModule {
    private static final int SAMPLE_COUNT = 96;

    private final int[] percentSamples = new int[SAMPLE_COUNT];
    private int writeCursor = 0;
    private int tickDivider = 0;

    public MemoryGraphModule() {
        super(
                "memory_graph",
                "Memory Graph",
                "Displays a real-time JVM memory allocation history curve.",
                ModuleCategory.PERFORMANCE,
                440.0f,
                260.0f,
                114.0f,
                46.0f,
                false
        );
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            percentSamples[i] = 45;
        }
        addAliases("ramgraph", "heapgraph", "gcgraph");
    }

    @Override
    public void onTick() {
        tickDivider++;
        if (tickDivider < 4) {
            return;
        }
        tickDivider = 0;

        Runtime rt = Runtime.getRuntime();
        long max = Math.max(1L, rt.maxMemory());
        long used = rt.totalMemory() - rt.freeMemory();
        int pct = (int) Math.min(100L, (used * 100L) / max);

        percentSamples[writeCursor] = pct;
        writeCursor = (writeCursor + 1) % SAMPLE_COUNT;
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int pad = paddingSetting.getIntValue();
        int graphW = SAMPLE_COUNT;
        int graphH = 26;
        int boxW = graphW + pad * 2;
        int boxH = graphH + 12 + pad * 2;

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

        int latestPct = percentSamples[(writeCursor - 1 + SAMPLE_COUNT) % SAMPLE_COUNT];
        String header = "Heap Graph • " + latestPct + "%";
        context.drawTextWithShadow(client.textRenderer, header, x + pad, y + pad, textColorSetting.getArgb());

        int gx = x + pad;
        int gy = y + pad + 12;
        int bottomY = gy + graphH;

        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : ThemeTokens.DEFAULT_ACCENT_PRIMARY;

        context.fill(gx, bottomY - 1, gx + graphW, bottomY, theme.getBorderSubtle());

        for (int i = 0; i < SAMPLE_COUNT; i++) {
            int idx = (writeCursor + i) % SAMPLE_COUNT;
            int pct = percentSamples[idx];
            float ratio = Math.min(1.0f, Math.max(0.05f, pct / 100.0f));
            int colH = Math.round(ratio * (graphH - 2));
            int colX = gx + i;
            int topY = bottomY - colH;

            int color = pct >= 85 ? ThemeTokens.STATUS_ERROR : accent;
            context.fill(colX, topY, colX + 1, bottomY - 1, ColorUtils.withAlpha(color, 0.22f));
            context.fill(colX, topY, colX + 1, topY + 1, color);
        }
    }
}
