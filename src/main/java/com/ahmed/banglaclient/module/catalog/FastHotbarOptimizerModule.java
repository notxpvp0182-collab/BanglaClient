package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class FastHotbarOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public FastHotbarOptimizerModule() {
        super("fast_hotbar_optimizer", "Fast Hotbar Optimizer", "Makes hotbar slot switching and key selection instantaneous.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Zero-Delay Slot Switch", "Makes hotbar slot switching and key selection instantaneous.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Instant Scroll Select", "Additional control for Fast Hotbar Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Fast Hotbar Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
