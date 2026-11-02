package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.chat.ChatCopyModule;
import com.ahmed.banglaclient.module.chat.ChatCustomizerModule;
import com.ahmed.banglaclient.module.chat.ChatHeadsModule;
import com.ahmed.banglaclient.module.chat.ChatTimestampsModule;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Connects ChatHeadsModule, BC Chat Identity, ChatTimestampsModule, ChatCopyModule,
 * and ChatCustomizerModule history retention to Minecraft's ChatHud.
 */
@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

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
        if (headsOpt.isPresent() && headsOpt.get().isEnabled()) {
            modified = headsOpt.get().decorateChatMessage(modified);
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
