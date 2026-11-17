package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ElytraOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ElytraOptimizerModule() {
        super("elytra_optimizer", "Elytra Optimizer", "Smooths Elytra flight transition and wing rendering.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Instant Glide Sync", "Smooths Elytra flight transition and wing rendering.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Optimize Wing Model", "Additional control for Elytra Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Elytra Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
