package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.StringSetting;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.Locale;

/**
 * Module 85: Chat Notifications
 * Alerts the player with a Liquid Glass toast notification and optional chime sound when their
 * username or custom watch keywords are mentioned in chat.
 */
public final class ChatNotificationsModule extends Module {
    private final BooleanSetting notifyOnUsernameSetting;
    private final StringSetting customKeywordsSetting;
    private final BooleanSetting playPingSoundSetting;

    private static boolean listenerRegistered = false;
    private static ChatNotificationsModule instance;

    public ChatNotificationsModule() {
        super(
                "chat_notifications",
                "Chat Notifications",
                "Alerts the player when their username or custom keywords are mentioned in chat.",
                ModuleCategory.CHAT
        );
        this.notifyOnUsernameSetting = addSetting(new BooleanSetting(
                "mention_username", "Notify on Username Mention", "Trigger alert when your Minecraft username appears in chat", true
        ));
        this.customKeywordsSetting = addSetting(new StringSetting(
                "keywords", "Custom Watch Words (Comma-Separated)", "Additional words that trigger a mention notification",
                "trade, party, help", 120
        ));
        this.playPingSoundSetting = addSetting(new BooleanSetting(
                "play_sound", "Play Mention Chime", "Play a soft experience-orb chime when mentioned", true
        ));
        instance = this;
        registerListenerOnce();
        addAliases("mentions", "chatping", "nameping", "chatNotify");
    }

    private static synchronized void registerListenerOnce() {
        if (listenerRegistered) {
            return;
        }
        listenerRegistered = true;
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (instance != null && instance.isEnabled()) {
                instance.inspectIncomingMessage(message);
            }
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay && instance != null && instance.isEnabled()) {
                instance.inspectIncomingMessage(message);
            }
        });
    }

    public void inspectIncomingMessage(Text message) {
        if (message == null || client == null) {
            return;
        }
        String raw = message.getString();
        if (raw == null || raw.isBlank()) {
            return;
        }
        String lower = raw.toLowerCase(Locale.ROOT);

        // Ignore messages sent by the local player themselves if prefixed with "<username>"
        String selfName = client.getSession() != null ? client.getSession().getUsername() : "";
        if (!selfName.isEmpty() && lower.startsWith("<" + selfName.toLowerCase(Locale.ROOT) + ">")) {
            return;
        }

        boolean matched = false;
        String matchedTerm = "";

        if (notifyOnUsernameSetting.isEnabled() && !selfName.isEmpty()
                && lower.contains(selfName.toLowerCase(Locale.ROOT))) {
            matched = true;
            matchedTerm = selfName;
        }

        if (!matched) {
            String[] words = customKeywordsSetting.getValue().split(",");
            for (String w : words) {
                String trimmed = w.trim().toLowerCase(Locale.ROOT);
                if (trimmed.length() >= 2 && lower.contains(trimmed)) {
                    matched = true;
                    matchedTerm = w.trim();
                    break;
                }
            }
        }

        if (matched) {
            if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
                BanglaClient.getInstance().getNotificationManager().pushInfo(
                        "Chat Mention (" + matchedTerm + ")",
                        raw
                );
            }
            if (playPingSoundSetting.isEnabled() && client.player != null) {
                client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.65f, 1.25f);
            }
        }
    }
}
