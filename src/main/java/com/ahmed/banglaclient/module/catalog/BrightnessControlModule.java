package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class BrightnessControlModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public BrightnessControlModule() {
        super("brightness_control", "Brightness Control", "Fine-tunes world gamma brightness from 0% to 1000%.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Max Gamma 1000%", "Fine-tunes world gamma brightness from 0% to 1000%.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Smooth Night Transition", "Additional control for Brightness Control", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Brightness Control Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
