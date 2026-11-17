package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class PotionFovParticleOffModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public PotionFovParticleOffModule() {
        super("potion_fov_particle_off", "Potion FOV Particle Off", "Disables first-person screen particles from beacon and potion effects.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Clean First-Person View", "Disables first-person screen particles from beacon and potion effects.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Keep Status Icons", "Additional control for Potion FOV Particle Off", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Potion FOV Particle Off Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
