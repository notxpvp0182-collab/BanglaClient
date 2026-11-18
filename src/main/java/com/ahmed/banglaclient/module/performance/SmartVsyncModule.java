package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class SmartVsyncModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public SmartVsyncModule() {
        super("smart_vsync", "Smart VSync", "Eliminates screen tearing only when FPS exceeds refresh rate.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Adaptive Tear Control", "Eliminates screen tearing only when FPS exceeds refresh rate.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Low-Latency Buffer", "Additional control for Smart VSync", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Smart VSync Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
