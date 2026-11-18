package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;

public final class BeaconBeamOptimizerModule extends Module {
    private final NumberSetting maxDistanceSetting;
    private final BooleanSetting disableBeamsCompletelySetting;

    public BeaconBeamOptimizerModule() {
        super("beacon_beam_optimizer", "Beacon Beam Optimizer",
                "Limits beacon beam rendering distance or hides beacon beams to reduce GPU overdraw.",
                ModuleCategory.PERFORMANCE);
        this.maxDistanceSetting = addSetting(new NumberSetting("max_dist", "Max Beacon Render Distance", "Maximum distance in blocks to render beacons", 48.0, 16.0, 128.0, 8.0, "m"));
        this.disableBeamsCompletelySetting = addSetting(new BooleanSetting("hide_beams", "Hide All Beacon Beams", "Completely skip beacon block entity rendering", false));
        addAliases("beaconoptimizer", "nobeaconbeam", "beaconculling");
    }

    public boolean shouldSkipBeacon(BlockEntity be) {
        if (!isEnabled() || !(be instanceof BeaconBlockEntity) || client == null || client.player == null) return false;
        if (disableBeamsCompletelySetting.isEnabled()) return true;
        double max = maxDistanceSetting.getValue();
        return client.player.squaredDistanceTo(be.getPos().toCenterPos()) > (max * max);
    }
}
