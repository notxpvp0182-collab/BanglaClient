package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class SharpeningFilterModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public SharpeningFilterModule() {
        super("sharpening_filter", "Sharpening Filter", "Applies crisp contrast-adaptive sharpening to textures.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Crisp Block Textures", "Applies crisp contrast-adaptive sharpening to textures.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Zero Haloing", "Additional control for Sharpening Filter", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Sharpening Filter Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
