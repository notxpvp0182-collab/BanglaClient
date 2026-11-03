package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.cosmetics.PlayerPreviewRenderer;
import com.ahmed.banglaclient.cosmetics.SkinCapeManager;
import com.ahmed.banglaclient.identity.BcIdentityManager;
import com.ahmed.banglaclient.module.chat.ChatCopyModule;
import com.ahmed.banglaclient.module.chat.ChatCustomizerModule;
import com.ahmed.banglaclient.module.chat.ChatHeadsModule;
import com.ahmed.banglaclient.module.chat.ChatTimestampsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Shadow @Final private List<ChatHudLine.Visible> visibleMessages;
    @Shadow private int scrolledLines;
    @Shadow public abstract double getChatScale();

    @Unique
    private final Map<String, String> lineSenderCache = new HashMap<>();

    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
            at = @At("HEAD"),
            argsOnly = true,
            require = 0
    )
    private Text banglaclient$decorateIncomingChatMessage(Text message) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null || message == null) {
            return message;
        }
        Text modified = message;

        var headsOpt = bangla.getModuleManager().getModule(ChatHeadsModule.class);
        if (headsOpt.isPresent()) {
            String sender = headsOpt.get().resolveSenderFromLine(message.getString());
            if (sender != null) {
                lineSenderCache.put(message.getString().trim(), sender);
                modified = headsOpt.get().indentMessageForIcons(modified, sender);
            }
        }

        var tsOpt = bangla.getModuleManager().getModule(ChatTimestampsModule.class);
        if (tsOpt.isPresent() && tsOpt.get().isEnabled()) {
            modified = tsOpt.get().formatMessageWithTimestamp(modified);
        }

        var copyOpt = bangla.getModuleManager().getModule(ChatCopyModule.class);
        if (copyOpt.isPresent() && copyOpt.get().isEnabled()) {
            modified = copyOpt.get().appendCopyButton(modified);
        }

        return modified;
    }

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void banglaclient$drawChatHeadsAndIdentityIcon(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null || visibleMessages.isEmpty()) return;

        BanglaClient bangla = BanglaClient.getInstance();
        var headsOpt = bangla != null && bangla.getModuleManager() != null
                ? bangla.getModuleManager().getModule(ChatHeadsModule.class)
                : null;
        boolean headsEnabled = headsOpt != null && headsOpt.isPresent() && headsOpt.get().isEnabled();

        int maxLines = ((ChatHud) (Object) this).getVisibleLineCount();
        int total = Math.min(visibleMessages.size() - scrolledLines, maxLines);

        for (int i = 0; i < total; i++) {
            int lineIdx = i + scrolledLines;
            if (lineIdx < 0 || lineIdx >= visibleMessages.size()) continue;

            ChatHudLine.Visible line = visibleMessages.get(lineIdx);
            if (line == null) continue;

            String raw = line.content().getString().trim();
            String sender = findSenderForLine(raw);
            if (sender == null) continue;

            boolean isBc = BcIdentityManager.isVerifiedBcUserByName(sender);
            if (!isBc && !headsEnabled) continue;

            int lineY = -((i + 1) * 9);
            int startX = 0;

            // Render Order: [BANGLA CLIENT ICON] [SENDER SKIN HEAD] [NAME]: Message
            if (isBc) {
                BcIdentityManager.drawIdentityIcon(context, startX, lineY, 8, 8);
                startX += 9;
            }

            if (headsEnabled) {
                SkinTextures skin = resolveSenderSkinTextures(mc, sender);
                PlayerPreviewRenderer.drawPlayerHead(context, skin, startX, lineY, 8);
            }
        }
    }

    @Unique
    private String findSenderForLine(String raw) {
        if (raw == null) return null;
        for (Map.Entry<String, String> e : lineSenderCache.entrySet()) {
            if (raw.contains(e.getKey()) || e.getKey().contains(raw)) {
                return e.getValue();
            }
        }
        return null;
    }

    @Unique
    private SkinTextures resolveSenderSkinTextures(MinecraftClient mc, String sender) {
        if (mc.player != null && mc.getSession() != null && sender.equalsIgnoreCase(mc.getSession().getUsername())) {
            return SkinCapeManager.getInstance().isCustomSkinOverride()
                    ? SkinCapeManager.getInstance().getActiveSkinTextures()
                    : SkinCapeManager.getAuthenticatedAccountDefaultSkin(mc);
        }
        if (mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(sender);
            if (entry != null) {
                return entry.getSkinTextures();
            }
        }
        return SkinCapeManager.getAuthenticatedAccountDefaultSkin(mc);
    }

    @Inject(method = "clear", at = @At("HEAD"), cancellable = true, require = 0)
    private void banglaclient$preserveChatHistoryOnClear(boolean clearHistory, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var customizerOpt = bangla.getModuleManager().getModule(ChatCustomizerModule.class);
            if (customizerOpt.isPresent() && customizerOpt.get().shouldKeepHistoryOnDisconnect()) {
                ci.cancel();
            }
        }
    }
}
