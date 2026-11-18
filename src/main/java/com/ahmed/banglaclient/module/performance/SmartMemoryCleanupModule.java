package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class SmartMemoryCleanupModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public SmartMemoryCleanupModule() {
        super("smart_memory_cleanup", "Smart Memory Cleanup", "Automatically triggers cleanup when heap exceeds 85%.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Auto Cleanup >85%", "Automatically triggers cleanup when heap exceeds 85%.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Skip During Combat", "Additional control for Smart Memory Cleanup", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Smart Memory Cleanup Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
