package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Module 38: Custom Fire Overlay
 * Customizes the vertical Y-offset and scale/opacity of the first-person fire overlay so it does not obscure vision.
 */
public final class CustomFireOverlayModule extends Module {
    private final NumberSetting verticalOffsetSetting;
    private final NumberSetting opacityScaleSetting;

    public CustomFireOverlayModule() {
        super(
                "custom_fire_overlay",
                "Custom Fire Overlay",
                "Lowers and scales the first-person fire overlay so it does not block the screen.",
                ModuleCategory.VISUAL
        );
        this.verticalOffsetSetting = addSetting(new NumberSetting(
                "y_offset", "Vertical Offset", "How far down to shift the first-person fire flames",
                -0.22, -0.45, 0.0, 0.01, ""
        ));
        this.opacityScaleSetting = addSetting(new NumberSetting(
                "opacity", "Fire Opacity", "Opacity multiplier for the first-person fire flames",
                0.75, 0.15, 1.0, 0.05, ""
        ));
        addAliases("lowfire", "fireoverlay", "flames", "fire");
    }

    public float getVerticalOffset() {
        return isEnabled() ? verticalOffsetSetting.getFloatValue() : 0.0f;
    }

    public float getOpacityScale() {
        return isEnabled() ? opacityScaleSetting.getFloatValue() : 1.0f;
    }
}
