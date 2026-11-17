package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class StartupOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public StartupOptimizerModule() {
        super("startup_optimizer", "Startup Optimizer", "Defers non-essential caches for faster game launch.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Lazy Asset Init", "Defers non-essential caches for faster game launch.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Fast Font Warmup", "Additional control for Startup Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Startup Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
