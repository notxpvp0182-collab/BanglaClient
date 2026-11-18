package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class LowPowerModeModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public LowPowerModeModule() {
        super("low_power_mode", "Low Power Mode", "Ultra-lightweight rendering mode for low-power devices.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Disable Blur & Shadows", "Ultra-lightweight rendering mode for low-power devices.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Fast 2D UI Mode", "Additional control for Low Power Mode", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Low Power Mode Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
