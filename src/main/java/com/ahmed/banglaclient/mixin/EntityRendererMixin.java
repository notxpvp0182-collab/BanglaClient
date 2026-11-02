package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.identity.BcIdentityManager;
import com.ahmed.banglaclient.module.visual.NametagsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true, require = 0)
    private void banglaclient$appendBcIdentityToNametag(T entity, CallbackInfoReturnable<Text> cir) {
        if (!(entity instanceof PlayerEntity player)) return;
        Text original = cir.getReturnValue();
        if (original == null) return;

        BanglaClient bangla = BanglaClient.getInstance();
        boolean showBadge = true;
        if (bangla != null && bangla.getModuleManager() != null) {
            var modOpt = bangla.getModuleManager().getModule(NametagsModule.class);
            if (modOpt.isPresent()) {
                showBadge = modOpt.get().shouldShowBcBadge();
            }
        }

        if (showBadge && BcIdentityManager.isVerifiedBcUser(player.getUuid())) {
            String raw = original.getString();
            if (!raw.contains("[BC]")) {
                cir.setReturnValue(BcIdentityManager.createBcPrefixBadge().append(original));
            }
        }
    }

    @Mixin(LivingEntityRenderer.class)
    public static abstract class LivingNametagHookMixin<T extends LivingEntity> {

        @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true, require = 0)
        private void banglaclient$showOwnNametagInThirdPerson(T entity, double squaredDistanceToCamera, CallbackInfoReturnable<Boolean> cir) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.player == null || entity != mc.player) return;
            if (mc.options.getPerspective().isFirstPerson() || !MinecraftClient.isHudEnabled()) return;

            BanglaClient bangla = BanglaClient.getInstance();
            if (bangla != null && bangla.getModuleManager() != null) {
                var nametagsOpt = bangla.getModuleManager().getModule(NametagsModule.class);
                if (nametagsOpt.isPresent() && nametagsOpt.get().shouldShowOwnNametag()) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
