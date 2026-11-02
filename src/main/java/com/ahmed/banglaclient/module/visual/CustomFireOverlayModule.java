package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Module 38: Low Fire (Custom Fire Overlay)
 * Lowers or hides first-person normal and soul fire overlays and provides separate controls
 * without altering server fire mechanics.
 */
public final class CustomFireOverlayModule extends Module {
    private final NumberSetting verticalOffsetSetting;
    private final NumberSetting overlayScaleSetting;
    private final BooleanSetting supportSoulFireSetting;
    private final BooleanSetting hideOverlayCompletelySetting;

    public CustomFireOverlayModule() {
        super(
                "custom_fire_overlay",
                "Low Fire",
                "Lowers first-person normal and soul fire overlays so flames never block your vision.",
                ModuleCategory.VISUAL
        );
        this.verticalOffsetSetting = addSetting(new NumberSetting(
                "y_offset", "First-Person Fire Height", "How far down to shift first-person fire flames",
                -0.28, -0.50, 0.0, 0.02, ""
        ));
        this.overlayScaleSetting = addSetting(new NumberSetting(
                "flame_scale", "Flame Scale", "Visual height scale of first-person fire animation",
                0.75, 0.25, 1.0, 0.05, "x"
        ));
        this.supportSoulFireSetting = addSetting(new BooleanSetting(
                "soul_fire", "Support Soul Fire", "Apply Low Fire lowering to blue Soul Fire overlays as well", true
        ));
        this.hideOverlayCompletelySetting = addSetting(new BooleanSetting(
                "hide_overlay", "Hide First-Person Fire", "Completely suppress the first-person flame overlay while burning", false
        ));
        addAliases("lowfire", "fireoverlay", "flames", "fire", "soulfire");
    }

    public float getVerticalOffset() {
        return isEnabled() ? verticalOffsetSetting.getFloatValue() : 0.0f;
    }

    public float getOpacityScale() {
        return isEnabled() ? overlayScaleSetting.getFloatValue() : 1.0f;
    }

    public boolean shouldHideOverlayCompletely() {
        return isEnabled() && hideOverlayCompletelySetting.isEnabled();
    }

    public boolean isSoulFireSupported() {
        return isEnabled() && supportSoulFireSetting.isEnabled();
    }
}
