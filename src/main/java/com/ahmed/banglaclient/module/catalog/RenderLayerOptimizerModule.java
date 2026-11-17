package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class RenderLayerOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public RenderLayerOptimizerModule() {
        super("render_layer_optimizer", "Render Layer Optimizer", "Batches cutout, solid, and translucent render layers efficiently.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Merge Cutout Layers", "Batches cutout, solid, and translucent render layers efficiently.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Minimize State Changes", "Additional control for Render Layer Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Render Layer Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
