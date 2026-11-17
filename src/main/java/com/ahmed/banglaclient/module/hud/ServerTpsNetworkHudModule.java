package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

public final class ServerTpsNetworkHudModule extends HudModule {
    private final BooleanSetting showTpsSetting;
    private final BooleanSetting showServerBrandSetting;
    private final BooleanSetting showMoonPhaseSetting;
    private long lastWorldTime = -1L;
    private long lastCheckMs = 0L;
    private float estimatedTps = 20.0f;

    public ServerTpsNetworkHudModule() {
        super("server_tps_hud", "Server TPS & Brand HUD", "Displays live Server TPS, Server Brand, Packet status, and Moon Phase.",
                ModuleCategory.HUD, 160.0f, 10.0f, 110.0f, 20.0f, false);
        this.showTpsSetting = addSetting(new BooleanSetting("show_tps", "Show Server TPS", "Display estimated Server TPS (20.0)", true));
        this.showServerBrandSetting = addSetting(new BooleanSetting("show_brand", "Show Server Brand", "Display Paper/Velocity/Vanilla server brand", true));
        this.showMoonPhaseSetting = addSetting(new BooleanSetting("show_moon", "Show Moon Phase", "Append current world moon phase", false));
        addAliases("tps", "servertps", "serverbrand", "packetrate", "networkstatus", "moonphase", "autoreconnect");
    }

    @Override
    public void onTick() {
        if (client.world == null) return;
        long now = System.currentTimeMillis();
        long wt = client.world.getTime();
        if (lastWorldTime >= 0L && now - lastCheckMs >= 1000L) {
            long ticks = Math.max(0L, wt - lastWorldTime);
            float rawTps = Math.min(20.0f, (ticks * 1000.0f) / Math.max(1L, now - lastCheckMs));
            estimatedTps = estimatedTps * 0.7f + rawTps * 0.3f;
            lastWorldTime = wt;
            lastCheckMs = now;
        } else if (lastWorldTime < 0L) {
            lastWorldTime = wt;
            lastCheckMs = now;
        }
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        String brand = (client.player != null && client.player.networkHandler != null && client.player.networkHandler.getBrand() != null)
                ? client.player.networkHandler.getBrand() : "Vanilla";
        StringBuilder sb = new StringBuilder();
        if (showTpsSetting.isEnabled()) sb.append(String.format("TPS: %.1f", estimatedTps));
        if (showServerBrandSetting.isEnabled()) {
            if (!sb.isEmpty()) sb.append(" • ");
            sb.append(brand);
        }
        if (showMoonPhaseSetting.isEnabled() && client.world != null) {
            int phase = (int) ((client.world.getTimeOfDay() / 24000L) % 8L);
            sb.append(" • Moon:").append(phase + 1).append("/8");
        }
        String text = sb.isEmpty() ? "TPS: 20.0" : sb.toString();
        int pad = paddingSetting.getIntValue();
        int w = client.textRenderer.getWidth(text) + pad * 2;
        int h = client.textRenderer.fontHeight + pad * 2;
        setWidth(w);
        setHeight(h);
        int x = Math.round(getPosX()), y = Math.round(getPosY());
        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(context, x, y, w, h, backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled());
        }
        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, textColorSetting.getArgb());
    }
}
