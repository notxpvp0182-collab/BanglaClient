package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.hud.CpsCounterModule;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.performance.PerformanceEngine;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import java.util.Locale;

public final class CpuUsageHudModule extends HudModule {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting intensity;

    public CpuUsageHudModule() {
        super("cpu_usage_hud", "CPU Usage HUD", "Displays live JVM/System CPU usage percentage.", ModuleCategory.HUD, 14.0f, 136.0f, 108.0f, 20.0f, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Show Thread Count", "Displays live JVM/System CPU usage percentage.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Compact Mode", "Secondary option for CPU Usage HUD", false));
        this.intensity = addSetting(new NumberSetting("refresh_rate", "Update Smoothness", "Update frequency & smoothing", 100.0, 20.0, 200.0, 10.0, "%"));
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        PerformanceEngine eng = PerformanceEngine.get();
        int lowFps = eng.getOnePercentLowFps();
        String ms = String.format(Locale.ROOT, "%.1f", eng.getCurrentFrameTimeMs());
        Runtime rt = Runtime.getRuntime();
        int ram = (int) (((rt.totalMemory() - rt.freeMemory()) * 100L) / Math.max(1L, rt.maxMemory()));
        int ent = eng.getLoadedEntityCount();
        String partStr = eng.getActiveParticleCountString();
        String gpuStr = eng.getFormattedGpuUsage();
        String cpuStr = eng.getFormattedCpuUsage();
        int lCps = BanglaClient.getInstance().getModuleManager().getModule(CpsCounterModule.class).map(CpsCounterModule::getLeftCps).orElse(0);
        int rCps = BanglaClient.getInstance().getModuleManager().getModule(CpsCounterModule.class).map(CpsCounterModule::getRightCps).orElse(0);
        float dist = (client != null && client.targetedEntity != null && client.player != null)
                ? (float) client.player.distanceTo(client.targetedEntity) : 0.0f;

        String text = "CPU: {cpu}%"
                .replace("{fps}", String.valueOf(lowFps))
                .replace("{ms}", ms)
                .replace("{gpu}%", gpuStr)
                .replace("{cpu}%", cpuStr)
                .replace("{ram}", String.valueOf(ram))
                .replace("{ent}", String.valueOf(ent))
                .replace("{part}", partStr)
                .replace("{l}", String.valueOf(lCps))
                .replace("{r}", String.valueOf(rCps))
                .replace("{hits}", String.valueOf(eng.getConfirmedHitsSession()))
                .replace("{dist}", String.format(Locale.ROOT, "%.2f", dist));

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
