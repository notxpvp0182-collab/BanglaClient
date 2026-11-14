package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.util.MathUtils;
import org.lwjgl.glfw.GLFW;

/**
 * Advanced Infinite Smooth Zoom Module:
 * - Only activates when the module is ENABLED in ClickGUI and the user holds the Zoom keybind (default: C).
 * - Supports Infinite Scroll Zoom while holding the Zoom key (Scroll Up = zoom in infinitely, Scroll Down = zoom out).
 * - Uses per-frame exponential damping for 100% butter-smooth transitions at any framerate.
 */
public final class ZoomModule extends Module {
    private final NumberSetting defaultZoomLevelSetting;
    private final BooleanSetting scrollZoomSetting;
    private final BooleanSetting infiniteZoomSetting;
    private final NumberSetting scrollSpeedSetting;
    private final NumberSetting smoothSpeedSetting;
    private final BooleanSetting smoothTransitionSetting;
    private final BooleanSetting scaleMouseSensitivitySetting;
    private final BooleanSetting cinematicCameraSetting;
    private final BooleanSetting hideHandSetting;
    private final BooleanSetting showZoomHudSetting;

    private boolean holdingZoomKey = false;
    private boolean wasHoldingZoomKey = false;
    private boolean savedSmoothCameraState = false;
    private float targetZoomDivisor = 1.0f;
    private float currentZoomDivisor = 1.0f;
    private float scrollMultiplier = 1.0f;
    private long lastFrameNanos = System.nanoTime();

    public ZoomModule() {
        super(
                "zoom",
                "Zoom",
                "Infinite smooth camera zoom with scroll-wheel magnification and sensitivity scaling.",
                ModuleCategory.VISUAL,
                GLFW.GLFW_KEY_C,
                true
        );
        setTriggerKeybindWhileEnabledOnly(true);

        this.defaultZoomLevelSetting = addSetting(new NumberSetting(
                "zoom_factor", "Base Zoom Level", "Initial magnification when pressing the zoom key",
                4.0, 1.5, 20.0, 0.5, "x"
        ));
        this.scrollZoomSetting = addSetting(new BooleanSetting(
                "scroll_adjust", "Mouse Wheel Scroll Zoom", "Scroll up/down while holding zoom key to adjust magnification", true
        ));
        this.infiniteZoomSetting = addSetting(new BooleanSetting(
                "infinite_zoom", "Infinite / Unlimited Zoom", "Allow scrolling up to zoom in infinitely without a low cap", true
        ));
        this.scrollSpeedSetting = addSetting(new NumberSetting(
                "scroll_speed", "Scroll Zoom Speed", "How fast each mouse wheel scroll step changes the zoom",
                1.25, 0.5, 3.0, 0.05, "x"
        ));
        this.smoothTransitionSetting = addSetting(new BooleanSetting(
                "smooth_camera", "Smooth Zoom Animation", "Glide smoothly when zooming in, scrolling, and zooming out", true
        ));
        this.smoothSpeedSetting = addSetting(new NumberSetting(
                "smooth_speed", "Animation Smoothness", "Speed of the smooth zoom glide (lower = softer, higher = snappier)",
                14.0, 4.0, 30.0, 1.0, ""
        ));
        this.scaleMouseSensitivitySetting = addSetting(new BooleanSetting(
                "scale_sensitivity", "Smooth Mouse Sensitivity", "Proportionally slow down camera turn speed while zoomed in", true
        ));
        this.cinematicCameraSetting = addSetting(new BooleanSetting(
                "cinematic_cam", "Cinematic Glide Camera", "Enable cinematic camera inertia while holding zoom", false
        ));
        this.hideHandSetting = addSetting(new BooleanSetting(
                "hide_hand", "Hide Hand While Zooming", "Hide first-person held item while zoomed in", false
        ));
        this.showZoomHudSetting = addSetting(new BooleanSetting(
                "show_zoom_hud", "Show Zoom Level Badge", "Display current magnification (e.g. 12.5x) above hotbar while zooming", true
        ));
        addAliases("magnifying", "optic", "spyglass", "fov", "infinitezoom");
    }

    @Override
    protected void onDisable() {
        holdingZoomKey = false;
        wasHoldingZoomKey = false;
        scrollMultiplier = 1.0f;
        targetZoomDivisor = 1.0f;
        restoreCinematicCamera();
    }

    @Override
    public void onKeybindHoldState(boolean keyDown) {
        if (!isEnabled()) {
            this.holdingZoomKey = false;
            return;
        }
        this.holdingZoomKey = keyDown;
        if (this.holdingZoomKey && !this.wasHoldingZoomKey) {
            this.scrollMultiplier = 1.0f;
            this.targetZoomDivisor = defaultZoomLevelSetting.getFloatValue();
            if (cinematicCameraSetting.isEnabled() && client != null && client.options != null) {
                this.savedSmoothCameraState = client.options.smoothCameraEnabled;
                client.options.smoothCameraEnabled = true;
            }
        } else if (!this.holdingZoomKey && this.wasHoldingZoomKey) {
            this.scrollMultiplier = 1.0f;
            this.targetZoomDivisor = 1.0f;
            restoreCinematicCamera();
        }
        this.wasHoldingZoomKey = this.holdingZoomKey;
    }

    private void restoreCinematicCamera() {
        if (cinematicCameraSetting.isEnabled() && client != null && client.options != null) {
            client.options.smoothCameraEnabled = this.savedSmoothCameraState;
        }
    }

    public boolean onMouseScroll(double verticalAmount) {
        if (!isEnabled() || !holdingZoomKey || !scrollZoomSetting.isEnabled() || verticalAmount == 0.0) {
            return false;
        }
        float stepPower = 0.14f * scrollSpeedSetting.getFloatValue();
        float factor = verticalAmount > 0 ? (1.0f + stepPower) : (1.0f / (1.0f + stepPower));
        float maxMult = infiniteZoomSetting.isEnabled() ? 250.0f : 6.0f;

        scrollMultiplier = MathUtils.clamp(scrollMultiplier * factor, 0.25f, maxMult);
        float maxTotalZoom = infiniteZoomSetting.isEnabled() ? 1000.0f : 35.0f;
        targetZoomDivisor = MathUtils.clamp(defaultZoomLevelSetting.getFloatValue() * scrollMultiplier, 1.05f, maxTotalZoom);
        return true;
    }

    /**
     * Evaluates and returns the per-frame interpolated FOV divisor.
     */
    public float getFovDivisor() {
        long now = System.nanoTime();
        float deltaSec = MathUtils.clamp((now - lastFrameNanos) / 1_000_000_000.0f, 0.0005f, 0.1f);
        lastFrameNanos = now;

        float desired = (isEnabled() && holdingZoomKey) ? targetZoomDivisor : 1.0f;
        if (!smoothTransitionSetting.isEnabled()) {
            currentZoomDivisor = desired;
        } else {
            float speed = smoothSpeedSetting.getFloatValue();
            // Interpolate in logarithmic space so zooming at 50x-500x feels just as smooth as 4x!
            float logCurrent = (float) Math.log(Math.max(1.0f, currentZoomDivisor));
            float logTarget = (float) Math.log(Math.max(1.0f, desired));
            float dampedLog = MathUtils.damp(logCurrent, logTarget, speed, deltaSec);
            currentZoomDivisor = (float) Math.exp(dampedLog);
            if (Math.abs(currentZoomDivisor - desired) < 0.01f) {
                currentZoomDivisor = desired;
            }
        }
        return Math.max(1.0f, currentZoomDivisor);
    }

    public boolean isActivelyZooming() {
        return isEnabled() && (holdingZoomKey || currentZoomDivisor > 1.04f);
    }

    public boolean shouldHideHandWhileZooming() {
        return isActivelyZooming() && hideHandSetting.isEnabled();
    }

    public boolean shouldShowZoomHud() {
        return isActivelyZooming() && showZoomHudSetting.isEnabled();
    }

    public float getSensitivityMultiplier() {
        if (!scaleMouseSensitivitySetting.isEnabled() || !isActivelyZooming()) {
            return 1.0f;
        }
        return 1.0f / Math.max(1.0f, currentZoomDivisor);
    }
}
