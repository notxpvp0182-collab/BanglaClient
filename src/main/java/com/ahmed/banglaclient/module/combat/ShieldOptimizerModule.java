package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * "Shield Optimizer" module.
 * Optimizes first-person shield view offset and visual raise responsiveness
 * without altering server-side blocking validation.
 */
public final class ShieldOptimizerModule extends Module {
    private final NumberSetting lowerShieldOffsetSetting;
    private final BooleanSetting smoothRaiseVisualSetting;

    public ShieldOptimizerModule() {
        super(
                "shield_optimizer",
                "Shield Optimizer",
                "Lowers first-person shield view so it never blocks your screen and optimizes visual responsiveness.",
                ModuleCategory.COMBAT
        );
        this.lowerShieldOffsetSetting = addSetting(new NumberSetting(
                "lower_offset", "Lower Shield View Offset", "Vertical Y-offset for held shields in first-person view",
                -0.18, -0.45, 0.0, 0.02, ""
        ));
        this.smoothRaiseVisualSetting = addSetting(new BooleanSetting(
                "smooth_raise", "Responsive Raise Visual", "Reduce visual equip jitter when raising a shield", true
        ));
        addAliases("lowshield", "shieldopt", "shieldview");
    }

    public float getShieldVerticalOffset() {
        return isEnabled() ? lowerShieldOffsetSetting.getFloatValue() : 0.0f;
    }

    public boolean isSmoothRaiseEnabled() {
        return isEnabled() && smoothRaiseVisualSetting.isEnabled();
    }
}
