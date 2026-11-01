package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Module 45: Item Counter
 * Displays inventory counts for configurable key utility items (Golden Apples, Ender Pearls, XP Bottles, Wind Charges).
 */
public final class ItemCounterModule extends HudModule {
    private final BooleanSetting countGoldenApplesSetting;
    private final BooleanSetting countEnderPearlsSetting;
    private final BooleanSetting countXpBottlesSetting;
    private final BooleanSetting countWindChargesSetting;

    public ItemCounterModule() {
        super(
                "item_counter",
                "Item Counter",
                "Displays live inventory counts for configurable utility items.",
                ModuleCategory.INVENTORY,
                415.0f,
                168.0f,
                110.0f,
                44.0f,
                false
        );
        this.countGoldenApplesSetting = addSetting(new BooleanSetting(
                "gapples", "Count Golden Apples", "Include Golden Apples and Enchanted Golden Apples", true
        ));
        this.countEnderPearlsSetting = addSetting(new BooleanSetting(
                "pearls", "Count Ender Pearls", "Include Ender Pearls", true
        ));
        this.countXpBottlesSetting = addSetting(new BooleanSetting(
                "xp_bottles", "Count Bottles o' Enchanting", "Include Experience Bottles", true
        ));
        this.countWindChargesSetting = addSetting(new BooleanSetting(
                "wind_charges", "Count Wind Charges", "Include Wind Charges", true
        ));
        addAliases("items", "gapplecounter", "pearlcounter");
    }

    private int countItemInInventory(Item... targets) {
        if (client.player == null || targets == null) {
            return 0;
        }
        int sum = 0;
        var inv = client.player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty()) {
                for (Item target : targets) {
                    if (stack.isOf(target)) {
                        sum += stack.getCount();
                        break;
                    }
                }
            }
        }
        return sum;
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        List<ItemStack> displayIcons = new ArrayList<>(4);
        List<Integer> counts = new ArrayList<>(4);

        if (countGoldenApplesSetting.isEnabled()) {
            displayIcons.add(new ItemStack(Items.GOLDEN_APPLE));
            counts.add(countItemInInventory(Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE));
        }
        if (countEnderPearlsSetting.isEnabled()) {
            displayIcons.add(new ItemStack(Items.ENDER_PEARL));
            counts.add(countItemInInventory(Items.ENDER_PEARL));
        }
        if (countXpBottlesSetting.isEnabled()) {
            displayIcons.add(new ItemStack(Items.EXPERIENCE_BOTTLE));
            counts.add(countItemInInventory(Items.EXPERIENCE_BOTTLE));
        }
        if (countWindChargesSetting.isEnabled()) {
            displayIcons.add(new ItemStack(Items.WIND_CHARGE));
            counts.add(countItemInInventory(Items.WIND_CHARGE));
        }

        if (displayIcons.isEmpty()) {
            return;
        }

        int pad = paddingSetting.getIntValue();
        int cellW = 42;
        int boxW = displayIcons.size() * cellW + pad * 2;
        int boxH = 20 + pad * 2;

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
        for (int i = 0; i < displayIcons.size(); i++) {
            context.drawItem(displayIcons.get(i), curX, y + pad + 2);
            String label = "x" + counts.get(i);
            context.drawTextWithShadow(client.textRenderer, label, curX + 18, y + pad + 6, textColorSetting.getArgb());
            curX += cellW;
        }
    }
}
