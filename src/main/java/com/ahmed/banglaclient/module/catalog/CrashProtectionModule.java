package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class CrashProtectionModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public CrashProtectionModule() {
        super("crash_protection", "Crash Protection", "Catches module tick/render exceptions to prevent game crashes.", ModuleCategory.UTILITY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Isolate Module Errors", "Catches module tick/render exceptions to prevent game crashes.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Auto Recover Config", "Additional control for Crash Protection", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Crash Protection Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
