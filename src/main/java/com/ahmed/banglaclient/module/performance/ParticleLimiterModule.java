package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.EnumSetting;
import net.minecraft.particle.ParticlesMode;

/**
 * Module 105: Particle Limiter
 * Provides configurable particle density controls mapped to Minecraft's native `ParticlesMode`
 * (`ALL`, `DECREASED`, `MINIMAL`) and automatic particle reduction during FPS drops.
 */
public final class ParticleLimiterModule extends Module {
    public enum DensityPreset {
        ALL("All Particles", ParticlesMode.ALL),
        DECREASED("Decreased", ParticlesMode.DECREASED),
        MINIMAL("Minimal (Best FPS)", ParticlesMode.MINIMAL);

        private final String label;
        private final ParticlesMode vanillaMode;

        DensityPreset(String label, ParticlesMode vanillaMode) {
            this.label = label;
            this.vanillaMode = vanillaMode;
        }

        public ParticlesMode getVanillaMode() {
            return vanillaMode;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final EnumSetting<DensityPreset> densityPresetSetting;
    private final BooleanSetting autoMinimalBelow40FpsSetting;

    public ParticleLimiterModule() {
        super(
                "particle_limiter",
                "Particle Limiter",
                "Controls particle density mode and optionally reduces particles when FPS drops.",
                ModuleCategory.PERFORMANCE
        );
        this.densityPresetSetting = addSetting(new EnumSetting<>(
                "density", "Particle Density Mode", "Target particle rendering density", DensityPreset.DECREASED
        ));
        this.autoMinimalBelow40FpsSetting = addSetting(new BooleanSetting(
                "auto_minimal", "Auto-Minimal Below 40 FPS", "Temporarily switch to Minimal particles when FPS dips below 40", true
        ));
        addAliases("particles", "lessparticles", "fpsparticles");
    }

    @Override
    public void onTick() {
        if (client == null || client.options == null) {
            return;
        }
        ParticlesMode target = densityPresetSetting.getValue().getVanillaMode();
        if (autoMinimalBelow40FpsSetting.isEnabled() && client.getCurrentFps() > 0 && client.getCurrentFps() < 40) {
            target = ParticlesMode.MINIMAL;
        }
        if (client.options.getParticles().getValue() != target) {
            client.options.getParticles().setValue(target);
        }
    }
}
