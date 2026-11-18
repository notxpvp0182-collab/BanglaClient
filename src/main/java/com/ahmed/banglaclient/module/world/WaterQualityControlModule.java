package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class WaterQualityControlModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public WaterQualityControlModule() {
        super("water_quality_control", "Water Quality Control", "Adjusts water surface transparency and biome tint blending.", ModuleCategory.WORLD, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Crystal Surface", "Adjusts water surface transparency and biome tint blending.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Smooth Biome Blend", "Additional control for Water Quality Control", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Water Quality Control Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
