package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 71: Moon Phase
 * Displays the current lunar phase name (`Full Moon`, `Waning Gibbous`, etc.) and optional moon size percentage.
 */
public final class MoonPhaseModule extends HudModule {
    private final BooleanSetting showMoonSizeSetting;

    public MoonPhaseModule() {
        super(
                "moon_phase",
                "Moon Phase",
                "Displays the current lunar phase and illumination percentage.",
                ModuleCategory.WORLD,
                160.0f,
                302.0f,
                124.0f,
                20.0f,
                false
        );
        this.showMoonSizeSetting = addSetting(new BooleanSetting(
                "show_size", "Show Illumination %", "Append moon illumination percentage (affects swamp slime spawning)", true
        ));
        addAliases("moon", "lunar", "fullmoon");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int phaseIndex = 0;
        float moonSize = 1.0f;

        if (client.world != null) {
            phaseIndex = client.world.getMoonPhase();
            moonSize = client.world.getMoonSize();
        }

        String phaseName = switch (phaseIndex) {
            case 0 -> "Full Moon";
            case 1 -> "Waning Gibbous";
            case 2 -> "Third Quarter";
            case 3 -> "Waning Crescent";
            case 4 -> "New Moon";
            case 5 -> "Waxing Crescent";
            case 6 -> "First Quarter";
            case 7 -> "Waxing Gibbous";
            default -> "Unknown";
        };

        String text = "Moon: " + phaseName;
        if (showMoonSizeSetting.isEnabled()) {
            text += String.format(" (%.0f%%)", moonSize * 100.0f);
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

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, textColorSetting.getArgb());
    }
}
