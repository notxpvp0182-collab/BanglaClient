package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class SafeModeModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public SafeModeModule() {
        super("safe_mode", "Safe Mode", "Disables experimental shaders and heavy hooks for 100% stability.", ModuleCategory.UTILITY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Bypass Custom Shaders", "Disables experimental shaders and heavy hooks for 100% stability.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Reset Broken Positions", "Additional control for Safe Mode", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Safe Mode Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
