package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Separate "Left Click Delay" module controlling client-side missed-swing attackCooldown penalty ticks
 * while preserving weapon recharge cooldowns and server hit validation.
 */
public final class LeftClickDelayModule extends Module {
    private final NumberSetting missPenaltyTicksSetting;

    public LeftClickDelayModule() {
        super(
                "left_click_delay",
                "Left Click Delay",
                "Independently configures client-side left-click miss-penalty delay ticks.",
                ModuleCategory.UTILITY
        );
        this.missPenaltyTicksSetting = addSetting(new NumberSetting(
                "lc_ticks", "Miss Delay Cap (Ticks)", "Maximum client miss-penalty ticks after swinging at air (vanilla = 10)",
                0.0, 0.0, 10.0, 1.0, "t"
        ));
        addAliases("leftclickdelay", "hitdelay", "misspenalty");
    }

    public int getMissPenaltyTicks() {
        return missPenaltyTicksSetting.getIntValue();
    }
}
