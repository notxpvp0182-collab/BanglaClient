package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ItemFovControlModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ItemFovControlModule() {
        super("item_fov_control", "Item FOV Control", "Controls FOV zoom when pulling a bow or using a spyglass.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Custom Bow Zoom", "Controls FOV zoom when pulling a bow or using a spyglass.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Keep Crosshair Scale", "Additional control for Item FOV Control", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Item FOV Control Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
