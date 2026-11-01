package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Module 41: Custom Hurt Camera
 * Customizes the visual hurt-camera tilt multiplier (`0%` to `150%`) to reduce motion sickness
 * without changing server combat behavior or knockback.
 */
public final class CustomHurtCameraModule extends Module {
    private final NumberSetting tiltStrengthSetting;

    public CustomHurtCameraModule() {
        super(
                "custom_hurt_camera",
                "Custom Hurt Camera",
                "Customizes the visual camera tilt intensity when taking damage.",
                ModuleCategory.VISUAL
        );
        this.tiltStrengthSetting = addSetting(new NumberSetting(
                "tilt_strength", "Camera Tilt Multiplier", "Intensity of the hurt camera roll (35% reduces fire/drowning shake)",
                35.0, 0.0, 150.0, 5.0, "%"
        ));
        addAliases("hurtcam", "nohurtcam", "screenshake", "cameratilt");
    }

    public float getHurtTiltMultiplier() {
        if (!isEnabled()) {
            return 1.0f;
        }
        return tiltStrengthSetting.getFloatValue() / 100.0f;
    }
}
