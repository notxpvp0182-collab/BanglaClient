package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

/**
 * Module 23: Target Information
 * Displays legitimate information (name, distance, and visible health bar) about the entity
 * the player is directly looking at via vanilla crosshair raycast.
 */
public final class TargetInfoModule extends HudModule {
    private final BooleanSetting showDistanceSetting;
    private final BooleanSetting showHealthBarSetting;

    public TargetInfoModule() {
        super(
                "target_info",
                "Target Information",
                "Displays name, distance, and health bar for the entity under the player's crosshair.",
                ModuleCategory.COMBAT,
                300.0f,
                230.0f,
                142.0f,
                42.0f,
                false
        );
        this.showDistanceSetting = addSetting(new BooleanSetting(
                "show_distance", "Show Distance", "Display distance in blocks to the targeted entity", true
        ));
        this.showHealthBarSetting = addSetting(new BooleanSetting(
                "show_health_bar", "Show Health Bar", "Render a smooth health progress bar for LivingEntities", true
        ));
        addAliases("targethud", "target", "entityinfo", "lookat");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        Entity targeted = client != null ? client.targetedEntity : null;
        if (targeted == null && !preview) {
            return;
        }

        String name = "Target Player";
        float health = 16.0f;
        float maxHealth = 20.0f;
        double distance = 3.15;

        if (targeted != null && client.player != null) {
            name = targeted.getDisplayName() != null ? targeted.getDisplayName().getString() : targeted.getName().getString();
            distance = client.player.distanceTo(targeted);
            if (targeted instanceof LivingEntity living) {
                health = living.getHealth() + living.getAbsorptionAmount();
                maxHealth = Math.max(1.0f, living.getMaxHealth());
            } else {
                health = -1.0f;
            }
        }

        int pad = paddingSetting.getIntValue();
        Theme theme = GlassRenderer.getTheme();

        String subLine = "";
        if (health >= 0.0f) {
            subLine = String.format("HP: %.1f / %.1f", health, maxHealth);
        }
        if (showDistanceSetting.isEnabled()) {
            subLine += (subLine.isEmpty() ? "" : " • ") + String.format("%.1fm", distance);
        }

        int contentW = Math.max(116, Math.max(client.textRenderer.getWidth(name), client.textRenderer.getWidth(subLine)));
        int barExtra = (showHealthBarSetting.isEnabled() && health >= 0.0f) ? 8 : 0;
        int boxW = contentW + pad * 2 + 4;
        int boxH = 22 + barExtra + pad * 2;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, true, showBorderSetting.isEnabled()
            );
        }

        context.drawTextWithShadow(client.textRenderer, name, x + pad + 2, y + pad + 1, textColorSetting.getArgb());
        context.drawTextWithShadow(client.textRenderer, subLine, x + pad + 2, y + pad + 12, theme.getTextSecondary());

        if (showHealthBarSetting.isEnabled() && health >= 0.0f) {
            int barX = x + pad + 2;
            int barY = y + boxH - pad - 4;
            int barW = boxW - (pad + 2) * 2;
            context.fill(barX, barY, barX + barW, barY + 3, 0x44FFFFFF);

            float ratio = Math.max(0.0f, Math.min(1.0f, health / maxHealth));
            int fillW = Math.round(barW * ratio);
            int barColor = ratio <= 0.25f
                    ? ThemeTokens.STATUS_ERROR
                    : (ratio <= 0.60f ? ThemeTokens.STATUS_WARNING : ThemeTokens.STATUS_SUCCESS);
            if (fillW > 0) {
                context.fill(barX, barY, barX + fillW, barY + 3, barColor);
            }
        }
    }
}
