package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class RamUsageOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public RamUsageOptimizerModule() {
        super("ram_usage_optimizer", "RAM Usage Optimizer", "Reduces object allocations and memory churn on the render thread.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Pool Temporary Buffers", "Reduces object allocations and memory churn on the render thread.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Trim Idle Collections", "Additional control for RAM Usage Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "RAM Usage Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
