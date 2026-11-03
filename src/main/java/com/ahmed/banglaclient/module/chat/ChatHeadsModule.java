package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.identity.BcIdentityManager;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Collection;

public final class ChatHeadsModule extends Module {
    private final BooleanSetting supportJoinLeaveSetting;

    public ChatHeadsModule() {
        super(
                "chat_heads",
                "Chat Heads",
                "Renders player skin heads and official BC identity icon in chat.",
                ModuleCategory.CHAT,
                -1,
                true
        );
        this.supportJoinLeaveSetting = addSetting(new BooleanSetting(
                "join_leave", "Support Join / Leave Messages", "Attach sender head to join and leave announcements", true
        ));
        addAliases("chathead", "senderhead", "playerheads");
    }

    public Text indentMessageForIcons(Text original, String detectedSender) {
        if (original == null || detectedSender == null) return original;
        boolean isBc = BcIdentityManager.isVerifiedBcUserByName(detectedSender);
        boolean hasHead = isEnabled();

        // 3 spaces = ~10px for 1 icon; 6 spaces = ~20px for both [ICON] and [HEAD]
        String indent = (isBc && hasHead) ? "      " : ((isBc || hasHead) ? "   " : "");
        if (indent.isEmpty()) return original;

        MutableText prefix = Text.literal(indent);
        return prefix.append(original);
    }

    public String resolveSenderFromLine(String raw) {
        if (raw == null || raw.isBlank()) return null;
        if (client != null && client.getNetworkHandler() != null) {
            Collection<PlayerListEntry> entries = client.getNetworkHandler().getPlayerList();
            for (PlayerListEntry entry : entries) {
                if (entry.getProfile() == null || entry.getProfile().name() == null) continue;
                String name = entry.getProfile().name();
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
