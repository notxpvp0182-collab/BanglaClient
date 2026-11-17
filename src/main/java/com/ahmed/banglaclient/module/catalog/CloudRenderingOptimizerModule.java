package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class CloudRenderingOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public CloudRenderingOptimizerModule() {
        super("cloud_rendering_optimizer", "Cloud Rendering Optimizer", "Caches cloud mesh geometry to eliminate per-frame cloud rebuilds.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Static Cloud Mesh Cache", "Caches cloud mesh geometry to eliminate per-frame cloud rebuilds.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Fast Cloud Culling", "Additional control for Cloud Rendering Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Cloud Rendering Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
