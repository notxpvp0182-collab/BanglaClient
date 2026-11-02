package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * "Crystal Optimizer" module.
 * Optimizes client-side End Crystal rendering overhead and visual responsiveness
 * while strictly preserving server-authoritative combat validation.
 */
public final class CrystalOptimizerModule extends Module {
    private final NumberSetting crystalScaleSetting;
    private final BooleanSetting reduceInnerCubeOverheadSetting;
    private final BooleanSetting cleanExplosionParticlesSetting;

    public CrystalOptimizerModule() {
        super(
                "crystal_optimizer",
                "Crystal Optimizer",
                "Optimizes client-side End Crystal rendering overhead and visual responsiveness.",
                ModuleCategory.COMBAT
        );
        this.crystalScaleSetting = addSetting(new NumberSetting(
                "crystal_scale", "Crystal Visual Scale", "Client-side visual scale multiplier for End Crystals",
                1.0, 0.6, 1.2, 0.05, "x"
        ));
        this.reduceInnerCubeOverheadSetting = addSetting(new BooleanSetting(
                "reduce_overhead", "Optimize Multi-Crystal Rendering", "Reduce redundant rotation calculations when many crystals are loaded", true
        ));
        this.cleanExplosionParticlesSetting = addSetting(new BooleanSetting(
                "clean_particles", "Clean Explosion Particles", "Prevent dense crystal explosion particles from obscuring vision", true
        ));
        addAliases("crystal", "endcrystal", "crystalopt", "cpvp");
    }

    public float getCrystalScale() { return crystalScaleSetting.getFloatValue(); }
    public boolean shouldReduceOverhead() { return isEnabled() && reduceInnerCubeOverheadSetting.isEnabled(); }
    public boolean shouldCleanParticles() { return isEnabled() && cleanExplosionParticlesSetting.isEnabled(); }
}
