package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class OptimizationLogModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public OptimizationLogModule() {
        super("optimization_log", "Optimization Log", "Records active optimizations and freed memory in client logs.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Log Freed RAM MB", "Records active optimizations and freed memory in client logs.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Show Optimization Toasts", "Additional control for Optimization Log", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Optimization Log Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
