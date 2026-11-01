package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.EnumSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.world.World;

/**
 * Module 04: Coordinates
 * Displays the player's X, Y, and Z coordinates with block or decimal precision,
 * single-line or multi-line layout, and optional Nether/Overworld corresponding coordinates.
 */
public final class CoordinatesModule extends HudModule {
    public enum LayoutMode {
        COMPACT_SINGLE_LINE("Single Line"),
        DETAILED_MULTI_LINE("Multi-Line");

        private final String title;

        LayoutMode(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private final EnumSetting<LayoutMode> layoutSetting;
    private final BooleanSetting decimalPrecisionSetting;
    private final BooleanSetting showNetherCoordsSetting;

    public CoordinatesModule() {
        super(
                "coordinates",
                "Coordinates",
                "Displays X, Y, and Z player coordinates in block or decimal format.",
                ModuleCategory.HUD,
                10.0f,
                148.0f,
                126.0f,
                36.0f,
                true
        );
        this.layoutSetting = addSetting(new EnumSetting<>(
                "layout", "Layout Style", "Single-line compact or multi-line stacked layout", LayoutMode.COMPACT_SINGLE_LINE
        ));
        this.decimalPrecisionSetting = addSetting(new BooleanSetting(
                "decimals", "Decimal Format", "Show 1 decimal place instead of integer block positions", false
        ));
        this.showNetherCoordsSetting = addSetting(new BooleanSetting(
                "nether_coords", "Dimension Equivalent", "Show corresponding Nether/Overworld X and Z coordinates", false
        ));
        addAliases("xyz", "pos", "position", "location");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        double xPos = client.player != null ? client.player.getX() : 128.4;
        double yPos = client.player != null ? client.player.getY() : 64.0;
        double zPos = client.player != null ? client.player.getZ() : -256.8;

        boolean isNether = client.world != null && client.world.getRegistryKey() == World.NETHER;
        double altX = isNether ? xPos * 8.0 : xPos / 8.0;
        double altZ = isNether ? zPos * 8.0 : zPos / 8.0;

        int pad = paddingSetting.getIntValue();
        int x = Math.round(getPosX());
        int y = Math.round(getPosY());
        Theme theme = GlassRenderer.getTheme();

        if (layoutSetting.getValue() == LayoutMode.COMPACT_SINGLE_LINE) {
            String mainLine = decimalPrecisionSetting.isEnabled()
                    ? String.format("XYZ: %.1f, %.1f, %.1f", xPos, yPos, zPos)
                    : String.format("XYZ: %d, %d, %d", (int) Math.floor(xPos), (int) Math.floor(yPos), (int) Math.floor(zPos));

            String subLine = showNetherCoordsSetting.isEnabled()
                    ? String.format(" (%s: %d, %d)", isNether ? "OW" : "Nether", (int) Math.floor(altX), (int) Math.floor(altZ))
                    : "";

            String full = mainLine + subLine;
            int boxW = client.textRenderer.getWidth(full) + pad * 2;
            int boxH = client.textRenderer.fontHeight + pad * 2;
            setWidth(boxW);
            setHeight(boxH);

            if (showBackgroundSetting.isEnabled()) {
                GlassRenderer.drawCustomGlassPanel(
                        context, x, y, boxW, boxH,
                        backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
                );
            }
            context.drawTextWithShadow(client.textRenderer, full, x + pad, y + pad + 1, textColorSetting.getArgb());
        } else {
            String lineX = decimalPrecisionSetting.isEnabled() ? String.format("X: %.1f", xPos) : String.format("X: %d", (int) Math.floor(xPos));
            String lineY = decimalPrecisionSetting.isEnabled() ? String.format("Y: %.1f", yPos) : String.format("Y: %d", (int) Math.floor(yPos));
            String lineZ = decimalPrecisionSetting.isEnabled() ? String.format("Z: %.1f", zPos) : String.format("Z: %d", (int) Math.floor(zPos));

            if (showNetherCoordsSetting.isEnabled()) {
                lineX += String.format(" (%d)", (int) Math.floor(altX));
                lineZ += String.format(" (%d)", (int) Math.floor(altZ));
            }

            int maxW = Math.max(client.textRenderer.getWidth(lineX),
                    Math.max(client.textRenderer.getWidth(lineY), client.textRenderer.getWidth(lineZ)));
            int boxW = maxW + pad * 2 + 4;
            int boxH = (client.textRenderer.fontHeight + 2) * 3 + pad * 2 - 2;
            setWidth(boxW);
            setHeight(boxH);

            if (showBackgroundSetting.isEnabled()) {
                GlassRenderer.drawCustomGlassPanel(
                        context, x, y, boxW, boxH,
                        backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
                );
            }
            int textY = y + pad + 1;
            context.drawTextWithShadow(client.textRenderer, lineX, x + pad, textY, textColorSetting.getArgb());
            context.drawTextWithShadow(client.textRenderer, lineY, x + pad, textY + 11, theme.getTextSecondary());
            context.drawTextWithShadow(client.textRenderer, lineZ, x + pad, textY + 22, textColorSetting.getArgb());
        }
    }
}
