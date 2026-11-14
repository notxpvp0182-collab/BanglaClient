package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.rendering.ClipRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

/**
 * Unified Compass HUD + Direction Axis & Yaw Degrees Module.
 */
public final class CompassHudModule extends HudModule {
    private static final String[] CARDINALS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    private static final String[] AXES = {"-Z", "+X,-Z", "+X", "+X,+Z", "+Z", "-X,+Z", "-X", "-X,-Z"};

    private final NumberSetting ribbonWidthSetting;
    private final BooleanSetting showAxisSetting;
    private final BooleanSetting showDegreesSetting;

    public CompassHudModule() {
        super("compass_hud", "Compass HUD", "Displays a horizontal cardinal compass ribbon with optional coordinate axis and yaw degrees.",
                ModuleCategory.WORLD, 240.0f, 8.0f, 180.0f, 24.0f, false);
        this.ribbonWidthSetting = addSetting(new NumberSetting("ribbon_width", "Compass Width", "Horizontal span in pixels", 180.0, 120.0, 320.0, 10.0, "px"));
        this.showAxisSetting = addSetting(new BooleanSetting("show_axis", "Show Coordinate Axis", "Display (+X, -Z) below the compass ribbon", true));
        this.showDegreesSetting = addSetting(new BooleanSetting("show_degrees", "Show Exact Yaw Degrees", "Display exact 0-360° bearing", true));
        addAliases("compass", "direction", "facing", "cardinal", "yaw");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        boolean hasFooter = showAxisSetting.isEnabled() || showDegreesSetting.isEnabled();
        int boxW = ribbonWidthSetting.getIntValue();
        int boxH = hasFooter ? 34 : 22;
        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());
        Theme theme = GlassRenderer.getTheme();

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(context, x, y, boxW, boxH, backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled());
        }

        float rawYaw = client.player != null ? client.player.getYaw(tickDelta) : 180.0f;
        float bearing = (MathHelper.wrapDegrees(rawYaw) + 180.0f) % 360.0f;
        if (bearing < 0.0f) bearing += 360.0f;

        final float finalBearing = bearing;
        int centerX = x + boxW / 2;

        ClipRenderer.renderClipped(context, x + 2, y + 1, boxW - 4, 20, () -> {
            float pixelsPerDegree = boxW / 120.0f;
            for (int deg = -180; deg < 540; deg += 15) {
                float deltaDeg = deg - finalBearing;
                int markX = Math.round(centerX + deltaDeg * pixelsPerDegree);
                if (markX < x - 16 || markX > x + boxW + 16) continue;

                int normalizedDeg = ((deg % 360) + 360) % 360;
                if (normalizedDeg % 45 == 0) {
                    String label = CARDINALS[(normalizedDeg / 45) % 8];
                    int lw = client.textRenderer.getWidth(label);
                    context.drawTextWithShadow(client.textRenderer, label, markX - lw / 2, y + 7, 0xFFFFFFFF);
                    context.fill(markX, y + 2, markX + 1, y + 5, 0xFFFFFFFF);
                } else {
                    context.fill(markX, y + 2, markX + 1, y + 4, theme.getTextMuted());
                }
            }
        });

        context.fill(centerX - 1, y + 1, centerX + 2, y + 4, 0xFFFFFFFF);

        if (hasFooter) {
            int octant = Math.round(bearing / 45.0f) % 8;
            StringBuilder sub = new StringBuilder();
            if (showAxisSetting.isEnabled()) sub.append("[").append(AXES[octant]).append("]");
            if (showDegreesSetting.isEnabled()) sub.append(sub.isEmpty() ? "" : " ").append(String.format("%.0f°", bearing));
            String footer = sub.toString();
            int fw = client.textRenderer.getWidth(footer);
            context.drawTextWithShadow(client.textRenderer, footer, centerX - fw / 2, y + 21, theme.getTextSecondary());
        }
    }
}
