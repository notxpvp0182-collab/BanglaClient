package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.ModuleManager;
import com.ahmed.banglaclient.module.combat.PearlOptimizerModule;
import com.ahmed.banglaclient.module.utility.FastBlockPlacementModule;
import com.ahmed.banglaclient.module.utility.LeftClickDelayModule;
import com.ahmed.banglaclient.module.utility.RightClickDelayModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientInteractionMixin {

    @Shadow private int itemUseCooldown;
    @Shadow public int attackCooldown;
    @Shadow @Nullable public ClientPlayerEntity player;

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void banglaclient$applyInteractionDelayModules(CallbackInfo ci) {
        if (this.player == null) return;
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return;
        ModuleManager mm = bangla.getModuleManager();

        var lcOpt = mm.getModule(LeftClickDelayModule.class);
        if (lcOpt.isPresent() && lcOpt.get().isEnabled()) {
            int cap = lcOpt.get().getMissPenaltyTicks();
            if (this.attackCooldown > cap) {
                this.attackCooldown = cap;
            }
        }

        ItemStack held = this.player.getMainHandStack();
        boolean holdingBlock = !held.isEmpty() && held.getItem() instanceof BlockItem;
        boolean holdingPearl = !held.isEmpty() && held.isOf(Items.ENDER_PEARL);

        if (holdingPearl) {
            var pearlOpt = mm.getModule(PearlOptimizerModule.class);
            if (pearlOpt.isPresent() && pearlOpt.get().shouldOptimizePearlUse()) {
                if (!pearlOpt.get().isStrictCooldownGuard()
                        || !this.player.getItemCooldownManager().isCoolingDown(held)) {
                    if (this.itemUseCooldown > 1) {
                        this.itemUseCooldown = 1;
                    }
                }
            }
        }

        if (holdingBlock) {
            var fastPlaceOpt = mm.getModule(FastBlockPlacementModule.class);
            if (fastPlaceOpt.isPresent() && fastPlaceOpt.get().isEnabled()) {
                int cap = fastPlaceOpt.get().getTargetDelayTicks();
                if (this.itemUseCooldown > cap) {
                    this.itemUseCooldown = cap;
                }
            }
        } else {
            var rcOpt = mm.getModule(RightClickDelayModule.class);
            if (rcOpt.isPresent() && rcOpt.get().isEnabled()) {
                int cap = rcOpt.get().getRightClickTicks();
                if (this.itemUseCooldown > cap) {
                    this.itemUseCooldown = cap;
                }
            }
        }
    }
}
