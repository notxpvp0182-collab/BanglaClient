package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ClearWaterModule extends Module {
    private final BooleanSetting crystalClearNoFogSetting;
    private final NumberSetting clarityPercentSetting;
    private final BooleanSetting surfaceClaritySetting;
    private final NumberSetting surfaceTransparencySetting;
    private final BooleanSetting removeWaterOverlaySetting;

    public ClearWaterModule() {
        super(
                "clear_water",
                "Clear Water",
                "Makes water crystal-clear both when submerged underwater AND when looking into water from above.",
                ModuleCategory.VISUAL
        );
        this.crystalClearNoFogSetting = addSetting(new BooleanSetting(
                "no_water_fog", "Crystal Clear Underwater (No Fog)", "Completely eliminate underwater fog when submerged", true
        ));
        this.clarityPercentSetting = addSetting(new NumberSetting(
                "clarity_pct", "Underwater Clarity %", "How clear underwater vision should be (10% to 100%)",
                100.0, 10.0, 100.0, 5.0, "%"
        ));
        this.surfaceClaritySetting = addSetting(new BooleanSetting(
                "surface_clear", "Clear View From Above Water", "Make water surface transparent & bright when looking from above", true
        ));
        this.surfaceTransparencySetting = addSetting(new NumberSetting(
                "surface_pct", "Surface-to-Bottom Clarity %", "How transparent and bright water appears from above (10% to 100%)",
                85.0, 10.0, 100.0, 5.0, "%"
        ));
        this.removeWaterOverlaySetting = addSetting(new BooleanSetting(
                "soften_overlay", "Remove Underwater Texture Overlay", "Hide the murky underwater screen overlay texture", true
        ));

        this.surfaceClaritySetting.onChange(v -> reloadFluidChunks());
        this.surfaceTransparencySetting.onChange(v -> reloadFluidChunks());
        addAliases("waterclarity", "nofogwater", "underwater", "crystalwater", "waterqualitycontrol");
    }

    @Override
    protected void onEnable() { reloadFluidChunks(); }

    @Override
    protected void onDisable() { reloadFluidChunks(); }

    private void reloadFluidChunks() {
        if (client != null && client.worldRenderer != null && client.world != null) {
            client.execute(() -> {
                try { client.worldRenderer.reload(); } catch (Throwable ignored) {}
            });
        }
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

    public float getSurfaceClarityFactor() {
        return surfaceTransparencySetting.getFloatValue() / 100.0f;
    }

    public boolean shouldSoftenWaterOverlay() {
        return isEnabled() && removeWaterOverlaySetting.isEnabled();
    }
}
