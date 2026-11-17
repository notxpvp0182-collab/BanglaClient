package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class PotionParticleOffModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public PotionParticleOffModule() {
        super("potion_particle_off", "Potion Particle Off", "Hides swirling potion effect particles around players.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Hide Self Swirls", "Hides swirling potion effect particles around players.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Hide Other Players", "Additional control for Potion Particle Off", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Potion Particle Off Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
