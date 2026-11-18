package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ParticleVisibilityControlModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ParticleVisibilityControlModule() {
        super("particle_visibility_control", "Particle Visibility Control", "Selectively hide explosion, weather, or block-break particles.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Hide Explosion Smoke", "Selectively hide explosion, weather, or block-break particles.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Hide Block Dust", "Additional control for Particle Visibility Control", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Particle Visibility Control Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
