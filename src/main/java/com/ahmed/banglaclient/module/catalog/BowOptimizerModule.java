package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class BowOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public BowOptimizerModule() {
        super("bow_optimizer", "Bow Optimizer", "Optimizes bow pull animation and critical arrow trail particles.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Smooth Pull Interpolation", "Optimizes bow pull animation and critical arrow trail particles.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Reduce Crit Trail", "Additional control for Bow Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Bow Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
