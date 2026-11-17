package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class RenderThreadOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public RenderThreadOptimizerModule() {
        super("render_thread_optimizer", "Render Thread Optimizer", "Offloads non-GL tasks away from the main Render thread.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Offload IO Tasks", "Offloads non-GL tasks away from the main Render thread.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Batch Draw Commands", "Additional control for Render Thread Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Render Thread Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
