package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Module 47: Container Preview
 * Provides a legitimate preview of container contents when data is available on the client:
 * 1. Held item carrying `DataComponentTypes.CONTAINER` (e.g., Shulker Box, Bundle, decorated container item).
 * 2. World Chest/Barrel that the player has legitimately opened during the current session (cached locally).
 * 3. Clearly reports "Server-Side (Open Once to Cache)" for unopened multiplayer containers.
 */
public final class ContainerPreviewModule extends HudModule {
    private static final int MAX_CACHED_CONTAINERS = 64;

    private final Map<BlockPos, List<ItemStack>> openedContainerCache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<BlockPos, List<ItemStack>> eldest) {
            return size() > MAX_CACHED_CONTAINERS;
        }
    };
    private BlockPos lastTargetedContainerPos = null;

    public ContainerPreviewModule() {
        super(
                "container_preview",
                "Container Preview",
                "Displays container contents for held container items or previously opened world chests.",
                ModuleCategory.INVENTORY,
                415.0f,
                248.0f,
                168.0f,
                68.0f,
                false
        );
        addAliases("chestpreview", "container", "barrelpreview");
    }

    @Override
    public void onTick() {
        if (client.world == null || client.player == null) {
            openedContainerCache.clear();
            return;
        }

        // Track targeted container block position
        if (client.crosshairTarget instanceof BlockHitResult bhr && bhr.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = bhr.getBlockPos();
            BlockState state = client.world.getBlockState(pos);
            if (isContainerBlock(state)) {
                lastTargetedContainerPos = pos.toImmutable();
            }
        }

        // If the player currently has a container screen open, snapshot its top 27 slots legitimately
        if (client.currentScreen instanceof GenericContainerScreen screen && lastTargetedContainerPos != null) {
            var handler = screen.getScreenHandler();
            int containerSlots = Math.min(27, handler.getRows() * 9);
            List<ItemStack> snapshot = new ArrayList<>(27);
            for (int i = 0; i < containerSlots && i < handler.slots.size(); i++) {
                Slot slot = handler.slots.get(i);
                snapshot.add(slot.getStack().copy());
            }
            openedContainerCache.put(lastTargetedContainerPos, snapshot);
        }
    }

    private static boolean isContainerBlock(BlockState state) {
        if (state == null) {
            return false;
        }
        var block = state.getBlock();
        return block instanceof ChestBlock
                || block instanceof BarrelBlock
                || block instanceof ShulkerBoxBlock
                || block instanceof EnderChestBlock;
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        List<ItemStack> itemsToRender = null;
        String headerTitle = "Container Preview";
        boolean unopenedMultiplayerState = false;

        // 1. Check held item for DataComponentTypes.CONTAINER
        if (client.player != null) {
            ItemStack main = client.player.getMainHandStack();
            ContainerComponent containerComp = main.get(DataComponentTypes.CONTAINER);
            if (containerComp != null) {
                headerTitle = main.getName().getString();
                itemsToRender = new ArrayList<>(27);
                for (ItemStack s : containerComp.iterateNonEmpty()) {
                    itemsToRender.add(s);
                }
            }
        }

        // 2. Otherwise check targeted world container block
        if (itemsToRender == null && client.world != null && client.crosshairTarget instanceof BlockHitResult bhr && bhr.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = bhr.getBlockPos();
            BlockState state = client.world.getBlockState(pos);
            if (isContainerBlock(state)) {
                headerTitle = state.getBlock().getName().getString();
                if (openedContainerCache.containsKey(pos)) {
                    itemsToRender = openedContainerCache.get(pos);
                } else {
                    unopenedMultiplayerState = true;
                }
            }
        }

        if (itemsToRender == null && !unopenedMultiplayerState && !preview) {
            return;
        }

        int pad = paddingSetting.getIntValue();
        int x = Math.round(getPosX());
        int y = Math.round(getPosY());
        Theme theme = GlassRenderer.getTheme();

        if (unopenedMultiplayerState) {
            String msg = "Unopened (Open once to cache contents)";
            int boxW = Math.max(client.textRenderer.getWidth(headerTitle), client.textRenderer.getWidth(msg)) + pad * 2;
            int boxH = 26 + pad * 2;
            setWidth(boxW);
            setHeight(boxH);
            if (showBackgroundSetting.isEnabled()) {
                GlassRenderer.drawCustomGlassPanel(context, x, y, boxW, boxH, backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled());
            }
            context.drawTextWithShadow(client.textRenderer, headerTitle, x + pad, y + pad + 1, textColorSetting.getArgb());
            context.drawTextWithShadow(client.textRenderer, msg, x + pad, y + pad + 13, theme.getTextSecondary());
            return;
        }

        int slotSize = 18;
        int boxW = 9 * slotSize + pad * 2;
        int boxH = 14 + 3 * slotSize + pad * 2;
        setWidth(boxW);
        setHeight(boxH);

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        context.drawTextWithShadow(client.textRenderer, client.textRenderer.trimToWidth(headerTitle, boxW - pad * 2),
                x + pad, y + pad, textColorSetting.getArgb());

        int gridY = y + pad + 13;
        for (int i = 0; i < 27; i++) {
            int row = i / 9;
            int col = i % 9;
            int sx = x + pad + col * slotSize;
            int sy = gridY + row * slotSize;
            context.fill(sx, sy, sx + slotSize - 1, sy + slotSize - 1, 0x16FFFFFF);

            if (itemsToRender != null && i < itemsToRender.size()) {
                ItemStack stack = itemsToRender.get(i);
                if (!stack.isEmpty()) {
                    context.drawItem(stack, sx + 1, sy + 1);
                    context.drawStackOverlay(client.textRenderer, stack, sx + 1, sy + 1);
                }
            }
        }
    }
}
