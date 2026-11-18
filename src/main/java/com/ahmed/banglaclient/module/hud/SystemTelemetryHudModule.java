package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.performance.PerformanceEngine;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

public final class SystemTelemetryHudModule extends HudModule {
    private final BooleanSetting showCpuSetting;
    private final BooleanSetting showGpuSetting;
    private final BooleanSetting showEntitiesSetting;
    private final BooleanSetting showParticlesSetting;

    public SystemTelemetryHudModule() {
        super("system_telemetry_hud", "CPU / GPU / Entity / Particle HUD",
                "Displays real JMX CPU load, GPU status, loaded Entity count, and active Particle count.",
                ModuleCategory.HUD, 10.0f, 58.0f, 138.0f, 20.0f, false);
        this.showCpuSetting = addSetting(new BooleanSetting("show_cpu", "Show CPU Usage", "Display real JVM/OS CPU usage or Unavailable", true));
        this.showGpuSetting = addSetting(new BooleanSetting("show_gpu", "Show GPU Usage", "Display GPU usage or Unavailable", false));
        this.showEntitiesSetting = addSetting(new BooleanSetting("show_entities", "Show Entity Counter", "Display loaded world entity count", true));
        this.showParticlesSetting = addSetting(new BooleanSetting("show_particles", "Show Particle Counter", "Display active particle count", true));
        addAliases("cpuusage", "gpuusage", "entitycounter", "particlecounter", "performancedashboard");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        PerformanceEngine eng = PerformanceEngine.get();
        StringBuilder sb = new StringBuilder();
        if (showCpuSetting.isEnabled()) sb.append("CPU: ").append(eng.getFormattedCpuUsage());
        if (showGpuSetting.isEnabled()) {
            if (!sb.isEmpty()) sb.append(" • ");
            sb.append("GPU: ").append(eng.getFormattedGpuUsage());
        }
        if (showEntitiesSetting.isEnabled()) {
            if (!sb.isEmpty()) sb.append(" • ");
            sb.append("E: ").append(eng.getLoadedEntityCount());
        }
        if (showParticlesSetting.isEnabled()) {
            if (!sb.isEmpty()) sb.append(" • ");
            sb.append("P: ").append(eng.getActiveParticleCountString());
        }
        String text = sb.isEmpty() ? ("E: " + eng.getLoadedEntityCount()) : sb.toString();
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
