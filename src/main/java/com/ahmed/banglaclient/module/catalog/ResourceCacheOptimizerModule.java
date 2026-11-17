package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ResourceCacheOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ResourceCacheOptimizerModule() {
        super("resource_cache_optimizer", "Resource Cache Optimizer", "Caches resource pack lookups to speed up asset access.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Fast Identifier Cache", "Caches resource pack lookups to speed up asset access.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Skip Missing Lookups", "Additional control for Resource Cache Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Resource Cache Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
