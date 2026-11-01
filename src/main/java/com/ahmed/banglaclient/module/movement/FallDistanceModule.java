package com.ahmed.banglaclient.module.movement;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 66: Fall Distance
 * Displays the player's current fall distance in blocks and estimated unmitigated fall damage hearts.
 */
public final class FallDistanceModule extends HudModule {
    private final BooleanSetting hideWhenZeroSetting;
    private final BooleanSetting showEstimatedDamageSetting;

    public FallDistanceModule() {
        super(
                "fall_distance",
                "Fall Distance",
                "Displays current fall distance and estimated vanilla fall damage.",
                ModuleCategory.MOVEMENT,
                160.0f,
                182.0f,
                104.0f,
                20.0f,
                false
        );
        this.hideWhenZeroSetting = addSetting(new BooleanSetting(
                "hide_when_grounded", "Hide When On Ground", "Only show widget while falling more than 1.5 blocks", false
        ));
        this.showEstimatedDamageSetting = addSetting(new BooleanSetting(
                "show_damage", "Show Estimated Damage", "Append raw unmitigated fall damage when fall distance exceeds 3 blocks", true
        ));
        addAliases("fall", "falldamage", "drop");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        float fallDist = client.player != null ? (float) client.player.fallDistance : 0.0f;
        if (preview && fallDist < 0.1f) {
            fallDist = 7.4f;
        }
        if (!preview && hideWhenZeroSetting.isEnabled() && fallDist < 1.5f) {
            return;
        }

        String text = String.format("Fall: %.1fm", fallDist);
        int color = textColorSetting.getArgb();

        if (fallDist > 3.0f) {
            int rawDamage = (int) Math.ceil(fallDist - 3.0f);
            if (showEstimatedDamageSetting.isEnabled()) {
                text += String.format(" (-%.1f ♥)", rawDamage / 2.0f);
            }
            color = rawDamage >= 10 ? ThemeTokens.STATUS_ERROR : ThemeTokens.STATUS_WARNING;
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

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, color);
    }
}
