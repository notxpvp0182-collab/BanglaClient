package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class TotemOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public TotemOptimizerModule() {
        super("totem_optimizer", "Totem Optimizer", "Optimizes Totem pop particle burst and offhand tracking.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Reduce Pop Particles", "Optimizes Totem pop particle burst and offhand tracking.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Fast Offhand Sync", "Additional control for Totem Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Totem Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
