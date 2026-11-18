package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class FireRenderingOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public FireRenderingOptimizerModule() {
        super("fire_rendering_optimizer", "Fire Rendering Optimizer", "Reduces GPU load from widespread fire blocks and burning entities.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Simplify Fire Quads", "Reduces GPU load from widespread fire blocks and burning entities.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Cull Distant Flames", "Additional control for Fire Rendering Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Fire Rendering Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
