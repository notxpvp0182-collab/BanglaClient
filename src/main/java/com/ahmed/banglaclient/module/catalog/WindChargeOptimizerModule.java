package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class WindChargeOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public WindChargeOptimizerModule() {
        super("wind_charge_optimizer", "Wind Charge Optimizer", "Optimizes Wind Charge burst particles and throw responsiveness.", ModuleCategory.COMBAT, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Clean Wind Burst FX", "Optimizes Wind Charge burst particles and throw responsiveness.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Fast Wind Throw", "Additional control for Wind Charge Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Wind Charge Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
