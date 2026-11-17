package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class AutoSprintModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public AutoSprintModule() {
        super("auto_sprint", "Auto Sprint", "Automatically sprints whenever moving forward.", ModuleCategory.MOVEMENT, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Omni-Directional", "Automatically sprints whenever moving forward.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Sprint in Water", "Additional control for Auto Sprint", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Auto Sprint Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
