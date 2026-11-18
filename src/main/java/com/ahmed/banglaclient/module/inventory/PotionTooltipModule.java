package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class PotionTooltipModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public PotionTooltipModule() {
        super("potion_tooltip", "Potion Tooltip", "Displays exact potion duration and effect strength in tooltips.", ModuleCategory.INVENTORY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Show Exact Seconds", "Displays exact potion duration and effect strength in tooltips.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Color by Effect Type", "Additional control for Potion Tooltip", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Potion Tooltip Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
