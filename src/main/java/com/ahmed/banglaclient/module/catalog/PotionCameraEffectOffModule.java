package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class PotionCameraEffectOffModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public PotionCameraEffectOffModule() {
        super("potion_camera_effect_off", "Potion Camera Effect Off", "Disables Nausea distortion and Darkness pulsing camera effects.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Disable Nausea Wave", "Disables Nausea distortion and Darkness pulsing camera effects.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Disable Darkness Pulse", "Additional control for Potion Camera Effect Off", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Potion Camera Effect Off Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
