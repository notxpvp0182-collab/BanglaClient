package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class MemoryCleanupModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public MemoryCleanupModule() {
        super("memory_cleanup", "Memory Cleanup", "Cleans unused client-side memory allocations.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Clean on World Leave", "Cleans unused client-side memory allocations.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Purge Stale Caches", "Additional control for Memory Cleanup", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Memory Cleanup Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
