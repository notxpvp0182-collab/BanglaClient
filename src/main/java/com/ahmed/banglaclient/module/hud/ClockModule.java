package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Module 07: Clock
 * Displays the local real-world system time with customizable 12-hour or 24-hour format and seconds toggle.
 */
public final class ClockModule extends HudModule {
    private static final DateTimeFormatter FMT_12_SEC = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.US);
    private static final DateTimeFormatter FMT_12_NO_SEC = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);
    private static final DateTimeFormatter FMT_24_SEC = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US);
    private static final DateTimeFormatter FMT_24_NO_SEC = DateTimeFormatter.ofPattern("HH:mm", Locale.US);

    private final BooleanSetting use24HourSetting;
    private final BooleanSetting showSecondsSetting;

    public ClockModule() {
        super(
                "clock",
                "Clock",
                "Displays the local real-world system clock in 12-hour or 24-hour format.",
                ModuleCategory.HUD,
                80.0f,
                34.0f,
                74.0f,
                20.0f,
                false
        );
        this.use24HourSetting = addSetting(new BooleanSetting(
                "format_24h", "24-Hour Format", "Use 24-hour military time instead of 12-hour AM/PM", false
        ));
        this.showSecondsSetting = addSetting(new BooleanSetting(
                "show_seconds", "Show Seconds", "Include seconds in the clock display", false
        ));
        addAliases("time", "realtime", "watch");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter;
        if (use24HourSetting.isEnabled()) {
            formatter = showSecondsSetting.isEnabled() ? FMT_24_SEC : FMT_24_NO_SEC;
        } else {
            formatter = showSecondsSetting.isEnabled() ? FMT_12_SEC : FMT_12_NO_SEC;
        }

        String text = now.format(formatter);
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
