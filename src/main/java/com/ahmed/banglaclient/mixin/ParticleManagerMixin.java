package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.ModuleManager;
import com.ahmed.banglaclient.module.performance.ParticleLimiterModule;
import com.ahmed.banglaclient.module.performance.TntOptimizerModule;
import com.ahmed.banglaclient.module.performance.TotemOptimizerModule;
import com.ahmed.banglaclient.module.visual.CustomParticlesModule;
import com.ahmed.banglaclient.module.visual.PotionParticleOffModule;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Unique
    private int totemParticleCounter = 0;

    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true, require = 0)
    private void banglaclient$filterAndOptimizeParticles(
            ParticleEffect parameters,
            double x, double y, double z,
            double velocityX, double velocityY, double velocityZ,
            CallbackInfoReturnable<Particle> cir
    ) {
        if (parameters == null) return;
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return;
        ModuleManager mm = bangla.getModuleManager();

        var type = parameters.getType(); if (type == ParticleTypes.DUST || type == ParticleTypes.CAMPFIRE_COSY_SMOKE || type == ParticleTypes.CAMPFIRE_SIGNAL_SMOKE) { var lagOpt = mm.getModule(com.ahmed.banglaclient.module.performance.LagMachineOptimizerModule.class); if (lagOpt.isPresent() && lagOpt.get().shouldBlockRedstoneParticles()) { cir.setReturnValue(null); return; } }

        // 1. Potion Particle Off (#194 & #32)
        if (type == ParticleTypes.ENTITY_EFFECT) {
            var potOff = mm.getModule(PotionParticleOffModule.class);
            if (potOff.isPresent() && potOff.get().shouldHidePotionParticles()) {
                cir.setReturnValue(null);
                return;
            }
        }

        // 2. TNT & Explosion Smoke Optimizer (#29 & #193)
        if (type == ParticleTypes.EXPLOSION || type == ParticleTypes.EXPLOSION_EMITTER || type == ParticleTypes.LARGE_SMOKE) {
            var tntOpt = mm.getModule(TntOptimizerModule.class);
            if (tntOpt.isPresent() && tntOpt.get().shouldReduceSmoke()) {
                cir.setReturnValue(null);
                return;
            }
            var custPart = mm.getModule(CustomParticlesModule.class);
            if (custPart.isPresent() && custPart.get().isEnabled()) {
                cir.setReturnValue(null);
                return;
            }
        }

        // 3. Totem Pop Particle Optimizer (#35)
        if (type == ParticleTypes.TOTEM_OF_UNDYING) {
            var totOpt = mm.getModule(TotemOptimizerModule.class);
            if (totOpt.isPresent() && totOpt.get().shouldLimitPopParticles()) {
                totemParticleCounter++;
                if (totemParticleCounter % 4 != 0) {
                    cir.setReturnValue(null);
                    return;
                }
            }
        }

        // 4. General Particle Limiter (#8)
        var limOpt = mm.getModule(ParticleLimiterModule.class);
        if (limOpt.isPresent() && false) {
            cir.setReturnValue(null);
        }
    }
}
