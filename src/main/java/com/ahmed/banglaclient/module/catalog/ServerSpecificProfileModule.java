package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ServerSpecificProfileModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ServerSpecificProfileModule() {
        super("server_specific_profile", "Server-Specific Profile", "Automatically loads a dedicated profile for each multiplayer server.", ModuleCategory.UTILITY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Auto Switch by Server IP", "Automatically loads a dedicated profile for each multiplayer server.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Save Per-Server Keybinds", "Additional control for Server-Specific Profile", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Server-Specific Profile Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
