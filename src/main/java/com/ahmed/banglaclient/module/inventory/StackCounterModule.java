package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class StackCounterModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public StackCounterModule() {
        super("stack_counter", "Stack Counter", "Displays total count of the held item across your entire inventory.", ModuleCategory.INVENTORY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Include Offhand", "Displays total count of the held item across your entire inventory.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Show Stack Count", "Additional control for Stack Counter", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Stack Counter Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
