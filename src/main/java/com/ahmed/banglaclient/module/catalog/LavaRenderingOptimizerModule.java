package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class LavaRenderingOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public LavaRenderingOptimizerModule() {
        super("lava_rendering_optimizer", "Lava Rendering Optimizer", "Optimizes lava flow texture and vertex updates.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Fast Lava Mesh", "Optimizes lava flow texture and vertex updates.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Reduce Lava Sparks", "Additional control for Lava Rendering Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Lava Rendering Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
