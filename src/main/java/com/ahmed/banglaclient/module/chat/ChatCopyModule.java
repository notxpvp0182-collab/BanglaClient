package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Module 88: Chat Copy
 * Allows copying any chat message to the system clipboard by clicking the `[Copy]` action badge
 * or copying the most recent message when toggled/keybound.
 */
public final class ChatCopyModule extends Module {
    private final BooleanSetting stripColorCodesSetting;
    private String lastMessagePlain = "";

    private static boolean listenerRegistered = false;
    private static ChatCopyModule instance;

    public ChatCopyModule() {
        super(
                "chat_copy",
                "Chat Copy",
                "Allows copying chat messages to the system clipboard with one click.",
                ModuleCategory.CHAT
        );
        this.stripColorCodesSetting = addSetting(new BooleanSetting(
                "strip_formatting", "Strip Formatting Codes", "Remove § color/formatting codes when copying text", true
        ));
        instance = this;
        registerListenerOnce();
        addAliases("copychat", "clipboardchat", "copy");
    }

    private static synchronized void registerListenerOnce() {
        if (listenerRegistered) {
            return;
        }
        listenerRegistered = true;
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (instance != null && message != null) {
                instance.lastMessagePlain = message.getString();
            }
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay && instance != null && message != null) {
                instance.lastMessagePlain = message.getString();
            }
        });
    }

    /**
     * Appends an interactive `[Copy]` click event tag to a chat message when enabled.
     */
    public Text appendCopyButton(Text original) {
        if (!isEnabled() || original == null) {
            return original;
        }
        String raw = original.getString();
        if (stripColorCodesSetting.isEnabled()) {
            raw = Formatting.strip(raw);
        }
        if (raw == null || raw.isBlank()) {
            return original;
        }

        MutableText copyBadge = Text.literal(" [Copy]").setStyle(
                Style.EMPTY
                        .withFormatting(Formatting.DARK_GRAY)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, raw))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to copy message to clipboard")))
        );
        return original.copy().append(copyBadge);
    }

    public void copyLatestMessageToClipboard() {
        if (client == null || client.keyboard == null || lastMessagePlain.isBlank()) {
            return;
        }
        String toCopy = stripColorCodesSetting.isEnabled() ? Formatting.strip(lastMessagePlain) : lastMessagePlain;
        if (toCopy != null) {
            client.keyboard.setClipboard(toCopy);
            if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
                BanglaClient.getInstance().getNotificationManager().pushSuccess("Chat Copy", "Copied latest chat message to clipboard.");
            }
        }
    }
}
