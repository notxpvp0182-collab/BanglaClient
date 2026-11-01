package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.ColorSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 35: Dynamic Crosshair
 * Provides a reactive crosshair that smoothly expands or contracts based on ordinary game state
 * (sprinting, jumping, bow draw progress, and attack cooldown).
 */
public final class DynamicCrosshairModule extends HudModule {
    private final NumberSetting baseGapSetting;
    private final NumberSetting movementExpansionSetting;
    private final BooleanSetting reactToCooldownSetting;
    private final ColorSetting colorSetting;

    private float smoothedGap = 3.0f;

    public DynamicCrosshairModule() {
        super(
                "dynamic_crosshair",
                "Dynamic Crosshair",
                "Provides a reactive crosshair whose gap adjusts smoothly to movement and item use.",
                ModuleCategory.VISUAL,
                260.0f,
                200.0f,
                24.0f,
                24.0f,
                false
        );
        this.baseGapSetting = addSetting(new NumberSetting(
                "base_gap", "Resting Gap", "Center gap when standing still",
                2.0, 1.0, 8.0, 1.0, "px"
        ));
        this.movementExpansionSetting = addSetting(new NumberSetting(
                "move_expand", "Movement Expansion", "Additional gap expansion while sprinting or airborne",
                4.0, 1.0, 10.0, 1.0, "px"
        ));
        this.reactToCooldownSetting = addSetting(new BooleanSetting(
                "cooldown_react", "React to Attack Cooldown", "Expand gap slightly while weapon is recharging", true
        ));
        this.colorSetting = addSetting(new ColorSetting(
                "color", "Crosshair Color", "Color of the dynamic crosshair arms", 0xFF38BDF8, true
        ));
        addAliases("reactivecrosshair", "spreadcrosshair");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        if (!preview && client.options != null && !client.options.getPerspective().isFirstPerson()) {
            return;
        }

        float targetGap = baseGapSetting.getFloatValue();
        float expand = movementExpansionSetting.getFloatValue();

        if (client.player != null) {
            if (client.player.isSprinting()) {
                targetGap += expand * 0.65f;
            }
            if (!client.player.isOnGround()) {
                targetGap += expand * 0.55f;
            }
            if (client.player.isSneaking()) {
                targetGap = Math.max(1.0f, targetGap - 1.0f);
            }
            if (reactToCooldownSetting.isEnabled()) {
                float cd = 1.0f - client.player.getAttackCooldownProgress(tickDelta);
                targetGap += cd * 3.0f;
            }
        }

        smoothedGap = MathUtils.lerp(smoothedGap, targetGap, 0.25f);
        int gap = Math.round(smoothedGap);
        int len = 4;
        int color = colorSetting.getArgb();

        int cx = preview ? Math.round(getPosX() + 12) : client.getWindow().getScaledWidth() / 2;
        int cy = preview ? Math.round(getPosY() + 12) : client.getWindow().getScaledHeight() / 2;

        // 4 reactive arms with 1px dark contrast backing
        context.fill(cx - gap - len - 1, cy - 1, cx - gap + 1, cy + 2, 0x99000000);
        context.fill(cx - gap - len, cy, cx - gap, cy + 1, color);

        context.fill(cx + gap, cy - 1, cx + gap + len + 2, cy + 2, 0x99000000);
        context.fill(cx + gap + 1, cy, cx + gap + len + 1, cy + 1, color);

        context.fill(cx - 1, cy - gap - len - 1, cx + 2, cy - gap + 1, 0x99000000);
        context.fill(cx, cy - gap - len, cx + 1, cy - gap, color);

        context.fill(cx - 1, cy + gap, cx + 2, cy + gap + len + 2, 0x99000000);
        context.fill(cx, cy + gap + 1, cx + 1, cy + gap + len + 1, color);
    }
}
