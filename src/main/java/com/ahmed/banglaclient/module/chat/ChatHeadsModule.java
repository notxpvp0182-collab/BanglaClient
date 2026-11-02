package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.identity.BcIdentityManager;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.Collection;

/**
 * "Chat Heads" module displaying sender head/identity badges beside normal chat,
 * player join, and player leave messages without altering server chat data.
 */
public final class ChatHeadsModule extends Module {
    private final BooleanSetting supportJoinLeaveSetting;
    private final BooleanSetting showBcChatBadgeSetting;

    public ChatHeadsModule() {
        super(
                "chat_heads",
                "Chat Heads",
                "Displays player skin head indicators and BC identity badges beside chat messages.",
                ModuleCategory.CHAT,
                -1,
                true
        );
        this.supportJoinLeaveSetting = addSetting(new BooleanSetting(
                "join_leave", "Support Join / Leave Messages", "Attach sender head indicator to player join and leave messages", true
        ));
        this.showBcChatBadgeSetting = addSetting(new BooleanSetting(
                "bc_chat_badge", "Show BC Identity in Chat", "Display [BC] badge before verified BANGLA CLIENT users in chat", true
        ));
        addAliases("chathead", "senderhead", "playerheads");
    }

    public Text decorateChatMessage(Text original) {
        if (!isEnabled() || original == null) return original;
        String raw = original.getString();
        if (raw == null || raw.isBlank()) return original;

        // Avoid double-decorating
        if (raw.contains("[BC]") || raw.startsWith("▪ ")) return original;

        String detectedSender = resolveSenderFromLine(raw);
        if (detectedSender == null) return original;

        MutableText prefix = Text.empty();
        if (showBcChatBadgeSetting.isEnabled() && BcIdentityManager.isVerifiedBcUserByName(detectedSender)) {
            prefix.append(BcIdentityManager.createBcPrefixBadge());
        }

        // Stylized head emblem colored deterministically from the sender's skin/UUID
        int headColor = 0xFFEF4444;
        if (client != null && client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(detectedSender);
            if (entry != null && entry.getProfile() != null && entry.getProfile().getId() != null) {
                int hash = entry.getProfile().getId().hashCode();
                int r = 110 + (Math.abs(hash >> 16) % 135);
                int g = 110 + (Math.abs(hash >> 8) % 135);
                int b = 110 + (Math.abs(hash) % 135);
                headColor = (r << 16) | (g << 8) | b;
            }
        }
        MutableText headGlyph = Text.literal("▪ ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(headColor)));
        prefix.append(headGlyph);
        return prefix.append(original);
    }

    private String resolveSenderFromLine(String raw) {
        if (client != null && client.getNetworkHandler() != null) {
            Collection<PlayerListEntry> entries = client.getNetworkHandler().getPlayerList();
            for (PlayerListEntry entry : entries) {
                if (entry.getProfile() == null || entry.getProfile().getName() == null) continue;
                String name = entry.getProfile().getName();
                if (name.length() < 3) continue;
                if (raw.contains("<" + name + ">") || raw.startsWith(name + ":") || raw.contains(" " + name + ":")) {
                    return name;
                }
                if (supportJoinLeaveSetting.isEnabled() && (raw.contains(name + " joined") || raw.contains(name + " left"))) {
                    return name;
                }
            }
        }
        if (client != null && client.getSession() != null) {
            String self = client.getSession().getUsername();
            if (self != null && (raw.contains("<" + self + ">") || raw.startsWith(self + ":") || raw.contains(self + " joined"))) {
                return self;
            }
        }
        return null;
    }
}
