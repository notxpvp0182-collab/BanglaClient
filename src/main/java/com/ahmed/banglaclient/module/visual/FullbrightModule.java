package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.lwjgl.glfw.GLFW;

/**
 * Module 28: Full Bright
 * Provides full-game brightness/gamma visibility in dark caves and low-light environments,
 * restoring normal rendering cleanly when disabled.
 */
public final class FullbrightModule extends Module {
    private final NumberSetting brightnessFactorSetting;
    private final BooleanSetting shaderNightVisionCarrierSetting;

    private double savedVanillaGamma = 0.5;
    private boolean appliedNightVision = false;

    public FullbrightModule() {
        super(
                "fullbright",
                "Full Bright",
                "Illuminates dark caves and low-light environments with full brightness.",
                ModuleCategory.VISUAL,
                GLFW.GLFW_KEY_G,
                false
        );
        this.brightnessFactorSetting = addSetting(new NumberSetting(
                "strength", "Brightness Strength", "Intensity of ambient light enhancement",
                100.0, 20.0, 100.0, 5.0, "%"
        ));
        this.shaderNightVisionCarrierSetting = addSetting(new BooleanSetting(
                "nv_carrier", "Full Cave Illumination", "Ensure 100% visibility in pitch-black caves and deep dark", true
        ));
        addAliases("fullbright", "gamma", "brightness", "light", "nightvision");
    }

    @Override
    protected void onEnable() {
        if (client != null && client.options != null) {
            savedVanillaGamma = client.options.getGamma().getValue();
            client.options.getGamma().setValue(1.0);
        }
    }

    @Override
    protected void onDisable() {
        if (client != null && client.options != null) {
            client.options.getGamma().setValue(Math.max(0.0, Math.min(1.0, savedVanillaGamma)));
        }
        removeClientNightVision();
    }

    @Override
    public void onTick() {
        if (client != null && client.options != null) {
            double targetGamma = brightnessFactorSetting.getValue() / 100.0;
            if (Math.abs(client.options.getGamma().getValue() - targetGamma) > 0.01) {
                client.options.getGamma().setValue(targetGamma);
            }
        }

        if (client != null && client.player != null && shaderNightVisionCarrierSetting.isEnabled()) {
            if (!client.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                appliedNightVision = true;
                client.player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NIGHT_VISION, 400, 0, true, false, false
                ));
            } else if (appliedNightVision) {
                StatusEffectInstance existing = client.player.getStatusEffect(StatusEffects.NIGHT_VISION);
                if (existing != null && existing.getDuration() < 260) {
                    client.player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.NIGHT_VISION, 400, 0, true, false, false
                    ));
                }
            }
        } else if (appliedNightVision) {
            removeClientNightVision();
        }
    }

    private void removeClientNightVision() {
        if (appliedNightVision && client != null && client.player != null) {
            client.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
        appliedNightVision = false;
    }
}
