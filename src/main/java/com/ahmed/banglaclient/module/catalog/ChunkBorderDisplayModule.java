package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ChunkBorderDisplayModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ChunkBorderDisplayModule() {
        super("chunk_border_display", "Chunk Border Display", "Displays current 16x16 chunk boundaries and subchunk grid.", ModuleCategory.WORLD, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Show 16x16 Corners", "Displays current 16x16 chunk boundaries and subchunk grid.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Show Subchunk Y Grid", "Additional control for Chunk Border Display", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Chunk Border Display Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
