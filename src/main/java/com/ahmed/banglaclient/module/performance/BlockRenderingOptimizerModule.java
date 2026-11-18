package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import net.minecraft.block.entity.*;

public final class BlockRenderingOptimizerModule extends Module {
    private final NumberSetting maxBlockEntityDistSetting;
    private final BooleanSetting cullChestsSetting;
    private final BooleanSetting cullBannersAndSkullsSetting;

    public BlockRenderingOptimizerModule() {
        super("block_rendering_optimizer", "Block Rendering Optimizer",
                "Culls distant Chests, Banners, Skulls, and Shulker Boxes beyond a configurable distance.",
                ModuleCategory.PERFORMANCE);
        this.maxBlockEntityDistSetting = addSetting(new NumberSetting("max_dist", "Max BlockEntity Distance", "Render distance for complex block entities", 40.0, 16.0, 96.0, 4.0, "m"));
        this.cullChestsSetting = addSetting(new BooleanSetting("cull_chests", "Cull Distant Chests", "Skip chest rendering beyond max distance", true));
        this.cullBannersAndSkullsSetting = addSetting(new BooleanSetting("cull_banners", "Cull Distant Banners & Heads", "Skip banners and skulls beyond max distance", true));
        addAliases("blockoptimizer", "chestculling", "bannerculling");
    }

    public boolean shouldSkipBlockEntity(BlockEntity be) {
        if (!isEnabled() || be == null || client == null || client.player == null) return false;
        double max = maxBlockEntityDistSetting.getValue();
        double distSq = client.player.squaredDistanceTo(be.getPos().toCenterPos());
        if (distSq <= max * max) return false;
        if (cullChestsSetting.isEnabled() && (be instanceof ChestBlockEntity || be instanceof EnderChestBlockEntity || be instanceof ShulkerBoxBlockEntity)) {
            return true;
        }
        if (cullBannersAndSkullsSetting.isEnabled() && (be instanceof BannerBlockEntity || be instanceof SkullBlockEntity)) {
            return true;
        }
        return false;
    }
}
