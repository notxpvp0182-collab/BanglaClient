package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.ColorSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Module 30: Custom Fog
 * Customizes horizon fog start/end distance multipliers and optional custom fog tint color.
 * Never bypasses server-applied Blindness or Darkness status effects.
 */
public final class CustomFogModule extends Module {
    private final NumberSetting fogStartMultiplierSetting;
    private final NumberSetting fogEndMultiplierSetting;
    private final BooleanSetting customFogColorEnabledSetting;
    private final ColorSetting fogColorSetting;

    public CustomFogModule() {
        super(
                "custom_fog",
                "Custom Fog",
                "Customizes horizon fog distance density and optional atmospheric fog color.",
                ModuleCategory.VISUAL
        );
        this.fogStartMultiplierSetting = addSetting(new NumberSetting(
                "fog_start", "Fog Start Distance", "Multiplier for where horizon fog begins",
                1.2, 0.2, 2.5, 0.1, "x"
        ));
        this.fogEndMultiplierSetting = addSetting(new NumberSetting(
                "fog_end", "Fog End Distance", "Multiplier for where horizon fog reaches full density",
                1.3, 0.5, 3.0, 0.1, "x"
        ));
        this.customFogColorEnabledSetting = addSetting(new BooleanSetting(
                "use_custom_color", "Custom Fog Tint", "Override biome fog color with a custom atmospheric tint", false
        ));
        this.fogColorSetting = addSetting(new ColorSetting(
                "fog_color", "Fog Color", "Atmospheric RGB tint for world fog", 0xFF1E293B, false
        )).visibleWhen(customFogColorEnabledSetting::isEnabled) instanceof ColorSetting cs ? cs : null;
        addAliases("fog", "atmosphere", "haze", "mist");
    }

    public float getFogStartMultiplier() {
        return fogStartMultiplierSetting.getFloatValue();
    }

    public float getFogEndMultiplier() {
        return fogEndMultiplierSetting.getFloatValue();
    }

    public boolean isCustomFogColorEnabled() {
        return customFogColorEnabledSetting.isEnabled();
    }

    public int getCustomFogColorRgb() {
        return fogColorSetting != null ? fogColorSetting.getArgb() : 0xFF1E293B;
    }
}
