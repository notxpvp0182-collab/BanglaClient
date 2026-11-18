package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ResetOptimizationModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ResetOptimizationModule() {
        super("reset_optimization", "Reset Optimization", "Resets all performance and visual optimization settings to default.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Restore Default Presets", "Resets all performance and visual optimization settings to default.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Keep Keybinds & Waypoints", "Additional control for Reset Optimization", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Reset Optimization Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
