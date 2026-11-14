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
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * Unified Block Information + Block Breaking Progress & Hardness Module.
 */
public final class BlockInfoModule extends HudModule {
    private final BooleanSetting showCoordinatesSetting;
    private final BooleanSetting showHardnessSetting;
    private final BooleanSetting showMiningProgressSetting;
    private final BooleanSetting showBlockStatePropsSetting;

    private BlockPos activeMiningPos = null;
    private float accumulatedProgress = 0.0f;

    public BlockInfoModule() {
        super("block_info", "Block Information", "Displays targeted block info, hardness, and live mining break progress.",
                ModuleCategory.WORLD, 300.0f, 340.0f, 148.0f, 32.0f, false);
        this.showCoordinatesSetting = addSetting(new BooleanSetting("show_coords", "Show Block Coordinates", "Include X, Y, Z position", true));
        this.showHardnessSetting = addSetting(new BooleanSetting("show_hardness", "Show Block Hardness", "Include block hardness rating", true));
        this.showMiningProgressSetting = addSetting(new BooleanSetting("show_mining", "Show Mining Progress Bar", "Show live break % and progress bar while mining", true));
        this.showBlockStatePropsSetting = addSetting(new BooleanSetting("show_props", "Show State Properties", "Include Redstone power or crop growth", true));
        addAliases("waila", "jade", "blockbreaking", "blockspeed", "mininginfo");
    }

    @Override
    public void onTick() {
        if (client.interactionManager == null || !client.interactionManager.isBreakingBlock()
                || client.world == null || client.player == null
                || !(client.crosshairTarget instanceof BlockHitResult bhr)
                || bhr.getType() != HitResult.Type.BLOCK) {
            activeMiningPos = null;
            accumulatedProgress = 0.0f;
            return;
        }
        BlockPos pos = bhr.getBlockPos();
        if (!pos.equals(activeMiningPos)) {
            activeMiningPos = pos.toImmutable();
            accumulatedProgress = 0.0f;
        }
        BlockState state = client.world.getBlockState(pos);
        float deltaPerTick = state.calcBlockBreakingDelta(client.player, client.world, pos);
        if (deltaPerTick > 0.0f) {
            accumulatedProgress = Math.min(1.0f, accumulatedProgress + deltaPerTick);
        }
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        BlockState state = null;
        BlockPos pos = null;

        if (client.world != null && client.crosshairTarget instanceof BlockHitResult bhr && bhr.getType() == HitResult.Type.BLOCK) {
            pos = bhr.getBlockPos();
            state = client.world.getBlockState(pos);
            if (state.isAir()) state = null;
        }

        if (state == null && !preview) return;

        String title = state != null ? state.getBlock().getName().getString() : "Grass Block";
        ItemStack icon = state != null ? new ItemStack(state.getBlock().asItem()) : new ItemStack(Items.GRASS_BLOCK);
        StringBuilder details = new StringBuilder();

        if (state != null && pos != null) {
            if (showCoordinatesSetting.isEnabled()) {
                details.append(String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ()));
            }
            if (showHardnessSetting.isEnabled() && client.world != null) {
                float h = state.getHardness(client.world, pos);
                if (h >= 0.0f) details.append(String.format(" H:%.1f", h));
            }
            if (showBlockStatePropsSetting.isEnabled()) {
                if (state.contains(Properties.POWER)) details.append(" Pwr:").append(state.get(Properties.POWER));
                if (state.contains(Properties.AGE_7)) details.append(" ").append(Math.round((state.get(Properties.AGE_7) / 7.0f) * 100.0f)).append("%");
            }
        } else {
            details.append("[128, 64, -256] H:0.6");
        }

        boolean isMining = showMiningProgressSetting.isEnabled() && ((client.interactionManager != null && client.interactionManager.isBreakingBlock()) || preview);
        if (isMining && accumulatedProgress > 0.01f) {
            details.append(String.format(" • %d%%", Math.round(accumulatedProgress * 100.0f)));
        }

        String subtitle = details.toString().trim();
        int pad = paddingSetting.getIntValue();
        Theme theme = GlassRenderer.getTheme();

        int textW = Math.max(client.textRenderer.getWidth(title), client.textRenderer.getWidth(subtitle));
        int barExtra = (isMining && accumulatedProgress > 0.01f) ? 5 : 0;
        int boxW = 22 + textW + pad * 2;
        int boxH = 22 + barExtra + pad * 2;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(context, x, y, boxW, boxH, backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled());
        }

        if (!icon.isEmpty()) {
            context.drawItem(icon, x + pad, y + pad + 3);
        }
        context.drawTextWithShadow(client.textRenderer, title, x + pad + 20, y + pad + 1, textColorSetting.getArgb());
        context.drawTextWithShadow(client.textRenderer, subtitle, x + pad + 20, y + pad + 12, theme.getTextSecondary());

        if (barExtra > 0) {
            int bx = x + pad;
            int by = y + boxH - pad - 2;
            int bw = boxW - pad * 2;
            context.fill(bx, by, bx + bw, by + 2, 0x33FFFFFF);
            context.fill(bx, by, bx + Math.round(bw * accumulatedProgress), by + 2, 0xFFFFFFFF);
        }
    }
}
