package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.cosmetics.PlayerPreviewRenderer;
import com.ahmed.banglaclient.cosmetics.SkinCapeManager;
import com.ahmed.banglaclient.identity.BcIdentityManager;
import com.ahmed.banglaclient.module.hud.TabListHeadsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {

    @Unique
    private PlayerListEntry currentRenderingEntry = null;

    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 0, require = 0)
    private boolean banglaclient$forceShowPlayerHeadsInTabList(boolean showHeads) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var tabOpt = bangla.getModuleManager().getModule(TabListHeadsModule.class);
            if (tabOpt.isPresent() && tabOpt.get().isEnabled()) {
                return true;
            }
        }
        return showHeads;
    }

    @Inject(method = "getPlayerName(Lnet/minecraft/client/network/PlayerListEntry;)Lnet/minecraft/text/Text;", at = @At("HEAD"), require = 0)
    private void banglaclient$captureRenderingEntry(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        this.currentRenderingEntry = entry;
    }

    @Inject(method = "getPlayerName(Lnet/minecraft/client/network/PlayerListEntry;)Lnet/minecraft/text/Text;", at = @At("RETURN"), cancellable = true, require = 0)
    private void banglaclient$indentTabNameForIconAndHead(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        if (entry == null || entry.getProfile() == null) return;
        Text original = cir.getReturnValue();
        if (original == null) return;

        boolean isBc = BcIdentityManager.isVerifiedBcUser(entry.getProfile().id())
                || BcIdentityManager.isVerifiedBcUserByName(entry.getProfile().name());

        if (isBc) {
            // Indent 10px so [ICON] renders before [HEAD] and [NAME] with clean slot spacing
            MutableText spacing = Text.literal("   ");
            cir.setReturnValue(spacing.append(original));
        }
    }

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"),
            require = 0
    )
    private void banglaclient$drawTabEntryWithIconAndHead(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int color) {
        PlayerListEntry entry = this.currentRenderingEntry;
        if (entry == null || entry.getProfile() == null) {
            context.drawTextWithShadow(textRenderer, text, x, y, color); return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        boolean isLocal = mc.player != null && entry.getProfile().id().equals(mc.player.getUuid());

        boolean isBc = BcIdentityManager.isVerifiedBcUser(entry.getProfile().id())
                || BcIdentityManager.isVerifiedBcUserByName(entry.getProfile().name());

        BanglaClient bangla = BanglaClient.getInstance();
        boolean headsModuleEnabled = true;
        if (bangla != null && bangla.getModuleManager() != null) {
            var tabOpt = bangla.getModuleManager().getModule(TabListHeadsModule.class);
            if (tabOpt.isPresent()) {
                headsModuleEnabled = tabOpt.get().isEnabled();
            }
        }

        // Order: [BANGLA CLIENT ICON] [SKIN HEAD] [PLAYER NAME]
        if (isBc) {
            int iconX = headsModuleEnabled ? (x - 19) : (x - 10);
            BcIdentityManager.drawIdentityIcon(context, iconX, y, 8, 8);
        }

        if (headsModuleEnabled) {
            int headX = isBc ? (x - 10) : (x - 9);
            SkinTextures textures = isLocal && SkinCapeManager.getInstance().isCustomSkinOverride()
                    ? SkinCapeManager.getInstance().getActiveSkinTextures()
                    : entry.getSkinTextures();
            PlayerPreviewRenderer.drawPlayerHead(context, textures, headX, y, 8);
        }

        context.drawTextWithShadow(textRenderer, text, x, y, color); return;
    }
}
