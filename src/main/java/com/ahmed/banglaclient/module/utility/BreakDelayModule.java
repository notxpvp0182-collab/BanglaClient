package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Separate "Break Delay" module controlling client-side post-break cooldown ticks
 * while strictly preserving vanilla block-breaking speed and server validation.
 */
public final class BreakDelayModule extends Module {
    private final NumberSetting breakCooldownTicksSetting;

    public BreakDelayModule() {
        super(
                "break_delay",
                "Break Delay",
                "Independently configures the client-side delay between breaking consecutive blocks.",
                ModuleCategory.UTILITY
        );
        this.breakCooldownTicksSetting = addSetting(new NumberSetting(
                "break_ticks", "Break Delay Ticks", "Cooldown ticks between breaking blocks (vanilla = 5)",
                1.0, 0.0, 5.0, 1.0, "t"
        ));
        addAliases("breakdelay", "minedelay", "nohitdelay");
    }

    public int getBreakCooldownTicks() {
        return breakCooldownTicksSetting.getIntValue();
    }
}
