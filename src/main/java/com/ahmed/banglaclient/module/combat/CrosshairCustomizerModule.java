package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.ColorSetting;
import com.ahmed.banglaclient.module.setting.EnumSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 21: Crosshair Customizer
 * Allows customization of crosshair shape (Cross, Dot, Circle-Box), size, thickness, center gap, outline, and entity highlight color.
 */
public final class CrosshairCustomizerModule extends HudModule {
    public enum CrosshairShape {
        CROSS("Plus Cross"),
        DOT_ONLY("Center Dot"),
        BOX_RING("Square Ring");

        private final String label;

        CrosshairShape(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final EnumSetting<CrosshairShape> shapeSetting;
    private final NumberSetting sizeSetting;
    private final NumberSetting gapSetting;
    private final NumberSetting thicknessSetting;
    private final BooleanSetting centerDotSetting;
    private final BooleanSetting darkOutlineSetting;
    private final BooleanSetting highlightTargetSetting;
    private final ColorSetting crosshairColorSetting;

    public CrosshairCustomizerModule() {
        super(
                "crosshair_customizer",
                "Crosshair Customizer",
                "Customizes crosshair shape, size, gap, thickness, outline, and color.",
                ModuleCategory.COMBAT,
                230.0f,
                200.0f,
                24.0f,
                24.0f,
                false
        );
        this.shapeSetting = addSetting(new EnumSetting<>(
                "shape", "Crosshair Shape", "Geometric style of the crosshair", CrosshairShape.CROSS
        ));
        this.sizeSetting = addSetting(new NumberSetting(
                "size", "Arm Length", "Length of each crosshair arm in pixels", 5.0, 1.0, 16.0, 1.0, "px"
        ));
        this.gapSetting = addSetting(new NumberSetting(
                "gap", "Center Gap", "Distance from the center point in pixels", 3.0, 0.0, 12.0, 1.0, "px"
        ));
        this.thicknessSetting = addSetting(new NumberSetting(
                "thickness", "Line Thickness", "Thickness of crosshair lines", 1.0, 1.0, 4.0, 1.0, "px"
        ));
        this.centerDotSetting = addSetting(new BooleanSetting(
                "center_dot", "Show Center Dot", "Draw a 1px/2px dot in the exact center", true
        ));
        this.darkOutlineSetting = addSetting(new BooleanSetting(
                "outline", "Dark Contrast Outline", "Draw a 1px dark border around the crosshair for visibility on snow/sky", true
        ));
        this.highlightTargetSetting = addSetting(new BooleanSetting(
                "target_color", "Highlight on Entity Target", "Turn red/emerald when looking directly at an entity", true
        ));
        this.crosshairColorSetting = addSetting(new ColorSetting(
                "color", "Crosshair Color", "Primary color of the custom crosshair", 0xFFF4F7FA, true
        ));
        addAliases("crosshair", "reticle", "aim");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        if (!preview && client.options != null && !client.options.getPerspective().isFirstPerson()) {
            return;
        }

        int cx = preview ? Math.round(getPosX() + 12) : client.getWindow().getScaledWidth() / 2;
        int cy = preview ? Math.round(getPosY() + 12) : client.getWindow().getScaledHeight() / 2;

        int color = crosshairColorSetting.getArgb();
        if (!preview && highlightTargetSetting.isEnabled() && client.targetedEntity != null) {
            color = ThemeTokens.STATUS_ERROR;
        }

        int len = sizeSetting.getIntValue();
        int gap = gapSetting.getIntValue();
        int thick = thicknessSetting.getIntValue();
        int halfThick = thick / 2;
        boolean outline = darkOutlineSetting.isEnabled();

        if (centerDotSetting.isEnabled() || shapeSetting.getValue() == CrosshairShape.DOT_ONLY) {
            drawRectWithOptionalOutline(context, cx - halfThick, cy - halfThick, thick, thick, color, outline);
        }

        if (shapeSetting.getValue() == CrosshairShape.CROSS) {
            // Left arm
            drawRectWithOptionalOutline(context, cx - gap - len, cy - halfThick, len, thick, color, outline);
            // Right arm
            drawRectWithOptionalOutline(context, cx + gap + 1, cy - halfThick, len, thick, color, outline);
            // Top arm
            drawRectWithOptionalOutline(context, cx - halfThick, cy - gap - len, thick, len, color, outline);
            // Bottom arm
            drawRectWithOptionalOutline(context, cx - halfThick, cy + gap + 1, thick, len, color, outline);
        } else if (shapeSetting.getValue() == CrosshairShape.BOX_RING) {
            int r = gap + len;
            com.ahmed.banglaclient.rendering.GlassRenderer.drawOutlineBorder(context, cx - r, cy - r, r * 2 + 1, r * 2 + 1, color);
        }
    }

    private static void drawRectWithOptionalOutline(DrawContext context, int x, int y, int w, int h, int color, boolean outline) {
        if (outline) {
            context.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xAA000000);
        }
        context.fill(x, y, x + w, y + h, color);
    }
}
