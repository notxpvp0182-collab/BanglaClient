package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class SurvivalProfileModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public SurvivalProfileModule() {
        super("survival_profile", "Survival Profile", "One-click preset enabling Coordinates, Biome, Clock, Waypoints & Light Level.", ModuleCategory.WORLD, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Enable Exploration HUDs", "One-click preset enabling Coordinates, Biome, Clock, Waypoints & Light Level.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Enable Shulker Preview", "Additional control for Survival Profile", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Survival Profile Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
