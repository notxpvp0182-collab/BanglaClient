package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class CpuThreadOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public CpuThreadOptimizerModule() {
        super("cpu_thread_optimizer", "CPU Thread Optimizer", "Balances worker thread priorities for smoother frame times.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Prioritize Render Thread", "Balances worker thread priorities for smoother frame times.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Optimize Worker Pool", "Additional control for CPU Thread Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "CPU Thread Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
