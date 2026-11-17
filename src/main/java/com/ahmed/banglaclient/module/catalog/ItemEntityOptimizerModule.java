package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ItemEntityOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ItemEntityOptimizerModule() {
        super("item_entity_optimizer", "Item Entity Optimizer", "Optimizes ground item rotation and physics calculations.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Static Far Ground Items", "Optimizes ground item rotation and physics calculations.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Batch Item Shadows", "Additional control for Item Entity Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Item Entity Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
