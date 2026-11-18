package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ItemEntityOptimizerModule extends Module {
    private final NumberSetting maxDropDistanceSetting;

    public ItemEntityOptimizerModule() {
        super("item_entity_optimizer", "Item Entity Optimizer",
                "Culls distant dropped ground items to prevent mass-drop FPS lag.",
                ModuleCategory.PERFORMANCE);
        this.maxDropDistanceSetting = addSetting(new NumberSetting("item_dist", "Max Dropped Item Distance", "Max distance in blocks to render dropped items", 40.0, 12.0, 96.0, 4.0, "m"));
        addAliases("itemrenderingoptimizer", "droppeditems", "itemlag");
    }

    public double getMaxDistanceSq() {
        double d = maxDropDistanceSetting.getValue();
        return d * d;
    }
}
