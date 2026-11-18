package com.ahmed.banglaclient.module.movement;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class MidClickElytraModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public MidClickElytraModule() {
        super("mid_click_elytra", "Mid Click → Elytra", "Middle click to quickly swap between Chestplate and Elytra.", ModuleCategory.MOVEMENT, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Prefer Netherite/Diamond", "Middle click to quickly swap between Chestplate and Elytra.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Play Equip Sound", "Additional control for Mid Click → Elytra", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Mid Click → Elytra Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
