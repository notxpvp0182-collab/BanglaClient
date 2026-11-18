package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;

public final class SignRenderingOptimizerModule extends Module {
    private final NumberSetting maxDistanceSetting;
    private final BooleanSetting alwaysRenderNearbySetting;

    public SignRenderingOptimizerModule() {
        super("sign_rendering_optimizer", "Sign Rendering Optimizer",
                "Skips rendering distant sign block entities beyond a configurable distance to boost FPS.",
                ModuleCategory.PERFORMANCE);
        this.maxDistanceSetting = addSetting(new NumberSetting("max_dist", "Max Sign Render Distance", "Maximum distance in blocks to render signs", 24.0, 8.0, 64.0, 4.0, "m"));
        this.alwaysRenderNearbySetting = addSetting(new BooleanSetting("always_near", "Always Render Within 8m", "Never cull signs within 8 blocks of the player", true));
        addAliases("signoptimizer", "nosigntext", "signculling");
    }

    public boolean shouldSkipSign(BlockEntity be) {
        if (!isEnabled() || !(be instanceof SignBlockEntity) || client == null || client.player == null) return false;
        double distSq = client.player.squaredDistanceTo(be.getPos().toCenterPos());
        if (alwaysRenderNearbySetting.isEnabled() && distSq <= 64.0) return false;
        double max = maxDistanceSetting.getValue();
        return distSq > (max * max);
    }
}
