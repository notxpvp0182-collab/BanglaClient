package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class EnchantmentTooltipModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public EnchantmentTooltipModule() {
        super("enchantment_tooltip", "Enchantment Tooltip", "Highlights max-level enchantments and shows clean levels.", ModuleCategory.INVENTORY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Highlight Max Enchants", "Highlights max-level enchantments and shows clean levels.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Numeric Levels (1-5)", "Additional control for Enchantment Tooltip", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Enchantment Tooltip Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
