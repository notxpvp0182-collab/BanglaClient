package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class StartupCacheModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public StartupCacheModule() {
        super("startup_cache", "Startup Cache", "Caches compiled glyph tables and icons for faster startup.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Cache Font Atlas", "Caches compiled glyph tables and icons for faster startup.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Cache Server Flags", "Additional control for Startup Cache", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Startup Cache Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
