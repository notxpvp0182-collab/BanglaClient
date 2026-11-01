package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Module 113: Custom Scoreboard
 * Customizes the vanilla sidebar scoreboard appearance (hiding red score numbers, applying Liquid Glass background,
 * or adjusting vertical offset) without modifying any server scoreboard data.
 */
public final class CustomScoreboardModule extends Module {
    private final BooleanSetting hideScoreNumbersSetting;
    private final BooleanSetting hideScoreboardEntirelySetting;
    private final BooleanSetting useGlassBackgroundSetting;
    private final NumberSetting verticalOffsetSetting;

    public CustomScoreboardModule() {
        super(
                "custom_scoreboard",
                "Custom Scoreboard",
                "Customizes sidebar scoreboard appearance, red score numbers, and vertical position.",
                ModuleCategory.UTILITY
        );
        this.hideScoreNumbersSetting = addSetting(new BooleanSetting(
                "hide_numbers", "Hide Red Score Numbers", "Hide the red numeric values on the right side of the scoreboard", true
        ));
        this.hideScoreboardEntirelySetting = addSetting(new BooleanSetting(
                "hide_all", "Hide Scoreboard Completely", "Do not render the sidebar scoreboard on screen", false
        ));
        this.useGlassBackgroundSetting = addSetting(new BooleanSetting(
                "glass_bg", "Liquid Glass Background", "Render a rounded Liquid Glass panel behind the scoreboard", true
        ));
        this.verticalOffsetSetting = addSetting(new NumberSetting(
                "y_offset", "Vertical Offset", "Shift the sidebar scoreboard up or down in pixels",
                0.0, -100.0, 100.0, 2.0, "px"
        ));
        addAliases("scoreboard", "sidebar", "noscoreboard");
    }

    public boolean shouldHideScoreNumbers() {
        return isEnabled() && hideScoreNumbersSetting.isEnabled();
    }

    public boolean shouldHideEntirely() {
        return isEnabled() && hideScoreboardEntirelySetting.isEnabled();
    }

    public boolean shouldUseGlassBackground() {
        return isEnabled() && useGlassBackgroundSetting.isEnabled();
    }

    public int getVerticalOffsetPx() {
        return isEnabled() ? verticalOffsetSetting.getIntValue() : 0;
    }
}
