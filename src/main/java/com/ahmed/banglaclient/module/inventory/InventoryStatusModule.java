package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

/**
 * Module 57: Inventory Status
 * Displays occupied vs. available main inventory slots (`Occupied: X/36` or `Free: Y slots`)
 * and highlights when the player's inventory is full.
 */
public final class InventoryStatusModule extends HudModule {
    private final BooleanSetting showFreeSlotsInsteadSetting;

    public InventoryStatusModule() {
        super(
                "inventory_status",
                "Inventory Status",
                "Displays inventory slot occupancy and available free slots.",
                ModuleCategory.INVENTORY,
                590.0f,
                256.0f,
                104.0f,
                20.0f,
                false
        );
        this.showFreeSlotsInsteadSetting = addSetting(new BooleanSetting(
                "show_free", "Show Free Slots Count", "Display remaining empty slots instead of occupied slots", false
        ));
        addAliases("invstatus", "freeslots", "fullinventory", "slotsleft");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int occupied = 14;
        int totalSlots = 36;

        if (client.player != null) {
            occupied = 0;
            var mainInv = client.player.getInventory().main;
            totalSlots = mainInv.size();
            for (ItemStack s : mainInv) {
                if (!s.isEmpty()) {
                    occupied++;
                }
            }
        }

        int free = Math.max(0, totalSlots - occupied);
        String text = showFreeSlotsInsteadSetting.isEnabled()
                ? ("Free Slots: " + free + "/" + totalSlots)
                : ("Slots: " + occupied + "/" + totalSlots);

        if (free == 0) {
            text += " [FULL]";
        }

        int pad = paddingSetting.getIntValue();
        int boxW = client.textRenderer.getWidth(text) + pad * 2;
        int boxH = client.textRenderer.fontHeight + pad * 2;

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

        int color = free == 0
                ? ThemeTokens.STATUS_ERROR
                : (free <= 4 ? ThemeTokens.STATUS_WARNING : textColorSetting.getArgb());
        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, color);
    }
}
