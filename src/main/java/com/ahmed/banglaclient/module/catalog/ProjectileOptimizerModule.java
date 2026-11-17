package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ProjectileOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ProjectileOptimizerModule() {
        super("projectile_optimizer", "Projectile Optimizer", "Optimizes arrow, trident, and snowball rendering.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Cull Stuck Arrows", "Optimizes arrow, trident, and snowball rendering.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Fast Projectile Mesh", "Additional control for Projectile Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Projectile Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
