package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class NametagDistanceControlModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public NametagDistanceControlModule() {
        super("nametag_distance_control", "Nametag Distance Control", "Controls the maximum distance at which entity nametags render.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Render Up to 64m", "Controls the maximum distance at which entity nametags render.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Scale Distant Tags", "Additional control for Nametag Distance Control", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Nametag Distance Control Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
