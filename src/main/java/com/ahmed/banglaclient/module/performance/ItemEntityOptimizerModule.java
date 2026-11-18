package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Prevents FPS drops when hundreds or thousands of loot items are dropped on the ground.
 * Uses 3D spatial block-cell clumping so only a few items per block cell are rendered.
 */
public final class ItemEntityOptimizerModule extends Module {
    private final NumberSetting maxDropDistanceSetting;
    private final NumberSetting maxItemsPerBlockCellSetting;
    private final BooleanSetting staticGroundItemsSetting;
    private final BooleanSetting mergeStackVisualsSetting;

    public ItemEntityOptimizerModule() {
        super(
                "item_entity_optimizer",
                "Loot & Item Entity Optimizer",
                "Eliminates FPS drops from huge loot piles by limiting rendered items per block cell and culling distant drops.",
                ModuleCategory.PERFORMANCE,
                -1,
                true
        );
        this.maxDropDistanceSetting = addSetting(new NumberSetting(
                "item_dist", "Max Loot Render Distance", "Max distance in blocks to render dropped ground items",
                44.0, 12.0, 96.0, 4.0, "m"
        ));
        this.maxItemsPerBlockCellSetting = addSetting(new NumberSetting(
                "cell_limit", "Max Rendered Items Per Block", "Max items to render on the same block in a huge loot pile",
                4.0, 1.0, 16.0, 1.0, " items"
        ));
        this.staticGroundItemsSetting = addSetting(new BooleanSetting(
                "static_items", "Disable Loot Spin & Bobbing", "Stop ground items from spinning every frame to boost FPS", false
        ));
        this.mergeStackVisualsSetting = addSetting(new BooleanSetting(
                "merge_visuals", "Optimize Stacked Loot Models", "Reduce 3D multi-layer count on large item stacks", true
        ));
        addAliases("itemrenderingoptimizer", "droppeditems", "itemlag", "lootoptimizer", "massloot");
    }

    public double getMaxDistanceSq() {
        double d = maxDropDistanceSetting.getValue();
        return d * d;
    }

    public int getMaxItemsPerCell() {
        return maxItemsPerBlockCellSetting.getIntValue();
    }

    public boolean isStaticGroundItems() {
        return isEnabled() && staticGroundItemsSetting.isEnabled();
    }

    public boolean shouldMergeStackVisuals() {
        return isEnabled() && mergeStackVisualsSetting.isEnabled();
    }
}
