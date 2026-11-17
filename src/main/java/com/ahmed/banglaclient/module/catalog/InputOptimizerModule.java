package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class InputOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public InputOptimizerModule() {
        super("input_optimizer", "Input Optimizer", "Reduces keyboard and mouse input latency for instant response.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Raw Mouse Polling", "Reduces keyboard and mouse input latency for instant response.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Zero-Tick Key Buffer", "Additional control for Input Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Input Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
