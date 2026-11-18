package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ManualMemoryCleanupKeybindModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ManualMemoryCleanupKeybindModule() {
        super("manual_memory_cleanup_keybind", "Manual Memory Cleanup Keybind", "Press keybind to trigger safe memory cleanup.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Show Freed MB Toast", "Press keybind to trigger safe memory cleanup.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Safe GC Hint", "Additional control for Manual Memory Cleanup Keybind", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Manual Memory Cleanup Keybind Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
