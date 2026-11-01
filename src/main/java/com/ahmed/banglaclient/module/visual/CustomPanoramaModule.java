package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.ColorSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Module 36: Custom Panorama
 * Allows customization of the main menu panorama rotation speed, ambient gradient overlay, and tint.
 */
public final class CustomPanoramaModule extends Module {
    private final NumberSetting rotationSpeedSetting;
    private final BooleanSetting applyDarkGlassTintSetting;
    private final ColorSetting topGradientColorSetting;
    private final ColorSetting bottomGradientColorSetting;

    public CustomPanoramaModule() {
        super(
                "custom_panorama",
                "Custom Panorama",
                "Customizes the main menu panorama rotation speed and ambient Liquid Glass backdrop tint.",
                ModuleCategory.VISUAL
        );
        this.rotationSpeedSetting = addSetting(new NumberSetting(
                "rotation_speed", "Rotation Speed", "Multiplier for main menu panorama rotation speed",
                1.0, 0.0, 4.0, 0.1, "x"
        ));
        this.applyDarkGlassTintSetting = addSetting(new BooleanSetting(
                "glass_tint", "Liquid Glass Gradient Overlay", "Render a smooth dark gradient over the panorama", true
        ));
        this.topGradientColorSetting = addSetting(new ColorSetting(
                "top_color", "Top Gradient Tint", "Top atmospheric overlay color", 0x66080A0F, true
        )).visibleWhen(applyDarkGlassTintSetting::isEnabled) instanceof ColorSetting cs ? cs : null;
        this.bottomGradientColorSetting = addSetting(new ColorSetting(
                "bottom_color", "Bottom Gradient Tint", "Bottom atmospheric overlay color", 0xB3080A0F, true
        )).visibleWhen(applyDarkGlassTintSetting::isEnabled) instanceof ColorSetting cs ? cs : null;
        addAliases("panorama", "menubackground", "skybox");
    }

    public float getRotationSpeedMultiplier() {
        return isEnabled() ? rotationSpeedSetting.getFloatValue() : 1.0f;
    }

    public boolean shouldRenderGradientTint() {
        return isEnabled() && applyDarkGlassTintSetting.isEnabled();
    }

    public int getTopGradientArgb() {
        return topGradientColorSetting != null ? topGradientColorSetting.getArgb() : 0x66080A0F;
    }

    public int getBottomGradientArgb() {
        return bottomGradientColorSetting != null ? bottomGradientColorSetting.getArgb() : 0xB3080A0F;
    }
}
