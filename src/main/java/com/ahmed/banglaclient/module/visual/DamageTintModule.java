package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.ColorSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 33: Damage Tint
 * Renders a customizable screen-edge gradient vignette pulse when the player takes damage (`hurtTime > 0`).
 */
public final class DamageTintModule extends HudModule {
    private final ColorSetting tintColorSetting;
    private final NumberSetting thicknessSetting;
    private final NumberSetting maxOpacitySetting;

    public DamageTintModule() {
        super(
                "damage_tint",
                "Damage Tint",
                "Customizes the screen-edge damage overlay pulse when taking damage.",
                ModuleCategory.VISUAL,
                0.0f,
                0.0f,
                40.0f,
                20.0f,
                false
        );
        this.tintColorSetting = addSetting(new ColorSetting(
                "tint_color", "Overlay Color", "Color of the damage screen-edge flash", 0xFFF87171, false
        ));
        this.thicknessSetting = addSetting(new NumberSetting(
                "border_span", "Vignette Span", "Width of the screen-edge tint in pixels",
                28.0, 10.0, 80.0, 2.0, "px"
        ));
        this.maxOpacitySetting = addSetting(new NumberSetting(
                "max_opacity", "Peak Opacity", "Maximum opacity at the start of the hurt tick",
                0.45, 0.10, 0.85, 0.05, ""
        ));
        addAliases("damagetint", "hitcolor", "hurtoverlay");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        if (preview) {
            setWidth(64);
            setHeight(18);
            context.drawTextWithShadow(client.textRenderer, "Damage Tint", Math.round(getPosX()), Math.round(getPosY()), tintColorSetting.getArgb());
            return;
        }
        if (client.player == null || client.player.hurtTime <= 0) {
            return;
        }

        float hurtProgress = (client.player.hurtTime - tickDelta) / Math.max(1.0f, (float) client.player.maxHurtTime);
        hurtProgress = Math.max(0.0f, Math.min(1.0f, hurtProgress));

        float alpha = hurtProgress * maxOpacitySetting.getFloatValue();
        int edgeColor = ColorUtils.withAlpha(tintColorSetting.getArgb(), alpha);
        int clearColor = ColorUtils.withAlpha(tintColorSetting.getArgb(), 0.0f);

        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        int span = thicknessSetting.getIntValue();

        // Top & Bottom gradient bands
        context.fillGradient(0, 0, sw, span, edgeColor, clearColor);
        context.fillGradient(0, sh - span, sw, sh, clearColor, edgeColor);

        // Left & Right thin edge highlights
        context.fill(0, 0, Math.max(4, span / 3), sh, ColorUtils.multiplyAlpha(edgeColor, 0.6f));
        context.fill(sw - Math.max(4, span / 3), 0, sw, sh, ColorUtils.multiplyAlpha(edgeColor, 0.6f));
    }
}
