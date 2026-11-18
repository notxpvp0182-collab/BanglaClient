package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ThermalModeModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ThermalModeModule() {
        super("thermal_mode", "Thermal Mode", "Reduces rendering workload during long sessions to prevent overheating.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Throttle Heavy Passes", "Reduces rendering workload during long sessions to prevent overheating.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Cool-Down Frame Pacing", "Additional control for Thermal Mode", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Thermal Mode Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
