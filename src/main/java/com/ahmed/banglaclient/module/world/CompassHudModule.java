package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.rendering.ClipRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

/**
 * Module 80: Compass HUD
 * Renders a sleek horizontal cardinal/intercardinal compass ribbon (`N`, `NE`, `E`, `SE`, `S`, `SW`, `W`, `NW`)
 * with degree tick marks and a center bearing pointer.
 */
public final class CompassHudModule extends HudModule {
    private static final String[] CARDINALS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

    private final NumberSetting ribbonWidthSetting;

    public CompassHudModule() {
        super(
                "compass_hud",
                "Compass HUD",
                "Displays a horizontal cardinal compass ribbon with degree markers.",
                ModuleCategory.WORLD,
                240.0f,
                8.0f,
                180.0f,
                24.0f,
                false
        );
        this.ribbonWidthSetting = addSetting(new NumberSetting(
                "ribbon_width", "Compass Width", "Horizontal span of the compass ribbon in pixels",
                180.0, 120.0, 320.0, 10.0, "px"
        ));
        addAliases("compass", "cardinalbar", "bearing", "topcompass");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int boxW = ribbonWidthSetting.getIntValue();
        int boxH = 24;
        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());
        Theme theme = GlassRenderer.getTheme();

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        float rawYaw = client.player != null ? client.player.getYaw(tickDelta) : 180.0f;
        float bearing = (MathHelper.wrapDegrees(rawYaw) + 180.0f) % 360.0f;
        if (bearing < 0.0f) {
            bearing += 360.0f;
        }

        int centerX = x + boxW / 2;
        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : ThemeTokens.DEFAULT_ACCENT_PRIMARY;

        ClipRenderer.renderClipped(context, x + 2, y + 1, boxW - 4, boxH - 2, () -> {
            float pixelsPerDegree = boxW / 120.0f; // 120-degree field of view on the ribbon
            for (int deg = -180; deg < 540; deg += 15) {
                float deltaDeg = deg - bearing;
                int markX = Math.round(centerX + deltaDeg * pixelsPerDegree);
                if (markX < x - 16 || markX > x + boxW + 16) {
                    continue;
                }

                int normalizedDeg = ((deg % 360) + 360) % 360;
                if (normalizedDeg % 45 == 0) {
                    String label = CARDINALS[(normalizedDeg / 45) % 8];
                    int lw = client.textRenderer.getWidth(label);
                    int color = (normalizedDeg == 0) ? accent : textColorSetting.getArgb();
                    context.drawTextWithShadow(client.textRenderer, label, markX - lw / 2, y + 8, color);
                    context.fill(markX, y + 2, markX + 1, y + 6, color);
                } else {
                    context.fill(markX, y + 2, markX + 1, y + 5, theme.getTextMuted());
                }
            }
        });

        // Center notch pointer
        context.fill(centerX - 1, y + 1, centerX + 2, y + 4, accent);
    }
}
