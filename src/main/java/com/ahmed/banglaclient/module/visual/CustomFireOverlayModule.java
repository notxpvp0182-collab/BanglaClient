package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Controls both First-Person Low Fire Overlay (raise/lower/scale/hide)
 * AND Ground Fire / Soul Fire height in the world!
 */
public final class CustomFireOverlayModule extends Module {
    private final NumberSetting verticalOffsetSetting;
    private final NumberSetting fireHeightScaleSetting;
    private final BooleanSetting hideCompletelySetting;
    private final BooleanSetting controlGroundFireSetting;
    private final NumberSetting groundFireHeightSetting;

    public CustomFireOverlayModule() {
        super(
                "custom_fire_overlay",
                "Low Fire & Ground Fire",
                "Lowers, raises, scales, or hides first-person fire overlay and controls ground/soul fire height.",
                ModuleCategory.VISUAL
        );
        this.verticalOffsetSetting = addSetting(new NumberSetting(
                "vertical_offset", "First-Person Fire Position", "Lower (-0.60) or raise (+0.30) first-person fire on screen",
                -0.30, -0.65, 0.35, 0.05, ""
        ));
        this.fireHeightScaleSetting = addSetting(new NumberSetting(
                "opacity_scale", "First-Person Fire Height", "Scale the height of first-person fire (10% to 150%)",
                65.0, 10.0, 150.0, 5.0, "%"
        ));
        this.hideCompletelySetting = addSetting(new BooleanSetting(
                "hide_fire", "Hide First-Person Fire", "Completely hide first-person fire overlay", false
        ));
        this.controlGroundFireSetting = addSetting(new BooleanSetting(
                "ground_fire_ctrl", "Control Ground & Soul Fire", "Enable custom height scaling for placed normal fire and soul fire", true
        ));
        this.groundFireHeightSetting = addSetting(new NumberSetting(
                "ground_fire_height", "Ground & Soul Fire Height", "Height of placed normal fire and soul fire blocks (15% to 150%)",
                50.0, 15.0, 150.0, 5.0, "%"
        ));
        addAliases("lowfire", "nofire", "fireoffset", "flameoverlay", "groundfire", "soulfire");
    }

    public float getVerticalOffset() {
        return isEnabled() ? verticalOffsetSetting.getFloatValue() : 0.0f;
    }

    public float getOpacityScale() {
        return isEnabled() ? (fireHeightScaleSetting.getFloatValue() / 100.0f) : 1.0f;
    }

    public boolean shouldHideOverlayCompletely() {
        return isEnabled() && hideCompletelySetting.isEnabled();
    }

    public boolean shouldScaleGroundFire() {
        return isEnabled() && controlGroundFireSetting.isEnabled();
    }

    public float getGroundFireHeightScale() {
        return isEnabled() && controlGroundFireSetting.isEnabled()
                ? (groundFireHeightSetting.getFloatValue() / 100.0f)
                : 1.0f;
    }
}
