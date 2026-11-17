package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class FastWorldLoadingModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public FastWorldLoadingModule() {
        super("fast_world_loading", "Fast World Loading", "Speeds up world entry and dimension switching.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Skip Spawn Terrain Wait", "Speeds up world entry and dimension switching.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Fast Portal Transition", "Additional control for Fast World Loading", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Fast World Loading Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
