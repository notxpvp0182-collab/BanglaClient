package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class DeathWaypointModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public DeathWaypointModule() {
        super("death_waypoint", "Death Waypoint", "Automatically creates a waypoint beacon at your death location.", ModuleCategory.WORLD, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Auto Create on Death", "Automatically creates a waypoint beacon at your death location.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Clear on Arrival", "Additional control for Death Waypoint", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Death Waypoint Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
