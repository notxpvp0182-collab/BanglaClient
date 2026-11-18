package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

public final class PotionParticleOffModule extends Module {
    private final BooleanSetting hideAllSwirlsSetting;

    public PotionParticleOffModule() {
        super("potion_particle_off", "Potion Particle Off",
                "Disables swirling status-effect potion particles for a completely clear PvP view.",
                ModuleCategory.VISUAL);
        this.hideAllSwirlsSetting = addSetting(new BooleanSetting("hide_swirls", "Hide Effect Swirl Particles", "Cancel entity_effect particle spawns", true));
        addAliases("nopotionparticles", "potionoptimizer", "cleareffects");
    }

    public boolean shouldHidePotionParticles() { return isEnabled() && hideAllSwirlsSetting.isEnabled(); }
}
