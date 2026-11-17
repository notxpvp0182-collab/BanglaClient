package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

public final class TntFuseHudStandaloneModule extends HudModule {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting intensity;

    public TntFuseHudStandaloneModule() {
        super("tnt_fuse_hud", "TNT Fuse HUD", "Displays exact countdown timer of nearby primed TNT.", ModuleCategory.COMBAT, 14.0f, 112.0f, 108.0f, 20.0f, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Danger Color <1.0s", "Displays exact countdown timer of nearby primed TNT.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Show Distance", "Secondary option for TNT Fuse HUD", false));
        this.intensity = addSetting(new NumberSetting("refresh_rate", "Update Smoothness", "Update frequency & smoothing", 100.0, 20.0, 200.0, 10.0, "%"));
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        var eng = com.ahmed.banglaclient.performance.PerformanceEngine.get();
        int fps = client != null ? Math.max(0, client.getCurrentFps()) : 0;
        int lowFps = eng.getOnePercentLowFps();
        String ms = String.format(java.util.Locale.ROOT, "%.1f", eng.getCurrentFrameTimeMs());
        Runtime rt = Runtime.getRuntime();
        int ram = (int) (((rt.totalMemory() - rt.freeMemory()) * 100L) / Math.max(1L, rt.maxMemory()));
        int ent = eng.getLoadedEntityCount();
        String partStr = eng.getActiveParticleCountString();
        String gpuStr = eng.getFormattedGpuUsage();
        String cpuStr = eng.getFormattedCpuUsage();
        int lCps = com.ahmed.banglaclient.BanglaClient.getInstance().getModuleManager()
                .getModule(com.ahmed.banglaclient.module.hud.CpsCounterModule.class)
                .map(com.ahmed.banglaclient.module.hud.CpsCounterModule::getLeftCps).orElse(0);
        int rCps = com.ahmed.banglaclient.BanglaClient.getInstance().getModuleManager()
                .getModule(com.ahmed.banglaclient.module.hud.CpsCounterModule.class)
                .map(com.ahmed.banglaclient.module.hud.CpsCounterModule::getRightCps).orElse(0);
        int pingVal = com.ahmed.banglaclient.BanglaClient.getInstance().getModuleManager()
                .getModule(com.ahmed.banglaclient.module.hud.PingDisplayModule.class)
                .map(com.ahmed.banglaclient.module.hud.PingDisplayModule::getCurrentPingMs).orElse(-1);
        float dist = (client != null && client.targetedEntity != null && client.player != null)
                ? (float) client.player.distanceTo(client.targetedEntity) : 0.0f;

        String text = "TNT Fuse: --"
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
                .replace("{dist}", String.format(java.util.Locale.ROOT, "%.2f", dist))
                .replace("{ping} ms", pingVal >= 0 ? (pingVal + " ms") : "Local");

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
