package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ItemHighlighterModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ItemHighlighterModule() {
        super("item_highlighter", "Item Highlighter", "Highlights searched or important PvP items inside containers.", ModuleCategory.INVENTORY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Highlight Totems & Gaps", "Highlights searched or important PvP items inside containers.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Pulse Border", "Additional control for Item Highlighter", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Item Highlighter Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
