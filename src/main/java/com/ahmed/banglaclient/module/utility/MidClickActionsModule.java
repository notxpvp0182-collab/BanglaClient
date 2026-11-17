package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.EnumSetting;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public final class MidClickActionsModule extends Module {
    public enum MidClickMode {
        ROCKET("Mid Click → Rocket"),
        ELYTRA("Mid Click → Elytra Swap"),
        XP_BOTTLE("Mid Click → XP Bottle");

        private final String label;
        MidClickMode(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    private final EnumSetting<MidClickMode> modeSetting;
    private final BooleanSetting fastXpBottleSetting;

    public MidClickActionsModule() {
        super("mid_click_actions", "Mid Click Elytra / Rocket / XP",
                "Assign Middle Mouse Button to use Firework Rocket, swap Elytra/Chestplate, or throw XP Bottles.",
                ModuleCategory.UTILITY);
        this.modeSetting = addSetting(new EnumSetting<>("mode", "Mid Click Action", "Action triggered on middle mouse click", MidClickMode.ROCKET));
        this.fastXpBottleSetting = addSetting(new BooleanSetting("fast_xp", "Fast XP Bottle", "Remove delay when throwing Experience Bottles", true));
        addAliases("midclickelytra", "midclickrocket", "midclickxp", "fastxp", "elytraswap", "fireworkbind");
    }

    public void onMiddleClick() {
        if (!isEnabled() || client == null || client.player == null || client.interactionManager == null) return;
        switch (modeSetting.getValue()) {
            case ROCKET -> useHotbarItem(Items.FIREWORK_ROCKET);
            case XP_BOTTLE -> useHotbarItem(Items.EXPERIENCE_BOTTLE);
            case ELYTRA -> swapElytraChestplate();
        }
    }

    private void useHotbarItem(Item targetItem) {
        int prevSlot = client.player.getInventory().getSelectedSlot();
        for (int i = 0; i < 9; i++) {
            ItemStack st = client.player.getInventory().getStack(i);
            if (!st.isEmpty() && st.isOf(targetItem)) {
                client.player.getInventory().setSelectedSlot(i);
                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                client.player.getInventory().setSelectedSlot(prevSlot);
                return;
            }
        }
    }

    private void swapElytraChestplate() {
        ItemStack chest = client.player.getEquippedStack(EquipmentSlot.CHEST);
        boolean wearingElytra = chest.isOf(Items.ELYTRA);
        for (int i = 0; i < 36; i++) {
            ItemStack st = client.player.getInventory().getStack(i);
            if (st.isEmpty()) continue;
            if ((wearingElytra && (st.isOf(Items.NETHERITE_CHESTPLATE) || st.isOf(Items.DIAMOND_CHESTPLATE)))
                    || (!wearingElytra && st.isOf(Items.ELYTRA))) {
                int slotId = i < 9 ? (i + 36) : i;
                client.interactionManager.clickSlot(client.player.playerScreenHandler.syncId, slotId, 0, SlotActionType.PICKUP, client.player);
                client.interactionManager.clickSlot(client.player.playerScreenHandler.syncId, 6, 0, SlotActionType.PICKUP, client.player);
                client.interactionManager.clickSlot(client.player.playerScreenHandler.syncId, slotId, 0, SlotActionType.PICKUP, client.player);
                return;
            }
        }
    }

    public boolean isFastXpEnabled() {
        return isEnabled() && fastXpBottleSetting.isEnabled();
    }
}
