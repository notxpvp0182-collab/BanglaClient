package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ChunkUpdateBatchingModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ChunkUpdateBatchingModule() {
        super("chunk_update_batching", "Chunk Update Batching", "Batches multiple block/chunk updates into a single mesh pass.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Batch Multi-Block Updates", "Batches multiple block/chunk updates into a single mesh pass.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Defer Far Updates", "Additional control for Chunk Update Batching", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Chunk Update Batching Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
