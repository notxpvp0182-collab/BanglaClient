package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Separate "Place Delay" module controlling client-side block placement cooldown ticks.
 */
public final class PlaceDelayModule extends Module {
    private final NumberSetting delayTicksSetting;

    public PlaceDelayModule() {
        super(
                "place_delay",
                "Place Delay",
                "Independently configures client-side block placement delay ticks.",
                ModuleCategory.UTILITY
        );
        this.delayTicksSetting = addSetting(new NumberSetting(
                "delay_ticks", "Place Delay Ticks", "Maximum client cooldown ticks after placing a block (vanilla = 4)",
                1.0, 0.0, 4.0, 1.0, "t"
        ));
        addAliases("placedelay", "blockdelay");
    }

    public int getDelayTicks() {
        return delayTicksSetting.getIntValue();
    }
}
