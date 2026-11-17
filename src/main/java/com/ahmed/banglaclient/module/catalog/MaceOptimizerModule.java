package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class MaceOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public MaceOptimizerModule() {
        super("mace_optimizer", "Mace Optimizer", "Optimizes Mace smash attack particles and fall-hit responsiveness.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Clean Smash Particles", "Optimizes Mace smash attack particles and fall-hit responsiveness.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Responsive Smash Input", "Additional control for Mace Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Mace Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
