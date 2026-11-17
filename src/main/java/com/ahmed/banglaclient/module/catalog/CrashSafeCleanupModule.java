package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class CrashSafeCleanupModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public CrashSafeCleanupModule() {
        super("crash_safe_cleanup", "Crash-Safe Cleanup", "Safely flushes all configs and releases GPU buffers on shutdown.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Atomic Config Save", "Safely flushes all configs and releases GPU buffers on shutdown.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Safe Buffer Release", "Additional control for Crash-Safe Cleanup", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Crash-Safe Cleanup Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
