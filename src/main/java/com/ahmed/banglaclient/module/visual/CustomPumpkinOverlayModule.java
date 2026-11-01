package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Module 39: Custom Pumpkin Overlay
 * Customizes the opacity of the carved pumpkin screen overlay when wearing a Carved Pumpkin.
 */
public final class CustomPumpkinOverlayModule extends Module {
    private final NumberSetting pumpkinOpacitySetting;

    public CustomPumpkinOverlayModule() {
        super(
                "custom_pumpkin_overlay",
                "Custom Pumpkin Overlay",
                "Customizes the opacity of the screen blur when wearing a Carved Pumpkin.",
                ModuleCategory.VISUAL
        );
        this.pumpkinOpacitySetting = addSetting(new NumberSetting(
                "opacity", "Overlay Opacity", "Opacity of the carved pumpkin overlay (0% = clear, 100% = vanilla)",
                20.0, 0.0, 100.0, 5.0, "%"
        ));
        addAliases("pumpkin", "nopumpkin", "pumpkinblur");
    }

    public float getOverlayOpacity() {
        if (!isEnabled()) {
            return 1.0f;
        }
        return pumpkinOpacitySetting.getFloatValue() / 100.0f;
    }
}
