package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class SmallFoodModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public SmallFoodModule() {
        super("small_food", "Small Food", "Reduces Golden Apple and food size while eating.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Small Golden Apples", "Reduces Golden Apple and food size while eating.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Clear Eating View", "Additional control for Small Food", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Small Food Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
