package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 70: Time Display
 * Converts the current Minecraft world time-of-day ticks (`0–23999`, where `0 = 06:00 AM`)
 * into a human-readable 12-hour or 24-hour world clock with Day/Night phase indicator.
 */
public final class TimeDisplayModule extends HudModule {
    private final BooleanSetting use24HourSetting;
    private final BooleanSetting showRawTicksSetting;

    public TimeDisplayModule() {
        super(
                "time_display",
                "Time Display",
                "Displays the current Minecraft in-game world time and day/night phase.",
                ModuleCategory.WORLD,
                160.0f,
                278.0f,
                118.0f,
                20.0f,
                false
        );
        this.use24HourSetting = addSetting(new BooleanSetting(
                "format_24h", "24-Hour World Clock", "Format in-game world time as 00:00 - 23:59 instead of AM/PM", false
        ));
        this.showRawTicksSetting = addSetting(new BooleanSetting(
                "show_ticks", "Show Raw World Ticks", "Append current tick within the 24000-tick cycle", false
        ));
        addAliases("worldtime", "ingametime", "daynight", "sun");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        long timeOfDay = 6000L;
        if (client.world != null) {
            timeOfDay = ((client.world.getTimeOfDay() % 24000L) + 24000L) % 24000L;
        }

        // Tick 0 in Minecraft corresponds to 06:00 AM
        int totalMinutes = (int) (((timeOfDay + 6000L) % 24000L) * 60L / 1000L);
        int hours24 = (totalMinutes / 60) % 24;
        int minutes = totalMinutes % 60;

        String phase = (timeOfDay >= 0L && timeOfDay < 12000L) ? "Day" : "Night";
        String formattedTime;
        if (use24HourSetting.isEnabled()) {
            formattedTime = String.format("%02d:%02d", hours24, minutes);
        } else {
            int hour12 = hours24 % 12;
            if (hour12 == 0) {
                hour12 = 12;
            }
            String amPm = hours24 < 12 ? "AM" : "PM";
            formattedTime = String.format("%02d:%02d %s", hour12, minutes, amPm);
        }

        StringBuilder sb = new StringBuilder("World: ").append(formattedTime).append(" (").append(phase).append(")");
        if (showRawTicksSetting.isEnabled()) {
            sb.append(" [").append(timeOfDay).append("t]");
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
