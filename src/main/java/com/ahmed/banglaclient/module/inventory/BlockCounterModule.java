package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Module 46: Block Counter
 * Displays the total count across the entire inventory of the block type currently held in the player's hand,
 * or all placeable building blocks if configured.
 */
public final class BlockCounterModule extends HudModule {
    private final BooleanSetting countAllPlaceableBlocksSetting;

    public BlockCounterModule() {
        super(
                "block_counter",
                "Block Counter",
                "Displays the total inventory count for the held block or all building blocks.",
                ModuleCategory.INVENTORY,
                415.0f,
                218.0f,
                84.0f,
                24.0f,
                false
        );
        this.countAllPlaceableBlocksSetting = addSetting(new BooleanSetting(
                "count_all_blocks", "Count All Blocks", "Sum all BlockItems in inventory when not holding a specific block", true
        ));
        addAliases("blocks", "scaffoldcounter", "buildingblocks");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        ItemStack iconStack = new ItemStack(Items.COBBLESTONE);
        int totalBlocks = 0;

        if (client.player != null) {
            ItemStack main = client.player.getMainHandStack();
            ItemStack off = client.player.getOffHandStack();
            Item heldBlockItem = null;

            if (main.getItem() instanceof BlockItem) {
                heldBlockItem = main.getItem();
                iconStack = main;
            } else if (off.getItem() instanceof BlockItem) {
                heldBlockItem = off.getItem();
                iconStack = off;
            }

            if (heldBlockItem != null) {
                var inv = client.player.getInventory();
                for (int i = 0; i < inv.size(); i++) {
                    ItemStack s = inv.getStack(i);
                    if (!s.isEmpty() && s.isOf(heldBlockItem)) {
                        totalBlocks += s.getCount();
                    }
                }
            } else if (countAllPlaceableBlocksSetting.isEnabled()) {
                var inv = client.player.getInventory();
                for (int i = 0; i < inv.size(); i++) {
                    ItemStack s = inv.getStack(i);
                    if (!s.isEmpty() && s.getItem() instanceof BlockItem) {
                        totalBlocks += s.getCount();
                    }
                }
            } else if (!preview) {
                return;
            }
        } else if (preview) {
            totalBlocks = 128;
        }

        String text = totalBlocks + " Blocks";
        int pad = paddingSetting.getIntValue();
        int boxW = 20 + client.textRenderer.getWidth(text) + pad * 2;
        int boxH = 16 + pad * 2;

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

        context.drawItem(iconStack, x + pad, y + pad);
        context.drawTextWithShadow(client.textRenderer, text, x + pad + 20, y + pad + 4, textColorSetting.getArgb());
    }
}
