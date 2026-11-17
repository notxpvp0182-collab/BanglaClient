package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class WaterRenderingOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public WaterRenderingOptimizerModule() {
        super("water_rendering_optimizer", "Water Rendering Optimizer", "Optimizes water surface wave and flow vertex calculations.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Fast Fluid Vertices", "Optimizes water surface wave and flow vertex calculations.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Cull Submerged Faces", "Additional control for Water Rendering Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Water Rendering Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
