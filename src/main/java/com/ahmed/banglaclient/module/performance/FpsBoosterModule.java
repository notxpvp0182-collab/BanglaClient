package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.EnumSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import net.minecraft.client.option.CloudRenderMode;

public final class FpsBoosterModule extends Module {
    public enum BoostMode {
        HIGH_PERFORMANCE("High Performance"),
        ULTRA_COMPETITIVE("Ultra Competitive"),
        EXTREME_MAX_FPS("Extreme Max FPS");

        private final String label;
        BoostMode(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    private final EnumSetting<BoostMode> boostModeSetting;
    private final BooleanSetting maxCpuGpuPrioritySetting;
    private final BooleanSetting uncapFramerateSetting;
    private final BooleanSetting disableEntityShadowsSetting;
    private final NumberSetting entityDistanceScaleSetting;
    private final BooleanSetting optimizeBiomeAndCloudsSetting;

    private int savedMaxFps = -1;
    private Boolean savedVsync = null;
    private Boolean savedEntityShadows = null;
    private Double savedEntityDistance = null;
    private Integer savedBiomeBlend = null;
    private CloudRenderMode savedClouds = null;

    public FpsBoosterModule() {
        super("fps_booster", "FPS Booster",
                "Maximizes game performance and FPS by boosting CPU/GPU thread priority and optimizing render pipelines.",
                ModuleCategory.PERFORMANCE, -1, true);
        this.boostModeSetting = addSetting(new EnumSetting<>("boost_mode", "Boost Profile", "Optimization aggressiveness level", BoostMode.ULTRA_COMPETITIVE));
        this.maxCpuGpuPrioritySetting = addSetting(new BooleanSetting("high_priority", "Max CPU & Render Thread Priority", "Elevate render thread priority for maximum frame output", true));
        this.uncapFramerateSetting = addSetting(new BooleanSetting("uncap_fps", "Uncap FPS (Unlimited)", "Remove 60/120 FPS cap and disable VSync delay", true));
        this.disableEntityShadowsSetting = addSetting(new BooleanSetting("no_shadows", "Disable Entity Shadows", "Skip circular entity shadow passes for higher FPS", true));
        this.entityDistanceScaleSetting = addSetting(new NumberSetting("entity_scale", "Entity Render Distance %", "Scale entity render distance for massive FPS gains", 70.0, 50.0, 100.0, 5.0, "%"));
        this.optimizeBiomeAndCloudsSetting = addSetting(new BooleanSetting("fast_env", "Fast Biome Blend & Clouds", "Reduce biome color blend radius and cloud overhead", true));
        addAliases("fpsboost", "boostfps", "maxfps", "highperformance", "gpuoptimizer", "cpuoptimizer");
    }

    @Override
    protected void onEnable() {
        applyHardwareAndRenderBoost();
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
            BanglaClient.getInstance().getNotificationManager().pushSuccess("FPS Booster Active", "Mode: " + boostModeSetting.getValue());
        }
    }

    @Override
    protected void onDisable() {
        restoreOriginalOptions();
        try { Thread.currentThread().setPriority(Thread.NORM_PRIORITY); } catch (Throwable ignored) {}
    }

    @Override
    public void onTick() {
        if (maxCpuGpuPrioritySetting.isEnabled()) {
            try {
                if (Thread.currentThread().getPriority() < Thread.MAX_PRIORITY) {
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                }
            } catch (Throwable ignored) {}
        }
        applyHardwareAndRenderBoost();
    }

    private void applyHardwareAndRenderBoost() {
        if (client == null || client.options == null) return;
        try {
            if (uncapFramerateSetting.isEnabled()) {
                if (savedMaxFps < 0) savedMaxFps = client.options.getMaxFps().getValue();
                if (savedVsync == null) savedVsync = client.options.getEnableVsync().getValue();
                if (client.options.getMaxFps().getValue() < 260) client.options.getMaxFps().setValue(260);
                if (Boolean.TRUE.equals(client.options.getEnableVsync().getValue())) client.options.getEnableVsync().setValue(false);
            }
            if (disableEntityShadowsSetting.isEnabled()) {
                if (savedEntityShadows == null) savedEntityShadows = client.options.getEntityShadows().getValue();
                client.options.getEntityShadows().setValue(false);
            }
            if (savedEntityDistance == null) savedEntityDistance = client.options.getEntityDistanceScaling().getValue();
            double targetDist = Math.max(0.5, entityDistanceScaleSetting.getValue() / 100.0);
            if (Math.abs(client.options.getEntityDistanceScaling().getValue() - targetDist) > 0.04) {
                client.options.getEntityDistanceScaling().setValue(targetDist);
            }
            if (optimizeBiomeAndCloudsSetting.isEnabled()) {
                if (savedBiomeBlend == null) savedBiomeBlend = client.options.getBiomeBlendRadius().getValue();
                if (client.options.getBiomeBlendRadius().getValue() > 1) client.options.getBiomeBlendRadius().setValue(1);
                if (boostModeSetting.getValue() == BoostMode.EXTREME_MAX_FPS) {
                    if (savedClouds == null) savedClouds = client.options.getCloudRenderMode().getValue();
                    client.options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
                }
            }
        } catch (Throwable ignored) {}
    }

    private void restoreOriginalOptions() {
        if (client == null || client.options == null) return;
        try {
            if (savedMaxFps > 0) { client.options.getMaxFps().setValue(savedMaxFps); savedMaxFps = -1; }
            if (savedVsync != null) { client.options.getEnableVsync().setValue(savedVsync); savedVsync = null; }
            if (savedEntityShadows != null) { client.options.getEntityShadows().setValue(savedEntityShadows); savedEntityShadows = null; }
            if (savedEntityDistance != null) { client.options.getEntityDistanceScaling().setValue(savedEntityDistance); savedEntityDistance = null; }
            if (savedBiomeBlend != null) { client.options.getBiomeBlendRadius().setValue(savedBiomeBlend); savedBiomeBlend = null; }
            if (savedClouds != null) { client.options.getCloudRenderMode().setValue(savedClouds); savedClouds = null; }
        } catch (Throwable ignored) {}
    }
}
