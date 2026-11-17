package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class TotemResizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public TotemResizerModule() {
        super("totem_resizer", "Totem Resizer", "Customizes Totem of Undying size by exact percentage.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Scale Offhand Totem", "Customizes Totem of Undying size by exact percentage.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Scale Mainhand Totem", "Additional control for Totem Resizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Totem Resizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
