package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class PvpProfileModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public PvpProfileModule() {
        super("pvp_profile", "PvP Profile", "One-click preset enabling CPS, Armor Status, Low Fire, Small Items & Sprint.", ModuleCategory.COMBAT, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Enable Combat HUDs", "One-click preset enabling CPS, Armor Status, Low Fire, Small Items & Sprint.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Enable Low Fire & Clear Water", "Additional control for PvP Profile", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "PvP Profile Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
