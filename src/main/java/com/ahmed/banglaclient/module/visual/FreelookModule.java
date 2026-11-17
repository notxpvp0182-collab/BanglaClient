package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import org.lwjgl.glfw.GLFW;

public final class FreelookModule extends Module {
    private final NumberSetting cameraDistanceSetting;
    private final BooleanSetting cameraSmoothingSetting;
    private final BooleanSetting collisionFixSetting;
    private final BooleanSetting thirdPerson3DViewSetting;

    private boolean freelooking = false;
    private float cameraYaw = 0.0f;
    private float cameraPitch = 0.0f;

    public FreelookModule() {
        super("freelook", "Freelook & Camera", "360° Freelook camera, third-person camera distance, smoothing, and 3D F5 view.",
                ModuleCategory.VISUAL, GLFW.GLFW_KEY_LEFT_ALT, false);
        setTriggerKeybindWhileEnabledOnly(true);
        this.cameraDistanceSetting = addSetting(new NumberSetting("cam_distance", "Camera Distance", "Third-person F5 camera distance in blocks", 4.0, 1.5, 12.0, 0.5, "m"));
        this.cameraSmoothingSetting = addSetting(new BooleanSetting("cam_smooth", "Camera Smoothing", "Smooth camera transitions in third-person", true));
        this.collisionFixSetting = addSetting(new BooleanSetting("collision_fix", "Camera Collision Fix", "Prevent sudden camera clipping on small blocks", true));
        this.thirdPerson3DViewSetting = addSetting(new BooleanSetting("f5_3d_view", "3D F5 View", "Enhanced third-person perspective", true));
        addAliases("camera", "cameradistance", "camerasmoothing", "3df5", "perspective");
    }

    @Override
    public void onKeybindHoldState(boolean keyDown) {
        if (!isEnabled() || client == null || client.player == null) {
            freelooking = false;
            return;
        }
        if (keyDown && !freelooking) {
            freelooking = true;
            cameraYaw = client.player.getYaw();
            cameraPitch = client.player.getPitch();
        } else if (!keyDown && freelooking) {
            freelooking = false;
        }
    }

    public boolean isFreelooking() { return isEnabled() && freelooking; }
    public float getCameraDistance() { return isEnabled() ? cameraDistanceSetting.getFloatValue() : 4.0f; }
    public float getCameraYaw() { return cameraYaw; }
    public float getCameraPitch() { return cameraPitch; }
}
