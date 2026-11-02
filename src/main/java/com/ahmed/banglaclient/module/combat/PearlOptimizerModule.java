package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

/**
 * "Pearl Optimizer" module.
 * Improves client-side Ender Pearl use responsiveness while strictly preserving
 * vanilla cooldowns and server projectile validation.
 */
public final class PearlOptimizerModule extends Module {
    private final BooleanSetting reduceUseDelaySetting;
    private final BooleanSetting strictCooldownGuardSetting;

    public PearlOptimizerModule() {
        super(
                "pearl_optimizer",
                "Pearl Optimizer",
                "Improves Ender Pearl use responsiveness while respecting cooldowns and server validation.",
                ModuleCategory.COMBAT
        );
        this.reduceUseDelaySetting = addSetting(new BooleanSetting(
                "responsive_use", "Responsive Pearl Use", "Remove redundant client-side right-click delay when holding an Ender Pearl", true
        ));
        this.strictCooldownGuardSetting = addSetting(new BooleanSetting(
                "respect_cooldown", "Strict Cooldown Guard", "Never attempt item use while Ender Pearl is on vanilla cooldown", true
        ));
        addAliases("pearlopt", "fastpearl", "enderpearlopt");
    }

    public boolean shouldOptimizePearlUse() {
        return isEnabled() && reduceUseDelaySetting.isEnabled();
    }

    public boolean isStrictCooldownGuard() {
        return strictCooldownGuardSetting.isEnabled();
    }
}
