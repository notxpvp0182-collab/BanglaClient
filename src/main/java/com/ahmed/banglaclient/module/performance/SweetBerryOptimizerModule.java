package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class SweetBerryOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public SweetBerryOptimizerModule() {
        super("sweet_berry_optimizer", "Sweet Berry Optimizer", "Optimizes sweet berry bush rendering and interaction.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Fast Bush Cutout", "Optimizes sweet berry bush rendering and interaction.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Prevent Accidental Slow", "Additional control for Sweet Berry Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Sweet Berry Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
