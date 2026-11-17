package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ItemRenderingOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ItemRenderingOptimizerModule() {
        super("item_rendering_optimizer", "Item Rendering Optimizer", "Optimizes dropped item and held item rendering.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Fast Dropped Items", "Optimizes dropped item and held item rendering.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Merge Nearby Stacks", "Additional control for Item Rendering Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Item Rendering Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
