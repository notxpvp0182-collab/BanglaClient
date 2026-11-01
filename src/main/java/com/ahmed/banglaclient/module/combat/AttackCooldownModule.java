package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 24: Attack Cooldown
 * Displays the vanilla weapon attack recharge progress bar and percentage.
 */
public final class AttackCooldownModule extends HudModule {
    private final BooleanSetting hideWhenFullSetting;
    private final BooleanSetting showPercentageSetting;

    public AttackCooldownModule() {
        super(
                "attack_cooldown",
                "Attack Cooldown",
                "Displays the vanilla weapon attack recharge indicator and percentage.",
                ModuleCategory.COMBAT,
                300.0f,
                278.0f,
                96.0f,
                26.0f,
                false
        );
        this.hideWhenFullSetting = addSetting(new BooleanSetting(
                "hide_when_ready", "Hide When 100% Ready", "Only display the recharge widget while weapon is on cooldown", false
        ));
        this.showPercentageSetting = addSetting(new BooleanSetting(
                "show_percent", "Show Percentage Text", "Display numeric recharge percentage above the bar", true
        ));
        addAliases("cooldown", "recharge", "weaponspeed", "swing");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        float progress = 1.0f;
        if (client.player != null) {
            progress = client.player.getAttackCooldownProgress(tickDelta);
        } else if (preview) {
            progress = 0.72f;
        }

        if (progress >= 0.999f && hideWhenFullSetting.isEnabled() && !preview) {
            return;
        }

        int pct = Math.round(progress * 100.0f);
        String label = pct >= 100 ? "Attack: READY" : ("Cooldown: " + pct + "%");

        int pad = paddingSetting.getIntValue();
        int textH = showPercentageSetting.isEnabled() ? (client.textRenderer.fontHeight + 3) : 0;
        int boxW = Math.max(88, client.textRenderer.getWidth(label) + pad * 2);
        int boxH = textH + 5 + pad * 2;

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

        if (showPercentageSetting.isEnabled()) {
            int color = pct >= 100 ? ThemeTokens.STATUS_SUCCESS : textColorSetting.getArgb();
            context.drawTextWithShadow(client.textRenderer, label, x + pad, y + pad, color);
        }

        int barX = x + pad;
        int barY = y + pad + textH;
        int barW = boxW - pad * 2;
        context.fill(barX, barY, barX + barW, barY + 4, 0x44FFFFFF);

        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : ThemeTokens.DEFAULT_ACCENT_PRIMARY;
        int fillW = Math.round(barW * Math.min(1.0f, progress));
        if (fillW > 0) {
            context.fill(barX, barY, barX + fillW, barY + 4, pct >= 100 ? ThemeTokens.STATUS_SUCCESS : accent);
        }
    }
}
