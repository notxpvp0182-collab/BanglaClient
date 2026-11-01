package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Module 43: Inventory HUD
 * Renders the player's 27 main inventory slots (3 rows of 9 columns) inside a Liquid Glass HUD widget.
 */
public final class InventoryHudModule extends HudModule {
    private final BooleanSetting drawSlotGridSetting;

    public InventoryHudModule() {
        super(
                "inventory_hud",
                "Inventory HUD",
                "Displays the 27 main inventory slots (3x9 grid) as a configurable HUD widget.",
                ModuleCategory.INVENTORY,
                415.0f,
                70.0f,
                168.0f,
                60.0f,
                false
        );
        this.drawSlotGridSetting = addSetting(new BooleanSetting(
                "slot_grid", "Draw Slot Outlines", "Render subtle 1px cell frames behind each inventory slot", true
        ));
        addAliases("inv", "invhud", "slots", "backpack");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int slotSize = 18;
        int pad = paddingSetting.getIntValue();
        int boxW = 9 * slotSize + pad * 2;
        int boxH = 3 * slotSize + pad * 2;

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

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = 9 + row * 9 + col;
                int sx = x + pad + col * slotSize;
                int sy = y + pad + row * slotSize;

                if (drawSlotGridSetting.isEnabled()) {
                    context.fill(sx, sy, sx + slotSize - 1, sy + slotSize - 1, 0x16FFFFFF);
                }

                ItemStack stack = ItemStack.EMPTY;
                if (client.player != null) {
                    stack = client.player.getInventory().getStack(slotIndex);
                } else if (preview && row == 0 && col < 3) {
                    stack = new ItemStack(Items.GOLDEN_APPLE, 16);
                }

                if (!stack.isEmpty()) {
                    context.drawItem(stack, sx + 1, sy + 1);
                    context.drawStackOverlay(client.textRenderer, stack, sx + 1, sy + 1);
                }
            }
        }
    }
}
