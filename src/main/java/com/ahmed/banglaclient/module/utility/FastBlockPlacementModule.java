package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * "Fast Block Placement" module.
 * Improves legitimate client-side block placement responsiveness while holding a BlockItem,
 * with strict anti-packet-spam protection.
 */
public final class FastBlockPlacementModule extends Module {
    private final NumberSetting targetDelayTicksSetting;
    private final BooleanSetting onlyBuildingBlocksSetting;

    public FastBlockPlacementModule() {
        super(
                "fast_block_placement",
                "Fast Block Placement",
                "Improves legitimate client-side block placement responsiveness without packet spam.",
                ModuleCategory.UTILITY
        );
        this.targetDelayTicksSetting = addSetting(new NumberSetting(
                "tick_delay", "Placement Tick Interval", "Minimum client ticks between consecutive block placements (vanilla = 4)",
                1.0, 1.0, 4.0, 1.0, "t"
        ));
        this.onlyBuildingBlocksSetting = addSetting(new BooleanSetting(
                "only_blocks", "Only For Block Items", "Only apply reduced placement interval when holding a placeable block", true
        ));
        addAliases("fastplace", "blockplacement", "builder");
    }

    public int getTargetDelayTicks() {
        return targetDelayTicksSetting.getIntValue();
    }

    public boolean isOnlyBuildingBlocks() {
        return onlyBuildingBlocksSetting.isEnabled();
    }
}
