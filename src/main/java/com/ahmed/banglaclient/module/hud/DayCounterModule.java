package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 11: Day Counter
 * Displays the current Minecraft world day derived from world time-of-day ticks.
 */
public final class DayCounterModule extends HudModule {
    private final BooleanSetting startAtDayOneSetting;

    public DayCounterModule() {
        super(
                "day_counter",
                "Day Counter",
                "Displays the current in-game world day count.",
                ModuleCategory.HUD,
                160.0f,
                10.0f,
                68.0f,
                20.0f,
                false
        );
        this.startAtDayOneSetting = addSetting(new BooleanSetting(
                "one_based", "Start at Day 1", "Count the first world day as Day 1 instead of Day 0", true
        ));
        addAliases("day", "days", "worldday");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        long day = 1L;
        if (client.world != null) {
            day = (client.world.getTimeOfDay() / 24000L) + (startAtDayOneSetting.isEnabled() ? 1L : 0L);
        } else if (preview) {
            day = 100L;
        }

        String text = "Day " + day;
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
