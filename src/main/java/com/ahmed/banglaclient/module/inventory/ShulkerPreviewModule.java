package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.block.ShulkerBoxBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Module 48: Shulker Preview
 * Displays the 27-slot contents of a Shulker Box when held in mainhand/offhand or inspected in inventory
 * using Minecraft 1.21.11's legitimate `DataComponentTypes.CONTAINER` API.
 */
public final class ShulkerPreviewModule extends HudModule {
    private final BooleanSetting checkHotbarShulkersSetting;

    public ShulkerPreviewModule() {
        super(
                "shulker_preview",
                "Shulker Preview",
                "Displays the 27-slot contents of a held or hotbar Shulker Box.",
                ModuleCategory.INVENTORY,
                415.0f,
                322.0f,
                168.0f,
                68.0f,
                false
        );
        this.checkHotbarShulkersSetting = addSetting(new BooleanSetting(
                "scan_hotbar", "Fallback to Hotbar Shulker", "If not holding a Shulker Box, preview the first Shulker Box on the hotbar", false
        ));
        addAliases("shulker", "shulkerbox", "shulkerhud");
    }

    private ItemStack findTargetShulker() {
        if (client.player == null) {
            return ItemStack.EMPTY;
        }
        ItemStack main = client.player.getMainHandStack();
        if (isShulkerItem(main)) {
            return main;
        }
        ItemStack off = client.player.getOffHandStack();
        if (isShulkerItem(off)) {
            return off;
        }
        if (checkHotbarShulkersSetting.isEnabled()) {
            for (int i = 0; i < 9; i++) {
                ItemStack s = client.player.getInventory().getStack(i);
                if (isShulkerItem(s)) {
                    return s;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isShulkerItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock;
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        ItemStack shulker = findTargetShulker();
        if (shulker.isEmpty() && !preview) {
            return;
        }

        String title = shulker.isEmpty() ? "Shulker Box Preview" : shulker.getName().getString();
        List<ItemStack> slots = new ArrayList<>(27);
        if (!shulker.isEmpty()) {
            ContainerComponent comp = shulker.get(DataComponentTypes.CONTAINER);
            if (comp != null) {
                comp.stream().forEach(slots::add);
            }
        }

        int slotSize = 18;
        int pad = paddingSetting.getIntValue();
        int boxW = 9 * slotSize + pad * 2;
        int boxH = 14 + 3 * slotSize + pad * 2;

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

        context.drawTextWithShadow(client.textRenderer, client.textRenderer.trimToWidth(title, boxW - pad * 2),
                x + pad, y + pad, textColorSetting.getArgb());

        int gridY = y + pad + 13;
        for (int i = 0; i < 27; i++) {
            int row = i / 9;
            int col = i % 9;
            int sx = x + pad + col * slotSize;
            int sy = gridY + row * slotSize;
            context.fill(sx, sy, sx + slotSize - 1, sy + slotSize - 1, 0x16FFFFFF);

            if (i < slots.size()) {
                ItemStack s = slots.get(i);
                if (!s.isEmpty()) {
                    context.drawItem(s, sx + 1, sy + 1);
                    context.drawStackOverlay(client.textRenderer, s, sx + 1, sy + 1);
                }
            }
        }
    }
}
