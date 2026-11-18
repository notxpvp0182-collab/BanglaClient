package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

public final class TotemOptimizerModule extends Module {
    private final BooleanSetting limitTotemPopParticlesSetting;

    public TotemOptimizerModule() {
        super("totem_optimizer", "Totem Optimizer",
                "Reduces Totem of Undying pop particle bursts so popping a totem in PvP never drops FPS.",
                ModuleCategory.PERFORMANCE);
        this.limitTotemPopParticlesSetting = addSetting(new BooleanSetting("limit_pop_particles", "Limit Totem Pop Particles", "Thin out yellow/green totem particle burst", true));
        addAliases("totemlag", "totemparticles");
    }

    public boolean shouldLimitPopParticles() { return isEnabled() && limitTotemPopParticlesSetting.isEnabled(); }
}
