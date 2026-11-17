package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class CrossbowOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public CrossbowOptimizerModule() {
        super("crossbow_optimizer", "Crossbow Optimizer", "Streamlines crossbow charge animation and firework projectile load.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Fast Charge Animation", "Streamlines crossbow charge animation and firework projectile load.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Limit Firework Trail", "Additional control for Crossbow Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Crossbow Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
