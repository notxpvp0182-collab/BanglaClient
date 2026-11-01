package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

/**
 * Module 05: Direction HUD
 * Displays the current facing cardinal/intercardinal direction, axis sign (+X, -Z, etc.), and compass yaw heading.
 */
public final class DirectionHudModule extends HudModule {
    private final BooleanSetting showYawDegreesSetting;
    private final BooleanSetting showAxisTowardsSetting;

    public DirectionHudModule() {
        super(
                "direction_hud",
                "Direction HUD",
                "Displays current facing cardinal direction, axis indicator, and compass heading.",
                ModuleCategory.HUD,
                10.0f,
                172.0f,
                96.0f,
                20.0f,
                false
        );
        this.showYawDegreesSetting = addSetting(new BooleanSetting(
                "show_degrees", "Show Yaw Degrees", "Display exact compass heading in degrees", true
        ));
        this.showAxisTowardsSetting = addSetting(new BooleanSetting(
                "show_axis", "Show Coordinate Axis", "Append (+Z), (-X), etc. axis direction", true
        ));
        addAliases("facing", "cardinal", "heading", "yaw");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        float rawYaw = client.player != null ? client.player.getYaw(tickDelta) : 180.0f;
        float wrappedYaw = MathHelper.wrapDegrees(rawYaw);
        // Convert Minecraft yaw (-180=North, -90=East, 0=South, 90=West) to standard 0..360 compass bearing
        float compassBearing = (wrappedYaw + 180.0f) % 360.0f;
        if (compassBearing < 0.0f) {
            compassBearing += 360.0f;
        }

        int octant = Math.round(compassBearing / 45.0f) % 8;
        String cardinal = switch (octant) {
            case 0 -> "N";
            case 1 -> "NE";
            case 2 -> "E";
            case 3 -> "SE";
            case 4 -> "S";
            case 5 -> "SW";
            case 6 -> "W";
            case 7 -> "NW";
            default -> "N";
        };

        String axis = switch (octant) {
            case 0 -> "-Z";
            case 1 -> "+X, -Z";
            case 2 -> "+X";
            case 3 -> "+X, +Z";
            case 4 -> "+Z";
            case 5 -> "-X, +Z";
            case 6 -> "-X";
            case 7 -> "-X, -Z";
            default -> "-Z";
        };

        StringBuilder sb = new StringBuilder("Facing: ").append(cardinal);
        if (showAxisTowardsSetting.isEnabled()) {
            sb.append(" [").append(axis).append("]");
        }
        if (showYawDegreesSetting.isEnabled()) {
            sb.append(String.format(" (%.0f°)", compassBearing));
        }

        String text = sb.toString();
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

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, textColorSetting.getArgb());
    }
}
