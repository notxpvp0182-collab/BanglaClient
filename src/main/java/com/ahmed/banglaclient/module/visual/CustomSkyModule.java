package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.ColorSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Module 31: Custom Sky
 * Customizes visual sky appearance and optional client-side visual time lock (day/sunset/midnight aesthetics).
 */
public final class CustomSkyModule extends Module {
    private final BooleanSetting customSkyColorSetting;
    private final ColorSetting zenithColorSetting;
    private final BooleanSetting lockVisualTimeSetting;
    private final NumberSetting visualTimeTicksSetting;

    public CustomSkyModule() {
        super(
                "custom_sky",
                "Custom Sky",
                "Customizes visual sky color and optional client-side visual time-of-day.",
                ModuleCategory.VISUAL
        );
        this.customSkyColorSetting = addSetting(new BooleanSetting(
                "custom_color", "Custom Sky Tint", "Apply a custom RGB tint to the Overworld sky dome", true
        ));
        this.zenithColorSetting = addSetting(new ColorSetting(
                "zenith_color", "Sky Color", "Primary color of the sky dome", 0xFF0F172A, false
        )).visibleWhen(customSkyColorSetting::isEnabled) instanceof ColorSetting cs ? cs : null;
        this.lockVisualTimeSetting = addSetting(new BooleanSetting(
                "lock_time", "Custom Visual Time", "Override client visual sun/moon angle (does not alter server time)", false
        ));
        this.visualTimeTicksSetting = addSetting(new NumberSetting(
                "time_ticks", "Time of Day (Ticks)", "6000 = Noon, 13000 = Sunset, 18000 = Midnight",
                13000.0, 0.0, 24000.0, 500.0, "t"
        )).visibleWhen(lockVisualTimeSetting::isEnabled) instanceof NumberSetting ns ? ns : null;
        addAliases("sky", "skycolor", "timechanger", "ambience");
    }

    public boolean isCustomSkyColorActive() {
        return isEnabled() && customSkyColorSetting.isEnabled();
    }

    public int getSkyColorRgb() {
        return zenithColorSetting != null ? (zenithColorSetting.getArgb() & 0x00FFFFFF) : 0x0F172A;
    }

    public boolean isVisualTimeLocked() {
        return isEnabled() && lockVisualTimeSetting.isEnabled();
    }

    public long getVisualTimeTicks() {
        return visualTimeTicksSetting != null ? visualTimeTicksSetting.getIntValue() : 6000L;
    }
}
