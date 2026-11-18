package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class AttackIndicatorCustomizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public AttackIndicatorCustomizerModule() {
        super("attack_indicator_customizer", "Attack Indicator Customizer", "Customizes the style, color, and position of the attack indicator.", ModuleCategory.COMBAT, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Highlight Full Charge", "Customizes the style, color, and position of the attack indicator.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Scale Indicator", "Additional control for Attack Indicator Customizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Attack Indicator Customizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
