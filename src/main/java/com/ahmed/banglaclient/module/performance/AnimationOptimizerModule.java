package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import net.minecraft.client.option.CloudRenderMode;

/**
 * Module 106: Animation Optimizer
 * Allows disabling selected optional visual animations and decorative client effects
 * (e.g., UI transition motion, view bobbing, and fancy cloud rendering) to reduce GPU/CPU load on low-end systems.
 */
public final class AnimationOptimizerModule extends Module {
    private final BooleanSetting reduceUiAnimationsSetting;
    private final BooleanSetting disableViewBobbingSetting;
    private final BooleanSetting optimizeCloudsSetting;

    public AnimationOptimizerModule() {
        super(
                "animation_optimizer",
                "Animation Optimizer",
                "Allows disabling optional visual and UI animations to reduce system overhead.",
                ModuleCategory.PERFORMANCE
        );
        this.reduceUiAnimationsSetting = addSetting(new BooleanSetting(
                "reduce_ui_motion", "Fast UI Transitions", "Enable reduced-motion instant UI transitions in BANGLA CLIENT", true
        ));
        this.disableViewBobbingSetting = addSetting(new BooleanSetting(
                "disable_bobbing", "Disable View Bobbing", "Turn off camera view bobbing while walking", false
        ));
        this.optimizeCloudsSetting = addSetting(new BooleanSetting(
                "fast_clouds", "Use Fast Clouds", "Switch cloud rendering from Fancy to Fast", false
        ));
        addAliases("animations", "fastanimations", "lowend", "optimizer");
    }

    @Override
    protected void onEnable() {
        applySettings();
    }

    @Override
    protected void onDisable() {
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getConfigManager() != null) {
            BanglaClient.getInstance().getConfigManager().setReducedMotion(false);
        }
    }

    @Override
    public void onTick() {
        applySettings();
    }

    private void applySettings() {
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getConfigManager() != null) {
            boolean wantReduced = reduceUiAnimationsSetting.isEnabled();
            if (BanglaClient.getInstance().getConfigManager().isReducedMotion() != wantReduced) {
                BanglaClient.getInstance().getConfigManager().setReducedMotion(wantReduced);
            }
        }
        if (client == null || client.options == null) {
            return;
        }
        if (disableViewBobbingSetting.isEnabled() && Boolean.TRUE.equals(client.options.getBobView().getValue())) {
            client.options.getBobView().setValue(false);
        }
        if (optimizeCloudsSetting.isEnabled() && client.options.getCloudRenderMode().getValue() == CloudRenderMode.FANCY) {
            client.options.getCloudRenderMode().setValue(CloudRenderMode.FAST);
        }
    }
}
