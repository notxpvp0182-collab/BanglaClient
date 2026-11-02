package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Separate "Right Click Delay" module controlling client-side item-use cooldown ticks.
 */
public final class RightClickDelayModule extends Module {
    private final NumberSetting rightClickTicksSetting;

    public RightClickDelayModule() {
        super(
                "right_click_delay",
                "Right Click Delay",
                "Independently configures client-side right-click item interaction delay ticks.",
                ModuleCategory.UTILITY
        );
        this.rightClickTicksSetting = addSetting(new NumberSetting(
                "rc_ticks", "Right-Click Delay Ticks", "Maximum client item-use cooldown ticks (vanilla = 4)",
                2.0, 1.0, 4.0, 1.0, "t"
        ));
        addAliases("rightclickdelay", "itemusedelay");
    }

    public int getRightClickTicks() {
        return rightClickTicksSetting.getIntValue();
    }
}
