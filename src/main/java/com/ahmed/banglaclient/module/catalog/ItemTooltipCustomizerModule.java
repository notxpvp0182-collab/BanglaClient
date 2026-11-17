package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ItemTooltipCustomizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ItemTooltipCustomizerModule() {
        super("item_tooltip_customizer", "Item Tooltip Customizer", "Styles item tooltips with dark glass borders and clean spacing.", ModuleCategory.INVENTORY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Glass Tooltip Frame", "Styles item tooltips with dark glass borders and clean spacing.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Show Item ID", "Additional control for Item Tooltip Customizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Item Tooltip Customizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
