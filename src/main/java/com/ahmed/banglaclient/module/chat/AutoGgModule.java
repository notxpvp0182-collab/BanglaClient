package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.module.setting.StringSetting;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;

import java.util.Locale;

/**
 * Module 89: Auto GG
 * Optional configurable end-of-match courtesy message with strict anti-spam cooldown (minimum 10 seconds)
 * and user-configurable trigger phrases. Disabled by default so it only runs with explicit user consent.
 */
public final class AutoGgModule extends Module {
    private static final long MIN_COOLDOWN_MILLIS = 10_000L;

    private final StringSetting courtesyMessageSetting;
    private final StringSetting triggerPhrasesSetting;
    private final NumberSetting delayTicksSetting;

    private long lastSentTimestamp = 0L;
    private int pendingSendTicks = -1;

    private static boolean hookRegistered = false;
    private static AutoGgModule instance;

    public AutoGgModule() {
        super(
                "auto_gg",
                "Auto GG",
                "Sends a courtesy 'gg' message at the end of a match with strict anti-spam cooldown.",
                ModuleCategory.CHAT
        );
        this.courtesyMessageSetting = addSetting(new StringSetting(
                "message", "Courtesy Text", "Message sent when a match end summary is detected", "gg", 16
        ));
        this.triggerPhrasesSetting = addSetting(new StringSetting(
                "triggers", "Match End Triggers", "Comma-separated server summary phrases",
                "1st killer, winning team, victory!, match summary", 120
        ));
        this.delayTicksSetting = addSetting(new NumberSetting(
                "delay_ticks", "Send Delay (Ticks)", "Wait ticks before sending (20 ticks = 1 second)",
                25.0, 10.0, 80.0, 5.0, "t"
        ));
        instance = this;
        registerHookOnce();
        addAliases("autogg", "goodgame", "gg");
    }

    private static synchronized void registerHookOnce() {
        if (hookRegistered) {
            return;
        }
        hookRegistered = true;
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay && instance != null && instance.isEnabled()) {
                instance.checkMatchSummary(message);
            }
        });
    }

    private void checkMatchSummary(Text message) {
        if (message == null || pendingSendTicks >= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastSentTimestamp < MIN_COOLDOWN_MILLIS) {
            return;
        }
        String raw = message.getString();
        if (raw == null || raw.isBlank()) {
            return;
        }
        String lower = raw.toLowerCase(Locale.ROOT);
        String[] triggers = triggerPhrasesSetting.getValue().split(",");
        for (String trigger : triggers) {
            String t = trigger.trim().toLowerCase(Locale.ROOT);
            if (t.length() >= 4 && lower.contains(t)) {
                pendingSendTicks = delayTicksSetting.getIntValue();
                break;
            }
        }
    }

    @Override
    public void onTick() {
        if (pendingSendTicks < 0) {
            return;
        }
        if (pendingSendTicks > 0) {
            pendingSendTicks--;
            return;
        }
        pendingSendTicks = -1;
        if (client.player != null && client.getNetworkHandler() != null) {
            String msg = courtesyMessageSetting.getValue().trim();
            if (!msg.isEmpty() && !msg.startsWith("/")) {
                client.getNetworkHandler().sendChatMessage(msg);
                lastSentTimestamp = System.currentTimeMillis();
            }
        }
    }
}
