package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class BatterySaverModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public BatterySaverModule() {
        super("battery_saver", "Battery Saver", "Reduces CPU/GPU power draw for laptops and mobile devices.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Cap at 60 FPS", "Reduces CPU/GPU power draw for laptops and mobile devices.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Reduce Background Polling", "Additional control for Battery Saver", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Battery Saver Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
