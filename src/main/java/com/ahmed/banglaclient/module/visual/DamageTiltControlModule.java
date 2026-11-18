package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class DamageTiltControlModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public DamageTiltControlModule() {
        super("damage_tilt_control", "Damage Tilt Control", "Customizes or disables red camera tilt when hurt.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Directional Tilt", "Customizes or disables red camera tilt when hurt.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Zero Tilt Mode", "Additional control for Damage Tilt Control", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Damage Tilt Control Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
