package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class TargetFpsModeModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public TargetFpsModeModule() {
        super("target_fps_mode", "Target FPS Mode", "Dynamically scales visual workload to stay above your target FPS.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Target 120+ FPS", "Dynamically scales visual workload to stay above your target FPS.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Prioritize Frame Stability", "Additional control for Target FPS Mode", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Target FPS Mode Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
