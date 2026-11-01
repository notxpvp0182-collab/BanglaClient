package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.lwjgl.glfw.GLFW;

/**
 * Module 28: Fullbright
 * Provides configurable client-side brightness enhancement using smooth gamma/lightmap scaling
 * or optional client-side night-vision preview, restoring vanilla brightness cleanly on disable.
 */
public final class FullbrightModule extends Module {
    private final NumberSetting brightnessFactorSetting;
    private final BooleanSetting smoothTransitionSetting;
    private final BooleanSetting nightVisionFallbackSetting;

    private float currentFactor = 0.0f;
    private boolean appliedNightVision = false;

    public FullbrightModule() {
        super(
                "fullbright",
                "Fullbright",
                "Enhances world ambient brightness in dark caves and night environments.",
                ModuleCategory.VISUAL,
                GLFW.GLFW_KEY_G,
                false
        );
        this.brightnessFactorSetting = addSetting(new NumberSetting(
                "strength", "Brightness Strength", "Intensity of ambient light enhancement",
                100.0, 20.0, 100.0, 5.0, "%"
        ));
        this.smoothTransitionSetting = addSetting(new BooleanSetting(
                "smooth_fade", "Smooth Fade Transition", "Smoothly interpolate brightness when toggling ON/OFF", true
        ));
        this.nightVisionFallbackSetting = addSetting(new BooleanSetting(
                "nv_carrier", "Shader-Compatible Light", "Apply client-side ambient night vision carrier for third-party shader packs", true
        ));
        addAliases("gamma", "brightness", "light", "nightvision");
    }

    @Override
    protected void onDisable() {
        currentFactor = 0.0f;
        removeClientNightVision();
    }

    @Override
    public void onTick() {
        float target = (brightnessFactorSetting.getFloatValue() / 100.0f);
        if (smoothTransitionSetting.isEnabled()) {
            currentFactor = MathUtils.lerp(currentFactor, target, 0.25f);
        } else {
            currentFactor = target;
        }

        if (client.player != null && nightVisionFallbackSetting.isEnabled()) {
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
        if (appliedNightVision && client.player != null) {
            client.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
        appliedNightVision = false;
    }

    public float getEffectiveBrightnessBoost() {
        return isEnabled() ? currentFactor : 0.0f;
    }
}
