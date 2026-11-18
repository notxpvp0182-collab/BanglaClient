package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class PerformanceHistoryModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public PerformanceHistoryModule() {
        super("performance_history", "Performance History", "Logs session FPS stability and memory trends.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Track 5-Minute Trend", "Logs session FPS stability and memory trends.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Highlight Lag Spikes", "Additional control for Performance History", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Performance History Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
