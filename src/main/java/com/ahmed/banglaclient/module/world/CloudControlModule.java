package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class CloudControlModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public CloudControlModule() {
        super("cloud_control", "Cloud Control", "Enables, disables, or customizes cloud height and opacity.", ModuleCategory.WORLD, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Hide Clouds", "Enables, disables, or customizes cloud height and opacity.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Raise Cloud Height", "Additional control for Cloud Control", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Cloud Control Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
