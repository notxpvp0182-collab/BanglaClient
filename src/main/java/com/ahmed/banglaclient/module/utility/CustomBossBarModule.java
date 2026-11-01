package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Module 114: Custom Boss Bar
 * Customizes boss bar appearance, vertical spacing, or allows hiding boss bars when multiple servers stack them.
 */
public final class CustomBossBarModule extends Module {
    private final BooleanSetting hideBossBarsSetting;
    private final BooleanSetting onlyShowTextSetting;
    private final NumberSetting maxVisibleBarsSetting;

    public CustomBossBarModule() {
        super(
                "custom_boss_bar",
                "Custom Boss Bar",
                "Customizes top-of-screen boss bar visibility, count limit, and compact styling.",
                ModuleCategory.UTILITY
        );
        this.hideBossBarsSetting = addSetting(new BooleanSetting(
                "hide_all", "Hide Boss Bars Completely", "Suppress boss bar rendering when servers use them for ads", false
        ));
        this.onlyShowTextSetting = addSetting(new BooleanSetting(
                "compact_text_only", "Compact Title Only", "Hide the bar graphic and only show the boss bar title", false
        ));
        this.maxVisibleBarsSetting = addSetting(new NumberSetting(
                "max_bars", "Max Visible Boss Bars", "Maximum number of stacked boss bars allowed on screen",
                2.0, 1.0, 6.0, 1.0, ""
        ));
        addAliases("bossbar", "nobossbar", "witherbar", "dragonbar");
    }

    public boolean shouldHideBossBars() {
        return isEnabled() && hideBossBarsSetting.isEnabled();
    }

    public boolean isCompactTextOnly() {
        return isEnabled() && onlyShowTextSetting.isEnabled();
    }

    public int getMaxVisibleBars() {
        return isEnabled() ? maxVisibleBarsSetting.getIntValue() : 5;
    }
}
