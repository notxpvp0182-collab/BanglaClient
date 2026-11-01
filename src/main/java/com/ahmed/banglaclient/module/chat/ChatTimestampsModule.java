package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.ColorSetting;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Module 82: Chat Timestamps
 * Prepends configurable local timestamps (12-hour or 24-hour, with or without seconds) to incoming chat messages.
 */
public final class ChatTimestampsModule extends Module {
    private static final DateTimeFormatter FMT_24_SEC = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US);
    private static final DateTimeFormatter FMT_24_NO_SEC = DateTimeFormatter.ofPattern("HH:mm", Locale.US);
    private static final DateTimeFormatter FMT_12_SEC = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.US);
    private static final DateTimeFormatter FMT_12_NO_SEC = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);

    private final BooleanSetting use24HourSetting;
    private final BooleanSetting showSecondsSetting;
    private final ColorSetting bracketColorSetting;

    public ChatTimestampsModule() {
        super(
                "chat_timestamps",
                "Chat Timestamps",
                "Adds customizable local timestamps to incoming chat messages.",
                ModuleCategory.CHAT
        );
        this.use24HourSetting = addSetting(new BooleanSetting(
                "format_24h", "24-Hour Format", "Use 24-hour time instead of 12-hour AM/PM", true
        ));
        this.showSecondsSetting = addSetting(new BooleanSetting(
                "show_seconds", "Show Seconds", "Include seconds in chat timestamps", true
        ));
        this.bracketColorSetting = addSetting(new ColorSetting(
                "timestamp_color", "Timestamp Color", "RGB color applied to the prepended timestamp prefix",
                0xFF94A3B8, false
        ));
        addAliases("timestamps", "chattime", "messagetime");
    }

    /**
     * Prepends a styled timestamp prefix to the given chat message.
     */
    public Text formatMessageWithTimestamp(Text original) {
        if (!isEnabled() || original == null) {
            return original;
        }
        DateTimeFormatter fmt;
        if (use24HourSetting.isEnabled()) {
            fmt = showSecondsSetting.isEnabled() ? FMT_24_SEC : FMT_24_NO_SEC;
        } else {
            fmt = showSecondsSetting.isEnabled() ? FMT_12_SEC : FMT_12_NO_SEC;
        }

        String timeStr = "[" + LocalTime.now().format(fmt) + "] ";
        int rgb = bracketColorSetting.getArgb() & 0x00FFFFFF;
        MutableText prefix = Text.literal(timeStr).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
        return prefix.append(original);
    }
}
