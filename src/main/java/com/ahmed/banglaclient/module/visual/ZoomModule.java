package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.util.MathUtils;
import org.lwjgl.glfw.GLFW;

/**
 * Module 29: Zoom
 * Provides smooth camera zoom with configurable hold keybind (default: C), mouse-wheel zoom level adjustment,
 * and automatic mouse sensitivity scaling.
 */
public final class ZoomModule extends Module {
    private final NumberSetting defaultZoomLevelSetting;
    private final BooleanSetting scrollZoomSetting;
    private final BooleanSetting smoothZoomSetting;
    private final BooleanSetting scaleMouseSensitivitySetting;

    private final Animation zoomAnimation = new Animation(1.0f, 180L, Easing.EASE_OUT_CUBIC);
    private float dynamicScrollMultiplier = 1.0f;

    public ZoomModule() {
        super(
                "zoom",
                "Zoom",
                "Smooth camera zoom with mouse-wheel magnification and sensitivity scaling.",
                ModuleCategory.VISUAL,
                GLFW.GLFW_KEY_C,
                false
        );
        setHoldKeybind(true);
        this.defaultZoomLevelSetting = addSetting(new NumberSetting(
                "zoom_factor", "Base Zoom Factor", "Default magnification level when pressing the zoom key",
                4.0, 1.5, 15.0, 0.5, "x"
        ));
        this.scrollZoomSetting = addSetting(new BooleanSetting(
                "scroll_adjust", "Mouse Wheel Adjustment", "Allow scrolling the mouse wheel while zooming to adjust magnification", true
        ));
        this.smoothZoomSetting = addSetting(new BooleanSetting(
                "smooth_camera", "Smooth Zoom Transition", "Use smooth cubic easing when zooming in and out", true
        ));
        this.scaleMouseSensitivitySetting = addSetting(new BooleanSetting(
                "scale_sensitivity", "Scale Mouse Sensitivity", "Proportionally reduce camera turn speed while zoomed in", true
        ));
        addAliases("magnifying", "optic", "spyglass", "fov");
    }

    @Override
    protected void onEnable() {
        dynamicScrollMultiplier = 1.0f;
        float target = defaultZoomLevelSetting.getFloatValue();
        if (smoothZoomSetting.isEnabled()) {
            zoomAnimation.animateTo(target);
        } else {
            zoomAnimation.snapTo(target);
        }
    }

    @Override
    protected void onDisable() {
        dynamicScrollMultiplier = 1.0f;
        if (smoothZoomSetting.isEnabled()) {
            zoomAnimation.animateTo(1.0f);
        } else {
            zoomAnimation.snapTo(1.0f);
        }
    }

    /**
     * Adjusts the active zoom multiplier when scrolling the mouse wheel in-game.
     */
    public boolean onMouseScroll(double verticalAmount) {
        if (!isEnabled() || !scrollZoomSetting.isEnabled() || verticalAmount == 0.0) {
            return false;
        }
        float step = verticalAmount > 0 ? 1.15f : 0.87f;
        dynamicScrollMultiplier = MathUtils.clamp(dynamicScrollMultiplier * step, 0.4f, 5.0f);
        float target = MathUtils.clamp(defaultZoomLevelSetting.getFloatValue() * dynamicScrollMultiplier, 1.2f, 25.0f);
        if (smoothZoomSetting.isEnabled()) {
            zoomAnimation.animateTo(target);
        } else {
            zoomAnimation.snapTo(target);
        }
        return true;
    }

    /**
     * Returns the current FOV divisor (>= 1.0f).
     */
    public float getFovDivisor() {
        float val = zoomAnimation.getValue();
        if (!isEnabled() && val <= 1.01f) {
            return 1.0f;
        }
        return Math.max(1.0f, val);
    }

    public float getSensitivityMultiplier() {
        if (!scaleMouseSensitivitySetting.isEnabled()) {
            return 1.0f;
        }
        return 1.0f / getFovDivisor();
    }
}
