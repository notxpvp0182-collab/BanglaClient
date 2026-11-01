package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 52: Health HUD
 * Displays numeric player health, absorption hearts, and an optional Liquid Glass health progress bar.
 */
public final class HealthHudModule extends HudModule {
    private final BooleanSetting showHeartsFormatSetting;
    private final BooleanSetting showHealthBarSetting;

    public HealthHudModule() {
        super(
                "health_hud",
                "Health HUD",
                "Displays numeric health, absorption, and a custom health bar.",
                ModuleCategory.INVENTORY,
                590.0f,
                92.0f,
                114.0f,
                26.0f,
                false
        );
        this.showHeartsFormatSetting = addSetting(new BooleanSetting(
                "hearts_mode", "Display in Hearts (10.0 ♥)", "Divide HP by 2 to show heart count instead of 20 HP points", false
        ));
        this.showHealthBarSetting = addSetting(new BooleanSetting(
                "show_bar", "Show Health Bar", "Render a color-coded health progress bar below the text", true
        ));
        addAliases("hp", "hearts", "absorption", "life");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        float hp = 20.0f;
        float maxHp = 20.0f;
        float absorption = 0.0f;

        if (client.player != null) {
            hp = client.player.getHealth();
            maxHp = Math.max(1.0f, client.player.getMaxHealth());
            absorption = client.player.getAbsorptionAmount();
        } else if (preview) {
            absorption = 4.0f;
        }

        String text;
        if (showHeartsFormatSetting.isEnabled()) {
            text = String.format("HP: %.1f / %.1f ♥", hp / 2.0f, maxHp / 2.0f);
            if (absorption > 0.0f) {
                text += String.format(" (+%.1f ♥)", absorption / 2.0f);
            }
        } else {
            text = String.format("HP: %.1f / %.0f", hp, maxHp);
            if (absorption > 0.0f) {
                text += String.format(" (+%.0f)", absorption);
            }
        }

        int pad = paddingSetting.getIntValue();
        int barExtra = showHealthBarSetting.isEnabled() ? 6 : 0;
        int boxW = Math.max(96, client.textRenderer.getWidth(text) + pad * 2);
        int boxH = client.textRenderer.fontHeight + pad * 2 + barExtra;

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

        float ratio = Math.max(0.0f, Math.min(1.0f, hp / maxHp));
        int color = ratio <= 0.25f
                ? ThemeTokens.STATUS_ERROR
                : (ratio <= 0.55f ? ThemeTokens.STATUS_WARNING : textColorSetting.getArgb());
        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, color);

        if (showHealthBarSetting.isEnabled()) {
            int barX = x + pad;
            int barY = y + boxH - pad - 3;
            int barW = boxW - pad * 2;
            context.fill(barX, barY, barX + barW, barY + 3, 0x44FFFFFF);

            int fillW = Math.round(barW * ratio);
            int barColor = ratio <= 0.25f
                    ? ThemeTokens.STATUS_ERROR
                    : (ratio <= 0.55f ? ThemeTokens.STATUS_WARNING : ThemeTokens.STATUS_SUCCESS);
            if (fillW > 0) {
                context.fill(barX, barY, barX + fillW, barY + 3, barColor);
            }
        }
    }
}
