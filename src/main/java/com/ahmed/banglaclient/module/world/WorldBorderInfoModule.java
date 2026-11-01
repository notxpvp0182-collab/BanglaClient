package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.world.border.WorldBorder;

/**
 * Module 81: World Border Info
 * Displays available world border diameter and the player's distance to the nearest border edge.
 */
public final class WorldBorderInfoModule extends HudModule {
    private final BooleanSetting showDiameterSetting;

    public WorldBorderInfoModule() {
        super(
                "world_border_info",
                "World Border Info",
                "Displays world border size and player distance to the nearest border wall.",
                ModuleCategory.WORLD,
                590.0f,
                396.0f,
                136.0f,
                20.0f,
                false
        );
        this.showDiameterSetting = addSetting(new BooleanSetting(
                "show_size", "Show Border Diameter", "Include the total diameter of the world border", true
        ));
        addAliases("worldborder", "border", "shrinkingborder");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        double distInside = 500.0;
        double size = 1000.0;

        if (client.world != null) {
            WorldBorder border = client.world.getWorldBorder();
            size = border.getSize();
            if (client.player != null) {
                distInside = border.getDistanceInsideBorder(client.player);
            }
        }

        String distStr = distInside > 99999.0 ? "Far" : String.format("%.0fm", distInside);
        String sizeStr = size > 999999.0 ? "60M" : String.format("%.0fm", size);

        String text = showDiameterSetting.isEnabled()
                ? String.format("Border: %s (Size: %s)", distStr, sizeStr)
                : ("Border Dist: " + distStr);

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

        int color = distInside <= 15.0
                ? ThemeTokens.STATUS_ERROR
                : (distInside <= 50.0 ? ThemeTokens.STATUS_WARNING : textColorSetting.getArgb());
        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, color);
    }
}
