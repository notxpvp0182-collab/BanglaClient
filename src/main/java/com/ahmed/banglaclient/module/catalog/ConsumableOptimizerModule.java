package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ConsumableOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ConsumableOptimizerModule() {
        super("consumable_optimizer", "Consumable Optimizer", "Cleans up food eating crumb particles and hand jitter.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Reduce Crumb Particles", "Cleans up food eating crumb particles and hand jitter.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Smooth Eat Animation", "Additional control for Consumable Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Consumable Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
