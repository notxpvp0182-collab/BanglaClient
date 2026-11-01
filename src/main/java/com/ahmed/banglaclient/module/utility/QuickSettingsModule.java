package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 112: Quick Settings
 * Provides quick access to frequently used client settings (cycling Performance Preset, cycling Liquid Glass Theme,
 * and toggling HUD visibility) and displays a compact status summary card.
 */
public final class QuickSettingsModule extends HudModule {
    private final BooleanSetting cyclePresetActionSetting;
    private final BooleanSetting cycleThemeActionSetting;
    private final BooleanSetting toggleHudVisibilitySetting;

    public QuickSettingsModule() {
        super(
                "quick_settings",
                "Quick Settings",
                "Provides quick access to graphics presets, themes, and global HUD visibility.",
                ModuleCategory.UTILITY,
                10.0f,
                418.0f,
                148.0f,
                32.0f,
                false
        );
        this.cyclePresetActionSetting = addSetting(new BooleanSetting(
                "cycle_preset", "Action: Cycle Graphics Preset", "Toggle ON to switch between Low, Balanced, and High glass modes", false
        ));
        this.cyclePresetActionSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val) && BanglaClient.getInstance() != null) {
                BanglaClient.getInstance().setPerformancePreset(BanglaClient.getInstance().getPerformancePreset().next());
                cyclePresetActionSetting.setValue(false);
            }
        });

        this.cycleThemeActionSetting = addSetting(new BooleanSetting(
                "cycle_theme", "Action: Cycle Liquid Theme", "Toggle ON to cycle to the next built-in Liquid Glass theme", false
        ));
        this.cycleThemeActionSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val) && BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null) {
                BanglaClient.getInstance().getThemeManager().cycleNextTheme();
                cycleThemeActionSetting.setValue(false);
            }
        });

        this.toggleHudVisibilitySetting = addSetting(new BooleanSetting(
                "hud_visible", "Global HUD Widgets Visible", "Master visibility switch for all BANGLA CLIENT HUD widgets", true
        ));
        this.toggleHudVisibilitySetting.onChange(visible -> {
            if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getHudManager() != null) {
                BanglaClient.getInstance().getHudManager().setHudVisible(Boolean.TRUE.equals(visible));
            }
        });
        addAliases("quick", "controlcenter", "fastsettings");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        Theme theme = GlassRenderer.getTheme();
        String preset = BanglaClient.getInstance() != null
                ? BanglaClient.getInstance().getPerformancePreset().getDisplayName()
                : "Balanced";

        String line1 = "Theme: " + theme.getDisplayName();
        String line2 = "Preset: " + preset + " • Profile: "
                + (BanglaClient.getInstance() != null ? BanglaClient.getInstance().getProfileManager().getActiveProfileName() : "Default");

        int pad = paddingSetting.getIntValue();
        int boxW = Math.max(client.textRenderer.getWidth(line1), client.textRenderer.getWidth(line2)) + pad * 2;
        int boxH = 22 + pad * 2;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        context.drawTextWithShadow(client.textRenderer, line1, x + pad, y + pad + 1, textColorSetting.getArgb());
        context.drawTextWithShadow(client.textRenderer, line2, x + pad, y + pad + 12, theme.getTextSecondary());
    }
}
