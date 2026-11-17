package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class SignRenderingOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public SignRenderingOptimizerModule() {
        super("sign_rendering_optimizer", "Sign Rendering Optimizer", "Skips rendering sign text beyond 24 blocks to save FPS.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Cull Distant Sign Text", "Skips rendering sign text beyond 24 blocks to save FPS.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Hide Empty Signs", "Additional control for Sign Rendering Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Sign Rendering Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
