package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ProfileManagerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ProfileManagerModule() {
        super("profile_manager", "Profile Manager", "Create, save, and switch between custom client configurations.", ModuleCategory.UTILITY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Auto Save Active Profile", "Create, save, and switch between custom client configurations.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Sync HUD Layout", "Additional control for Profile Manager", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Profile Manager Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
