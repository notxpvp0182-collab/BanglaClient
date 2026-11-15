package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Crystal Clear Water Module:
 * - Removes underwater fog completely when diving underwater (via CameraSubmersionType bypass)
 * - Adjustable clarity percentage (0% to 100%)
 * - Surface-to-underwater clarity boost
 * - Removes murky underwater texture overlay
 */
public final class ClearWaterModule extends Module {
    private final BooleanSetting crystalClearNoFogSetting;
    private final NumberSetting clarityPercentSetting;
    private final BooleanSetting surfaceClaritySetting;
    private final BooleanSetting removeWaterOverlaySetting;

    public ClearWaterModule() {
        super(
                "clear_water",
                "Clear Water",
                "Makes water crystal clear both underwater and from the surface, removing underwater fog.",
                ModuleCategory.VISUAL
        );
        this.crystalClearNoFogSetting = addSetting(new BooleanSetting(
                "no_water_fog", "Crystal Clear Underwater (No Fog)", "Completely eliminate underwater fog when submerged", true
        ));
        this.clarityPercentSetting = addSetting(new NumberSetting(
                "clarity_pct", "Water Clarity Percentage", "How clear underwater vision should be (10% to 100%)",
                100.0, 10.0, 100.0, 5.0, "%"
        ));
        this.surfaceClaritySetting = addSetting(new BooleanSetting(
                "surface_clear", "Clear View From Above Water", "Enhance visibility when looking into water from above", true
        ));
        this.removeWaterOverlaySetting = addSetting(new BooleanSetting(
                "soften_overlay", "Remove Underwater Texture Overlay", "Hide the blue water screen overlay texture", true
        ));
        addAliases("waterclarity", "nofogwater", "underwater", "crystalwater");
    }

    public boolean shouldBypassUnderwaterFog() {
        return isEnabled() && crystalClearNoFogSetting.isEnabled() && clarityPercentSetting.getFloatValue() >= 50.0f;
    }

    public float getUnderwaterFogMultiplier() {
        if (!isEnabled()) return 1.0f;
        return 1.0f + (clarityPercentSetting.getFloatValue() / 10.0f);
    }

    public boolean shouldEnhanceSurfaceClarity() {
        return isEnabled() && surfaceClaritySetting.isEnabled();
    }

    public boolean shouldSoftenWaterOverlay() {
        return isEnabled() && removeWaterOverlaySetting.isEnabled();
    }
}
