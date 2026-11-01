package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Module 49: Equipment HUD
 * Displays a unified overview of all 6 player equipment slots (Mainhand, Offhand, Helmet, Chestplate, Leggings, Boots).
 */
public final class EquipmentHudModule extends HudModule {
    private static final EquipmentSlot[] ALL_SLOTS = {
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND,
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    public EquipmentHudModule() {
        super(
                "equipment_hud",
                "Equipment HUD",
                "Displays a unified overview of Mainhand, Offhand, and all 4 Armor slots.",
                ModuleCategory.INVENTORY,
                590.0f,
                10.0f,
                122.0f,
                24.0f,
                false
        );
        addAliases("gear", "loadout", "equipment");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        List<ItemStack> equipped = new ArrayList<>(6);
        if (client.player != null) {
            for (EquipmentSlot slot : ALL_SLOTS) {
                ItemStack s = client.player.getEquippedStack(slot);
                if (!s.isEmpty()) {
                    equipped.add(s);
                }
            }
        }

        if (equipped.isEmpty() && preview) {
            equipped.add(new ItemStack(Items.DIAMOND_SWORD));
            equipped.add(new ItemStack(Items.SHIELD));
            equipped.add(new ItemStack(Items.DIAMOND_HELMET));
            equipped.add(new ItemStack(Items.DIAMOND_CHESTPLATE));
        }

        if (equipped.isEmpty()) {
            return;
        }

        int slotW = 19;
        int pad = paddingSetting.getIntValue();
        int boxW = equipped.size() * slotW + pad * 2;
        int boxH = 18 + pad * 2;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        int curX = x + pad;
        for (ItemStack s : equipped) {
            context.drawItem(s, curX + 1, y + pad + 1);
            context.drawStackOverlay(client.textRenderer, s, curX + 1, y + pad + 1);
            curX += slotW;
        }
    }
}
