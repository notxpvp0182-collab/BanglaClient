package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ModelCacheOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ModelCacheOptimizerModule() {
        super("model_cache_optimizer", "Model Cache Optimizer", "Caches baked item and block models to prevent repeated allocation.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Cache Baked Models", "Caches baked item and block models to prevent repeated allocation.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Fast ItemQuad Lookup", "Additional control for Model Cache Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Model Cache Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
