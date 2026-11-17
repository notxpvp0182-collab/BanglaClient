package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class SmallShovelModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public SmallShovelModule() {
        super("small_shovel", "Small Shovel", "Reduces held Shovel size in first-person view.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Apply to All Shovels", "Reduces held Shovel size in first-person view.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Lower Shovel Position", "Additional control for Small Shovel", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Small Shovel Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
