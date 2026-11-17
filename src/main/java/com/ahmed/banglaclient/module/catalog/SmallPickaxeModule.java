package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class SmallPickaxeModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public SmallPickaxeModule() {
        super("small_pickaxe", "Small Pickaxe", "Reduces held Pickaxe size while mining.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Apply to All Pickaxes", "Reduces held Pickaxe size while mining.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Compact Mining Swing", "Additional control for Small Pickaxe", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Small Pickaxe Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
