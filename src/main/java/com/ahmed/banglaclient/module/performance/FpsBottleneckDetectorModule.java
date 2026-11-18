package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class FpsBottleneckDetectorModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public FpsBottleneckDetectorModule() {
        super("fps_bottleneck_detector", "FPS Bottleneck Detector", "Analyzes measured performance data and reports probable bottlenecks.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Alert on GC Pause", "Analyzes measured performance data and reports probable bottlenecks.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Show Bottleneck Source", "Additional control for FPS Bottleneck Detector", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "FPS Bottleneck Detector Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
