package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ChunkMeshCacheModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ChunkMeshCacheModule() {
        super("chunk_mesh_cache", "Chunk Mesh Cache", "Caches generated chunk geometry to avoid rebuilding unchanged chunks.", ModuleCategory.PERFORMANCE, -1, true);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Enable Mesh Cache", "Caches generated chunk geometry to avoid rebuilding unchanged chunks.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Reuse Static Geometry", "Additional control for Chunk Mesh Cache", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Chunk Mesh Cache Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
