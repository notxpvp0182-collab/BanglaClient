package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class MobRenderingOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public MobRenderingOptimizerModule() {
        super("mob_rendering_optimizer", "Mob Rendering Optimizer", "Reduces distant mob AI/animation rendering workload.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Throttle Distant Mobs", "Reduces distant mob AI/animation rendering workload.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Skip Far Limb Swing", "Additional control for Mob Rendering Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Mob Rendering Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
