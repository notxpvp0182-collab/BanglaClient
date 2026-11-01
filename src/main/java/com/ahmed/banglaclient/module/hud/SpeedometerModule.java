package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.EnumSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 08: Speedometer
 * Displays actual player movement speed with configurable units (m/s, km/h, mph) and optional vertical velocity.
 */
public final class SpeedometerModule extends HudModule {
    public enum SpeedUnit {
        BLOCKS_PER_SEC("m/s", 1.0),
        KILOMETERS_PER_HOUR("km/h", 3.6),
        MILES_PER_HOUR("mph", 2.23694);

        private final String suffix;
        private final double multiplier;

        SpeedUnit(String suffix, double multiplier) {
            this.suffix = suffix;
            this.multiplier = multiplier;
        }

        public String getSuffix() {
            return suffix;
        }

        public double getMultiplier() {
            return multiplier;
        }

        @Override
        public String toString() {
            return suffix;
        }
    }

    private final EnumSetting<SpeedUnit> unitSetting;
    private final BooleanSetting includeVerticalSpeedSetting;

    public SpeedometerModule() {
        super(
                "speedometer",
                "Speedometer",
                "Displays actual player movement speed in blocks/sec (m/s), km/h, or mph.",
                ModuleCategory.HUD,
                10.0f,
                196.0f,
                84.0f,
                20.0f,
                false
        );
        this.unitSetting = addSetting(new EnumSetting<>(
                "unit", "Speed Unit", "Measurement unit for player velocity", SpeedUnit.BLOCKS_PER_SEC
        ));
        this.includeVerticalSpeedSetting = addSetting(new BooleanSetting(
                "include_vertical", "Include Vertical Y Velocity", "Include jumping/falling velocity in speed calculation", false
        ));
        addAliases("speed", "velocity", "bps", "kmh", "mph");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        double blocksPerSec = 0.0;
        if (client.player != null) {
            double dx = client.player.getX() - client.player.prevX;
            double dy = includeVerticalSpeedSetting.isEnabled() ? (client.player.getY() - client.player.prevY) : 0.0;
            double dz = client.player.getZ() - client.player.prevZ;
            blocksPerSec = Math.sqrt(dx * dx + dy * dy + dz * dz) * 20.0;
        } else if (preview) {
            blocksPerSec = 5.61;
        }

        SpeedUnit unit = unitSetting.getValue();
        double converted = blocksPerSec * unit.getMultiplier();
        String text = String.format("Speed: %.2f %s", converted, unit.getSuffix());

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
