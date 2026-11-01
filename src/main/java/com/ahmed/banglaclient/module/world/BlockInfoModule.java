package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * Module 79: Block Information
 * Displays the block currently targeted by the player's crosshair, including its icon, localized name,
 * registry ID, and relevant block state properties (e.g., Redstone Power, Crop Age, Honey Level).
 */
public final class BlockInfoModule extends HudModule {
    private final BooleanSetting showCoordinatesSetting;
    private final BooleanSetting showBlockStatePropsSetting;

    public BlockInfoModule() {
        super(
                "block_info",
                "Block Information",
                "Displays the block targeted by the player's crosshair and its state metadata.",
                ModuleCategory.WORLD,
                300.0f,
                340.0f,
                148.0f,
                30.0f,
                false
        );
        this.showCoordinatesSetting = addSetting(new BooleanSetting(
                "show_coords", "Show Block Coordinates", "Include the targeted block's X, Y, Z position", true
        ));
        this.showBlockStatePropsSetting = addSetting(new BooleanSetting(
                "show_props", "Show State Properties", "Include Redstone power, crop maturity, or waterlogged state", true
        ));
        addAliases("waila", "jade", "whatisthat", "targetedblock");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        BlockState state = null;
        BlockPos pos = null;

        if (client.world != null && client.crosshairTarget instanceof BlockHitResult bhr && bhr.getType() == HitResult.Type.BLOCK) {
            pos = bhr.getBlockPos();
            state = client.world.getBlockState(pos);
            if (state.isAir()) {
                state = null;
            }
        }

        if (state == null && !preview) {
            return;
        }

        String title = state != null ? state.getBlock().getName().getString() : "Grass Block";
        ItemStack icon = state != null ? new ItemStack(state.getBlock().asItem()) : new ItemStack(Items.GRASS_BLOCK);
        String subtitle = state != null ? Registries.BLOCK.getId(state.getBlock()).toString() : "minecraft:grass_block";

        if (state != null && pos != null) {
            StringBuilder details = new StringBuilder();
            if (showCoordinatesSetting.isEnabled()) {
                details.append(String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ()));
            }
            if (showBlockStatePropsSetting.isEnabled()) {
                if (state.contains(Properties.POWER)) {
                    details.append(" • Power: ").append(state.get(Properties.POWER));
                }
                if (state.contains(Properties.AGE_7)) {
                    int age = state.get(Properties.AGE_7);
                    details.append(" • Growth: ").append(Math.round((age / 7.0f) * 100.0f)).append("%");
                }
                if (state.contains(Properties.HONEY_LEVEL)) {
                    details.append(" • Honey: ").append(state.get(Properties.HONEY_LEVEL)).append("/5");
                }
            }
            if (!details.isEmpty()) {
                subtitle = details.toString().trim();
            }
        }

        int pad = paddingSetting.getIntValue();
        Theme theme = GlassRenderer.getTheme();

        int textW = Math.max(client.textRenderer.getWidth(title), client.textRenderer.getWidth(subtitle));
        int boxW = 22 + textW + pad * 2;
        int boxH = 22 + pad * 2;

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

        if (!icon.isEmpty()) {
            context.drawItem(icon, x + pad, y + (boxH - 16) / 2);
        }
        context.drawTextWithShadow(client.textRenderer, title, x + pad + 20, y + pad + 1, textColorSetting.getArgb());
        context.drawTextWithShadow(client.textRenderer, subtitle, x + pad + 20, y + pad + 12, theme.getTextSecondary());
    }
}
