package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class CustomFovModule extends Module {
    private final BooleanSetting overrideBaseFov;
    private final NumberSetting customFovValue;
    private final BooleanSetting disableSprintFov;
    private final BooleanSetting disableSpeedPotionFov;
    private final BooleanSetting disableBowZoomFov;
    private final BooleanSetting noViewBobbing;

    public CustomFovModule() {
        super("custom_fov", "Custom FOV & Bobbing", "Controls base FOV, Sprint FOV, Speed Potion FOV, Bow FOV, and No View Bobbing.", ModuleCategory.VISUAL);
        this.overrideBaseFov = addSetting(new BooleanSetting("override_fov", "Enable Custom Base FOV", "Override vanilla FOV with custom value", false));
        this.customFovValue = addSetting(new NumberSetting("fov_value", "Custom FOV", "Preferred Field of View angle", 90.0, 30.0, 130.0, 1.0, "°"));
        this.disableSprintFov = addSetting(new BooleanSetting("no_sprint_fov", "Static Sprint FOV", "Prevent FOV widening when sprinting", false));
        this.disableSpeedPotionFov = addSetting(new BooleanSetting("no_speed_fov", "Potion Camera Effect Off", "Prevent Speed/Slowness potions from altering FOV", false));
        this.disableBowZoomFov = addSetting(new BooleanSetting("no_item_fov", "Item FOV Control", "Prevent bow/spyglass from shifting FOV", false));
        this.noViewBobbing = addSetting(new BooleanSetting("no_bobbing", "No View Bobbing", "Disable screen bobbing while walking", false));
        addAliases("fov", "sprintfov", "speedfov", "itemfov", "noviewbobbing", "bobbing", "potioncamera");
    }

    @Override
    public void onTick() {
        if (noViewBobbing.isEnabled() && client != null && client.options != null) {
            client.options.getBobView().setValue(false);
        }
    }

    public float modifyFov(float currentFov) {
        if (!isEnabled()) return currentFov;
        if (overrideBaseFov.isEnabled()) return customFovValue.getFloatValue();
        if (disableSprintFov.isEnabled() && client != null && client.player != null && client.player.isSprinting()) {
            return currentFov * 0.90f;
        }
        return currentFov;
    }
}
