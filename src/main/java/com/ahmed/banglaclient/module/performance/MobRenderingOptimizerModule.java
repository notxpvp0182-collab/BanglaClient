package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class MobRenderingOptimizerModule extends Module {
    private final NumberSetting maxMobDistanceSetting;
    private final BooleanSetting cullPassiveMobsSetting;

    public MobRenderingOptimizerModule() {
        super("mob_rendering_optimizer", "Mob Rendering Optimizer",
                "Optimizes mob entity rendering by culling distant passive and ambient mobs.",
                ModuleCategory.PERFORMANCE);
        this.maxMobDistanceSetting = addSetting(new NumberSetting("mob_dist", "Max Mob Render Distance", "Max distance to render mobs", 56.0, 20.0, 120.0, 4.0, "m"));
        this.cullPassiveMobsSetting = addSetting(new BooleanSetting("cull_passive", "Aggressive Passive Mob Culling", "Cull farm animals closer to boost farm FPS", true));
        addAliases("moboptimizer", "mobculling");
    }

    public double getMaxMobDistanceSq() {
        double d = maxMobDistanceSetting.getValue();
        return d * d;
    }
}
