package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class FireRenderingControlModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public FireRenderingControlModule() {
        super("fire_rendering_control", "Fire Rendering Control", "Controls world fire and soul fire flame height and opacity.", ModuleCategory.WORLD, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Low Ground Fire", "Controls world fire and soul fire flame height and opacity.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Soul Fire Tint", "Additional control for Fire Rendering Control", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Fire Rendering Control Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
