package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.StringSetting;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;

import java.util.Locale;

/**
 * Module 86: Chat Filter
 * Filters incoming chat messages locally according to user-defined blocked keywords and anti-duplicate rules.
 */
public final class ChatFilterModule extends Module {
    private final StringSetting blockedWordsSetting;
    private final BooleanSetting blockDuplicateSpamSetting;

    private String lastReceivedMessage = "";
    private int filteredCount = 0;

    private static boolean filterHookRegistered = false;
    private static ChatFilterModule instance;

    public ChatFilterModule() {
        super(
                "chat_filter",
                "Chat Filter",
                "Filters unwanted chat messages locally based on user-defined rules.",
                ModuleCategory.CHAT
        );
        this.blockedWordsSetting = addSetting(new StringSetting(
                "blocked_words", "Muted Keywords (Comma-Separated)", "Hide incoming messages containing any of these phrases",
                "free coins, visit my auction, spam", 128
        ));
        this.blockDuplicateSpamSetting = addSetting(new BooleanSetting(
                "anti_duplicate", "Hide Consecutive Duplicates", "Suppress identical consecutive chat lines", true
        ));
        instance = this;
        registerFilterHookOnce();
        addAliases("chatmute", "antispam", "regexfilter", "filter");
    }

    private static synchronized void registerFilterHookOnce() {
        if (filterHookRegistered) {
            return;
        }
        filterHookRegistered = true;
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (instance != null && instance.isEnabled()) {
                return instance.shouldAllowMessage(message);
            }
            return true;
        });
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (!overlay && instance != null && instance.isEnabled()) {
                return instance.shouldAllowMessage(message);
            }
            return true;
        });
    }

    public boolean shouldAllowMessage(Text message) {
        if (!isEnabled() || message == null) {
            return true;
        }
        String raw = message.getString();
        if (raw == null || raw.isBlank()) {
            return true;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);

        if (blockDuplicateSpamSetting.isEnabled() && normalized.equals(lastReceivedMessage)) {
            filteredCount++;
            return false;
        }
        lastReceivedMessage = normalized;

        String[] terms = blockedWordsSetting.getValue().split(",");
        for (String term : terms) {
            String clean = term.trim().toLowerCase(Locale.ROOT);
            if (clean.length() >= 2 && normalized.contains(clean)) {
                filteredCount++;
                return false;
            }
        }
        return true;
    }

    public int getFilteredCount() {
        return filteredCount;
    }
}
