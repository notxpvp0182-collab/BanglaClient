package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class BackgroundTaskOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public BackgroundTaskOptimizerModule() {
        super("background_task_optimizer", "Background Task Optimizer", "Limits CPU usage of background pinging and skin downloads.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Throttle Background IO", "Limits CPU usage of background pinging and skin downloads.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Pause During PvP", "Additional control for Background Task Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Background Task Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
