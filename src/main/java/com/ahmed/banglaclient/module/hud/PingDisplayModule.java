package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

/**
 * Module 06: Ping Display
 * Displays live server latency in milliseconds when connected to multiplayer,
 * and shows a clear Singleplayer state in local worlds.
 */
public final class PingDisplayModule extends HudModule {
    private final BooleanSetting colorByQualitySetting;

    public PingDisplayModule() {
        super(
                "ping_display",
                "Ping Display",
                "Displays live multiplayer latency in milliseconds or Singleplayer status.",
                ModuleCategory.HUD,
                80.0f,
                10.0f,
                72.0f,
                20.0f,
                false
        );
        this.colorByQualitySetting = addSetting(new BooleanSetting(
                "semantic_color", "Color by Connection Quality", "Color text green/amber/red based on latency ms", true
        ));
        addAliases("latency", "ms", "connection", "network");
    }

    public int getCurrentPingMs() {
        if (client == null || client.player == null || client.getNetworkHandler() == null) {
            return -1;
        }
        if (client.isInSingleplayer()) {
            return -1;
        }
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        return entry != null ? entry.getLatency() : -1;
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int ping = getCurrentPingMs();
        String text;
        int color = textColorSetting.getArgb();

        if (ping < 0) {
            text = preview ? "Ping: 32 ms" : "Ping: Singleplayer";
        } else {
            text = "Ping: " + ping + " ms";
            if (colorByQualitySetting.isEnabled()) {
                if (ping < 80) {
                    color = ThemeTokens.STATUS_SUCCESS;
                } else if (ping < 160) {
                    color = ThemeTokens.STATUS_WARNING;
                } else {
                    color = ThemeTokens.STATUS_ERROR;
                }
            }
        }

        int pad = paddingSetting.getIntValue();
        int boxW = client.textRenderer.getWidth(text) + pad * 2;
        int boxH = client.textRenderer.fontHeight + pad * 2;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, color);
    }
}
