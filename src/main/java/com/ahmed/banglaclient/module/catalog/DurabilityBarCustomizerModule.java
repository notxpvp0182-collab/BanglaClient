package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class DurabilityBarCustomizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public DurabilityBarCustomizerModule() {
        super("durability_bar_customizer", "Durability Bar Customizer", "Customizes item durability bar colors and gradient.", ModuleCategory.INVENTORY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Smooth RGB Gradient", "Customizes item durability bar colors and gradient.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Always Show Bar", "Additional control for Durability Bar Customizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Durability Bar Customizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
