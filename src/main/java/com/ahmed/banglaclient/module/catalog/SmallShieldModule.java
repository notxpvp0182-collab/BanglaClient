package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class SmallShieldModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public SmallShieldModule() {
        super("small_shield", "Small Shield", "Lowers and shrinks held Shield so it never blocks your screen.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Lower Blocking Shield", "Lowers and shrinks held Shield so it never blocks your screen.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Shrink Idle Shield", "Additional control for Small Shield", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Small Shield Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
