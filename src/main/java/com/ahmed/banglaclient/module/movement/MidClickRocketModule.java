package com.ahmed.banglaclient.module.movement;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class MidClickRocketModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public MidClickRocketModule() {
        super("mid_click_rocket", "Mid Click → Rocket", "Middle click to fire a Firework Rocket from your hotbar.", ModuleCategory.MOVEMENT, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Only While Gliding", "Middle click to fire a Firework Rocket from your hotbar.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Return to Weapon Slot", "Additional control for Mid Click → Rocket", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Mid Click → Rocket Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
