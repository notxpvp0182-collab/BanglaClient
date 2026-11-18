package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class RenderOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public RenderOptimizerModule() {
        super("render_optimizer", "Render Optimizer", "Eliminates redundant rendering passes to boost FPS.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Skip Redundant Passes", "Eliminates redundant rendering passes to boost FPS.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Fast State Batching", "Additional control for Render Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Render Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
