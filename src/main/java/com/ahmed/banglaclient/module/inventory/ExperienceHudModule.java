package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 53: Experience HUD
 * Displays current XP level, percentage progress to the next level, and optional total experience points.
 */
public final class ExperienceHudModule extends HudModule {
    private final BooleanSetting showTotalPointsSetting;

    public ExperienceHudModule() {
        super(
                "experience_hud",
                "Experience HUD",
                "Displays XP level, next-level percentage progress, and total experience points.",
                ModuleCategory.INVENTORY,
                590.0f,
                124.0f,
                108.0f,
                20.0f,
                false
        );
        this.showTotalPointsSetting = addSetting(new BooleanSetting(
                "show_total_xp", "Show Total XP Points", "Include cumulative totalExperience points", false
        ));
        addAliases("xp", "level", "levels", "exp");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int level = 30;
        float progress = 0.65f;
        int totalXp = 1395;

        if (client.player != null) {
            level = client.player.experienceLevel;
            progress = client.player.experienceProgress;
            totalXp = client.player.totalExperience;
        }

        int pct = Math.round(progress * 100.0f);
        String text = String.format("XP Lvl: %d (%d%%)", level, pct);
        if (showTotalPointsSetting.isEnabled()) {
            text += " [" + totalXp + " pts]";
        }

        int pad = paddingSetting.getIntValue();
        int boxW = client.textRenderer.getWidth(text) + pad * 2;
        int boxH = client.textRenderer.fontHeight + pad * 2;

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

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, ThemeTokens.STATUS_SUCCESS);
    }
}
