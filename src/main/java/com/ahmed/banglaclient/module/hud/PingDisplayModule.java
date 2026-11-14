package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

/**
 * Unified Ping Display + Optional Real-Time Ping Graph Module.
 */
public final class PingDisplayModule extends HudModule {
    private static final int GRAPH_SAMPLES = 48;
    private final BooleanSetting colorByQualitySetting;
    private final BooleanSetting showGraphSetting;

    private final int[] samples = new int[GRAPH_SAMPLES];
    private int cursor = 0;
    private int tickDivider = 0;

    public PingDisplayModule() {
        super("ping_display", "Ping Display", "Displays live multiplayer latency and an optional real-time ping graph.",
                ModuleCategory.HUD, 80.0f, 10.0f, 76.0f, 20.0f, false);
        this.colorByQualitySetting = addSetting(new BooleanSetting(
                "semantic_color", "Color by Connection Quality", "Color text green/amber/red based on latency ms", true
        ));
        this.showGraphSetting = addSetting(new BooleanSetting(
                "show_graph", "Show Ping Graph", "Render a live latency history graph below the ping counter", false
        ));
        for (int i = 0; i < GRAPH_SAMPLES; i++) samples[i] = 35;
        addAliases("latency", "pinggraph", "ms", "connection", "network");
    }

    public int getCurrentPingMs() {
        if (client == null || client.player == null || client.getNetworkHandler() == null || client.isInSingleplayer()) {
            return -1;
        }
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        return entry != null ? entry.getLatency() : -1;
    }

    @Override
    public void onTick() {
        tickDivider++;
        if (tickDivider >= 5) {
            tickDivider = 0;
            samples[cursor] = Math.max(0, getCurrentPingMs());
            cursor = (cursor + 1) % GRAPH_SAMPLES;
        }
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int ping = getCurrentPingMs();
        String text;
        int color = textColorSetting.getArgb();

        if (ping < 0) {
            text = preview ? "Ping: 32 ms" : "Ping: Local";
        } else {
            text = "Ping: " + ping + " ms";
            if (colorByQualitySetting.isEnabled()) {
                color = ping < 80 ? ThemeTokens.STATUS_SUCCESS : (ping < 160 ? ThemeTokens.STATUS_WARNING : ThemeTokens.STATUS_ERROR);
            }
        }

        int pad = paddingSetting.getIntValue();
        int graphExtraH = showGraphSetting.isEnabled() ? 16 : 0;
        int boxW = Math.max(showGraphSetting.isEnabled() ? (GRAPH_SAMPLES + pad * 2) : 0, client.textRenderer.getWidth(text) + pad * 2);
        int boxH = client.textRenderer.fontHeight + pad * 2 + graphExtraH;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(context, x, y, boxW, boxH, backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled());
        }

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, color);

        if (showGraphSetting.isEnabled()) {
            int gx = x + pad;
            int bottomY = y + boxH - pad;
            for (int i = 0; i < GRAPH_SAMPLES; i++) {
                int val = samples[(cursor + i) % GRAPH_SAMPLES];
                int h = Math.max(1, Math.min(13, Math.round((val / 200.0f) * 13.0f)));
                int barCol = val > 150 ? ThemeTokens.STATUS_ERROR : 0x88FFFFFF;
                context.fill(gx + i, bottomY - h, gx + i + 1, bottomY, barCol);
            }
        }
    }
}
