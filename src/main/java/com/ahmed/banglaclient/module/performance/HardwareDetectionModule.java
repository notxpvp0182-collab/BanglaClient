package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class HardwareDetectionModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public HardwareDetectionModule() {
        super("hardware_detection", "Hardware Detection", "Detects CPU cores, GPU renderer, and RAM to tune thread pools.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Auto Tune Worker Threads", "Detects CPU cores, GPU renderer, and RAM to tune thread pools.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Detect Mobile/Integrated GPU", "Additional control for Hardware Detection", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Hardware Detection Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
