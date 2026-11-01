package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Module 37: Clear Water
 * Provides a cleaner underwater visual appearance by extending underwater fog end distance
 * within legitimate client rendering bounds.
 */
public final class ClearWaterModule extends Module {
    private final NumberSetting clarityFactorSetting;
    private final BooleanSetting reduceUnderwaterOverlaySetting;

    public ClearWaterModule() {
        super(
                "clear_water",
                "Clear Water",
                "Enhances underwater visual clarity and softens the underwater screen overlay.",
                ModuleCategory.VISUAL
        );
        this.clarityFactorSetting = addSetting(new NumberSetting(
                "clarity_factor", "Water Clarity", "Underwater fog end distance multiplier",
                1.8, 1.0, 3.0, 0.1, "x"
        ));
        this.reduceUnderwaterOverlaySetting = addSetting(new BooleanSetting(
                "soften_overlay", "Soften Water Texture Overlay", "Reduce opacity of the first-person underwater texture overlay", true
        ));
        addAliases("water", "underwater", "waterfog");
    }

    public float getUnderwaterFogMultiplier() {
        return isEnabled() ? clarityFactorSetting.getFloatValue() : 1.0f;
    }

    public boolean shouldSoftenWaterOverlay() {
        return isEnabled() && reduceUnderwaterOverlaySetting.isEnabled();
    }
}
