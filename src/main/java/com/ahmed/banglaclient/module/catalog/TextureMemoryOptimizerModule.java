package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class TextureMemoryOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public TextureMemoryOptimizerModule() {
        super("texture_memory_optimizer", "Texture Memory Optimizer", "Optimizes GPU texture atlas memory footprint.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Evict Unused Previews", "Optimizes GPU texture atlas memory footprint.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Compress UI Atlases", "Additional control for Texture Memory Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Texture Memory Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
