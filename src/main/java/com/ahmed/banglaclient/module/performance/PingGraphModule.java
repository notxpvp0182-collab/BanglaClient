package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.hud.PingDisplayModule;
import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 102: Ping Graph
 * Renders a lightweight 96-sample latency history graph using pre-allocated primitive arrays.
 */
public final class PingGraphModule extends HudModule {
    private static final int SAMPLE_COUNT = 96;

    private final int[] samples = new int[SAMPLE_COUNT];
    private int writeCursor = 0;
    private int tickDivider = 0;

    public PingGraphModule() {
        super(
                "ping_graph",
                "Ping Graph",
                "Displays a lightweight server latency history graph.",
                ModuleCategory.PERFORMANCE,
                440.0f,
                208.0f,
                114.0f,
                46.0f,
                false
        );
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            samples[i] = 35;
        }
        addAliases("latencygraph", "networkgraph", "msgraph");
    }

    @Override
    public void onTick() {
        tickDivider++;
        if (tickDivider < 5) { // Sample 4 times per second
            return;
        }
        tickDivider = 0;

        int ping = 0;
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getModuleManager() != null) {
            var pingMod = BanglaClient.getInstance().getModuleManager().getModule(PingDisplayModule.class);
            if (pingMod.isPresent()) {
                ping = Math.max(0, pingMod.get().getCurrentPingMs());
            }
        }
        samples[writeCursor] = ping;
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

        int latestPing = samples[(writeCursor - 1 + SAMPLE_COUNT) % SAMPLE_COUNT];
        int maxPing = 100;
        for (int s : samples) {
            maxPing = Math.max(maxPing, s);
        }

        String header = client != null && client.isInSingleplayer() && !preview
                ? "Ping: Local (0 ms)"
                : ("Ping Graph • " + latestPing + " ms");
        context.drawTextWithShadow(client.textRenderer, header, x + pad, y + pad, textColorSetting.getArgb());

        int gx = x + pad;
        int gy = y + pad + 12;
        int bottomY = gy + graphH;

        context.fill(gx, bottomY - 1, gx + graphW, bottomY, theme.getBorderSubtle());

        for (int i = 0; i < SAMPLE_COUNT; i++) {
            int idx = (writeCursor + i) % SAMPLE_COUNT;
            int val = samples[idx];
            float ratio = Math.min(1.0f, Math.max(0.05f, (float) val / (float) maxPing));
            int colH = Math.round(ratio * (graphH - 2));
            int colX = gx + i;
            int topY = bottomY - colH;

            int color = val > 160
                    ? ThemeTokens.STATUS_ERROR
                    : (val > 85 ? ThemeTokens.STATUS_WARNING : ThemeTokens.STATUS_SUCCESS);
            context.fill(colX, topY, colX + 1, bottomY - 1, ColorUtils.withAlpha(color, 0.24f));
            context.fill(colX, topY, colX + 1, topY + 1, color);
        }
    }
}
