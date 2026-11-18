package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class CompatibilityModeModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public CompatibilityModeModule() {
        super("compatibility_mode", "Compatibility Mode", "Ensures seamless compatibility with Sodium, Lithium, Iris, and ModMenu.", ModuleCategory.UTILITY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Sodium Render Bridge", "Ensures seamless compatibility with Sodium, Lithium, Iris, and ModMenu.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Safe Mixin Fallback", "Additional control for Compatibility Mode", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Compatibility Mode Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
