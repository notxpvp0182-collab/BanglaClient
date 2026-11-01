package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;

/**
 * Module 34: Custom Particles
 * Customizes combat critical/enchant particle density and global particle visibility options.
 */
public final class CustomParticlesModule extends Module {
    private final NumberSetting attackParticleMultiplierSetting;
    private final BooleanSetting alwaysShowEnchantParticlesSetting;
    private final BooleanSetting reduceExplosionSmokeSetting;

    private static boolean hookRegistered = false;
    private static CustomParticlesModule instance;

    public CustomParticlesModule() {
        super(
                "custom_particles",
                "Custom Particles",
                "Customizes combat particle multiplier, sharpness sparkles, and smoke density.",
                ModuleCategory.VISUAL
        );
        this.attackParticleMultiplierSetting = addSetting(new NumberSetting(
                "multiplier", "Attack Particle Multiplier", "Number of extra visual particles spawned on hit",
                2.0, 1.0, 6.0, 1.0, "x"
        ));
        this.alwaysShowEnchantParticlesSetting = addSetting(new BooleanSetting(
                "enchant_sparkles", "Enchant Hit Sparkles", "Spawn enchanted hit particles on valid melee attacks", true
        ));
        this.reduceExplosionSmokeSetting = addSetting(new BooleanSetting(
                "reduce_explosion", "Clean Explosion Smoke", "Reduce dense screen-obscuring explosion smoke particles", false
        ));
        instance = this;
        registerHookOnce();
        addAliases("particles", "critparticles", "sparkles");
    }

    private static synchronized void registerHookOnce() {
        if (hookRegistered) {
            return;
        }
        hookRegistered = true;
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && instance != null && instance.isEnabled() && entity instanceof LivingEntity) {
                instance.spawnExtraHitParticles(entity);
            }
            return ActionResult.PASS;
        });
    }

    private void spawnExtraHitParticles( net.minecraft.entity.Entity target) {
        if (client == null || client.particleManager == null) {
            return;
        }
        int mult = attackParticleMultiplierSetting.getIntValue();
        for (int i = 1; i < mult; i++) {
            client.particleManager.addEmitter(target, ParticleTypes.CRIT);
            if (alwaysShowEnchantParticlesSetting.isEnabled()) {
                client.particleManager.addEmitter(target, ParticleTypes.ENCHANTED_HIT);
            }
        }
    }

    public boolean shouldReduceExplosionSmoke() {
        return isEnabled() && reduceExplosionSmokeSetting.isEnabled();
    }
}
