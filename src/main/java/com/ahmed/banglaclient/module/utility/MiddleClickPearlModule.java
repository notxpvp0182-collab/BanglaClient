package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * "Middle Click to Pearl" module.
 * Visibly selects the hotbar slot containing an Ender Pearl, performs the normal vanilla
 * pearl throw on the next tick, and restores the previously selected hotbar slot.
 */
public final class MiddleClickPearlModule extends Module {
    private final BooleanSetting enableMiddleMouseSetting;
    private final NumberSetting restoreDelayTicksSetting;
    private final BooleanSetting notifyWhenMissingSetting;

    private int savedSlot = -1;
    private int targetPearlSlot = -1;
    private int statePhase = 0; // 0 = idle, 1 = slot selected (ready to use), 2 = waiting to restore slot
    private int ticksRemaining = 0;

    public MiddleClickPearlModule() {
        super(
                "middle_click_pearl",
                "Middle Click to Pearl",
                "Visibly selects hotbar Ender Pearl on middle-click, throws it, and restores your previous slot.",
                ModuleCategory.UTILITY
        );
        this.enableMiddleMouseSetting = addSetting(new BooleanSetting(
                "middle_mouse", "Use Middle Mouse Button", "Trigger Ender Pearl throw when pressing Middle Mouse Button", true
        ));
        this.restoreDelayTicksSetting = addSetting(new NumberSetting(
                "restore_delay", "Slot Restore Delay", "Ticks to keep the Ender Pearl visibly held before restoring previous slot",
                2.0, 1.0, 8.0, 1.0, "t"
        ));
        this.notifyWhenMissingSetting = addSetting(new BooleanSetting(
                "notify_missing", "Notify When No Pearl", "Show a warning toast if no Ender Pearl is found on the hotbar", true
        ));
        addAliases("mcp", "middleclickpearl", "quickpearl", "enderpearl");
    }

    public void onMiddleMouseClick() {
        if (!isEnabled() || !enableMiddleMouseSetting.isEnabled()) return;
        triggerPearlSequence();
    }

    public void triggerPearlSequence() {
        if (client == null || client.player == null || client.interactionManager == null) return;
        if (client.currentScreen != null || statePhase != 0) return;

        ItemStack pearlSample = new ItemStack(Items.ENDER_PEARL);
        if (client.player.getItemCooldownManager().isCoolingDown(pearlSample)) {
            if (notifyWhenMissingSetting.isEnabled() && BanglaClient.getInstance() != null) {
                BanglaClient.getInstance().getNotificationManager().pushWarning("Ender Pearl", "Ender Pearl is currently on cooldown!");
            }
            return;
        }

        int foundSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = client.player.getInventory().getStack(i);
            if (!s.isEmpty() && s.isOf(Items.ENDER_PEARL)) {
                foundSlot = i;
                break;
            }
        }

        if (foundSlot == -1) {
            if (notifyWhenMissingSetting.isEnabled() && BanglaClient.getInstance() != null) {
                BanglaClient.getInstance().getNotificationManager().pushWarning("Middle Click Pearl", "No Ender Pearl found on hotbar (Slots 1-9).");
            }
            return;
        }

        this.savedSlot = client.player.getInventory().getSelectedSlot();
        this.targetPearlSlot = foundSlot;
        // Step 1: Visibly switch hotbar slot to the Ender Pearl slot
        client.player.getInventory().setSelectedSlot(this.targetPearlSlot);
        this.statePhase = 1;
        this.ticksRemaining = 1;
    }

    @Override
    protected void onDisable() {
        resetSequence();
    }

    private void resetSequence() {
        this.statePhase = 0;
        this.savedSlot = -1;
        this.targetPearlSlot = -1;
        this.ticksRemaining = 0;
    }

    @Override
    public void onTick() {
        if (client == null || client.player == null || client.interactionManager == null) {
            resetSequence();
            return;
        }
        if (statePhase == 1) {
            // Step 2: Player is now visibly holding the Ender Pearl -> perform legitimate vanilla interactItem
            client.player.getInventory().setSelectedSlot(this.targetPearlSlot);
            client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
            client.player.swingHand(Hand.MAIN_HAND);
            this.statePhase = 2;
            this.ticksRemaining = restoreDelayTicksSetting.getIntValue();
        } else if (statePhase == 2) {
            ticksRemaining--;
            if (ticksRemaining <= 0) {
                // Step 3: Restore original hotbar slot
                if (savedSlot >= 0 && savedSlot < 9) {
                    client.player.getInventory().setSelectedSlot(savedSlot);
                }
                resetSequence();
            }
        }
    }
}
