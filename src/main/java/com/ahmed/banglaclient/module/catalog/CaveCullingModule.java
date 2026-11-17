package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class CaveCullingModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public CaveCullingModule() {
        super("cave_culling", "Cave Culling", "Aggressively culls hidden underground caves and occluded subchunks.", ModuleCategory.PERFORMANCE, -1, true);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Underground Occlusion", "Aggressively culls hidden underground caves and occluded subchunks.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Seal Hidden Caves", "Additional control for Cave Culling", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Cave Culling Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
