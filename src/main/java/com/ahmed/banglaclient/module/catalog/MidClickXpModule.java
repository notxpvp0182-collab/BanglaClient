package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class MidClickXpModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public MidClickXpModule() {
        super("mid_click_xp", "Mid Click → XP", "Middle click to throw Experience Bottles from your hotbar.", ModuleCategory.UTILITY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Throw While Held", "Middle click to throw Experience Bottles from your hotbar.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Return to Original Slot", "Additional control for Mid Click → XP", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Mid Click → XP Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
