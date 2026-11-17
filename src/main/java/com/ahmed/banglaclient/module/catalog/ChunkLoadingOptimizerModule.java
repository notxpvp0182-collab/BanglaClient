package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ChunkLoadingOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ChunkLoadingOptimizerModule() {
        super("chunk_loading_optimizer", "Chunk Loading Optimizer", "Optimizes chunk loading and rebuild scheduling.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Prioritize Near Chunks", "Optimizes chunk loading and rebuild scheduling.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Async Rebuild Queue", "Additional control for Chunk Loading Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Chunk Loading Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
