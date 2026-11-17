package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class TransparentRenderOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public TransparentRenderOptimizerModule() {
        super("transparent_render_optimizer", "Transparent Render Optimizer", "Optimizes sorting and rendering of glass, ice, and water layers.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Fast Translucent Sort", "Optimizes sorting and rendering of glass, ice, and water layers.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Simplified Glass Pass", "Additional control for Transparent Render Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Transparent Render Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
