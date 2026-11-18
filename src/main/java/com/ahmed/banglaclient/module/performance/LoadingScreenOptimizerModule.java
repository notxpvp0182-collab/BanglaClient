package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class LoadingScreenOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public LoadingScreenOptimizerModule() {
        super("loading_screen_optimizer", "Loading Screen Optimizer", "Reduces resource reload overhead during splash screens.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Fast Atlas Stitch", "Reduces resource reload overhead during splash screens.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Skip Unused Reloads", "Additional control for Loading Screen Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Loading Screen Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
