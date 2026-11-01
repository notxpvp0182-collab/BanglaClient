package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Module 40: Custom Vignette
 * Customizes or disables the vanilla dark corner vignette darkness in dark areas.
 */
public final class CustomVignetteModule extends Module {
    private final BooleanSetting disableVanillaVignetteSetting;
    private final NumberSetting vignetteStrengthSetting;

    public CustomVignetteModule() {
        super(
                "custom_vignette",
                "Custom Vignette",
                "Customizes or cleanly disables the vanilla dark corner vignette overlay.",
                ModuleCategory.VISUAL
        );
        this.disableVanillaVignetteSetting = addSetting(new BooleanSetting(
                "disable_vignette", "Disable Dark Corner Vignette", "Completely hide the vanilla dark corner shading", true
        ));
        this.vignetteStrengthSetting = addSetting(new NumberSetting(
                "strength", "Vignette Darkness", "Custom vignette intensity when not disabled",
                30.0, 0.0, 100.0, 5.0, "%"
        )).visibleWhen(() -> !disableVanillaVignetteSetting.isEnabled()) instanceof NumberSetting ns ? ns : null;
        addAliases("vignette", "novignette", "cornershadow");
    }

    public boolean shouldDisableVignette() {
        return isEnabled() && disableVanillaVignetteSetting.isEnabled();
    }

    public float getVignetteMultiplier() {
        if (!isEnabled()) {
            return 1.0f;
        }
        if (disableVanillaVignetteSetting.isEnabled()) {
            return 0.0f;
        }
        return vignetteStrengthSetting != null ? (vignetteStrengthSetting.getFloatValue() / 100.0f) : 0.3f;
    }
}
