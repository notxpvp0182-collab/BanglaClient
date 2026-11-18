package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class BcNametagIconModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public BcNametagIconModule() {
        super("bc_nametag_icon", "BC Nametag Icon", "Displays the official BanglaClient Crown Icon on player nametags & tablist.", ModuleCategory.VISUAL, -1, true);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Show Above Head (F5)", "Displays the official BanglaClient Crown Icon on player nametags & tablist.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Show in Tablist", "Additional control for BC Nametag Icon", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "BC Nametag Icon Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
