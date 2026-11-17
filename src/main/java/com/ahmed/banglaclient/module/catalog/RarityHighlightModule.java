package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class RarityHighlightModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public RarityHighlightModule() {
        super("rarity_highlight", "Rarity Highlight", "Draws colored rarity backgrounds (Common, Rare, Epic) behind items.", ModuleCategory.INVENTORY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Show Hotbar Rarity", "Draws colored rarity backgrounds (Common, Rare, Epic) behind items.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Show Inventory Rarity", "Additional control for Rarity Highlight", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Rarity Highlight Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
