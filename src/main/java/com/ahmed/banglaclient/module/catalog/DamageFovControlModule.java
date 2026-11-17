package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class DamageFovControlModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public DamageFovControlModule() {
        super("damage_fov_control", "Damage FOV Control", "Prevents sudden FOV jolts when taking damage.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Lock FOV on Hit", "Prevents sudden FOV jolts when taking damage.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Smooth Recovery", "Additional control for Damage FOV Control", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Damage FOV Control Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
