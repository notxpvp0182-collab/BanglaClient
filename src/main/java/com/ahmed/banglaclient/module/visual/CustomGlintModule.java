package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Module 42: Custom Glint
 * Customizes enchantment glint strength and animation speed multiplier.
 */
public final class CustomGlintModule extends Module {
    private final NumberSetting glintStrengthSetting;
    private final NumberSetting glintSpeedSetting;

    public CustomGlintModule() {
        super(
                "custom_glint",
                "Custom Glint",
                "Customizes enchantment glint brightness and shimmer speed.",
                ModuleCategory.VISUAL
        );
        this.glintStrengthSetting = addSetting(new NumberSetting(
                "glint_strength", "Glint Brightness", "Intensity of the enchantment shimmer overlay",
                85.0, 0.0, 100.0, 5.0, "%"
        ));
        this.glintSpeedSetting = addSetting(new NumberSetting(
                "glint_speed", "Glint Shimmer Speed", "Animation speed multiplier for enchantment glint",
                100.0, 10.0, 200.0, 10.0, "%"
        ));
        addAliases("glint", "enchantglint", "shiny", "shimmer");
    }

    @Override
    public void onTick() {
        if (client == null || client.options == null) {
            return;
        }
        double targetStrength = glintStrengthSetting.getValue() / 100.0;
        double targetSpeed = Math.min(1.0, glintSpeedSetting.getValue() / 100.0);

        if (Math.abs(client.options.getGlintStrength().getValue() - targetStrength) > 0.01) {
            client.options.getGlintStrength().setValue(targetStrength);
        }
        if (Math.abs(client.options.getGlintSpeed().getValue() - targetSpeed) > 0.01) {
            client.options.getGlintSpeed().setValue(targetSpeed);
        }
    }

    public float getGlintStrengthMultiplier() {
        return isEnabled() ? (glintStrengthSetting.getFloatValue() / 100.0f) : 1.0f;
    }

    public float getGlintSpeedMultiplier() {
        return isEnabled() ? (glintSpeedSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
