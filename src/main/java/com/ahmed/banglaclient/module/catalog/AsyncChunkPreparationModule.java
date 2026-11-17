package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class AsyncChunkPreparationModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public AsyncChunkPreparationModule() {
        super("async_chunk_preparation", "Async Chunk Preparation", "Prepares chunk light and biome data on background threads.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Parallel Chunk Prep", "Prepares chunk light and biome data on background threads.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Fast Light Sync", "Additional control for Async Chunk Preparation", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Async Chunk Preparation Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
