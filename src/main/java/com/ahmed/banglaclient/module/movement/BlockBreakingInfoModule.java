package com.ahmed.banglaclient.module.movement;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * Module 62: Block Breaking Info
 * Displays information about the block currently being mined (block name, hardness, and tool effectiveness).
 */
public final class BlockBreakingInfoModule extends HudModule {
    private final BooleanSetting showHardnessSetting;

    public BlockBreakingInfoModule() {
        super(
                "block_breaking_info",
                "Block Breaking Info",
                "Displays information about the block currently being mined.",
                ModuleCategory.MOVEMENT,
                10.0f,
                292.0f,
                132.0f,
                22.0f,
                false
        );
        this.showHardnessSetting = addSetting(new BooleanSetting(
                "show_hardness", "Show Block Hardness", "Include the targeted block's hardness rating", true
        ));
        addAliases("mininginfo", "breaking", "minehud");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        boolean isMining = client.interactionManager != null && client.interactionManager.isBreakingBlock();
        if (!isMining && !preview) {
            return;
        }

        String blockName = "Stone";
        float hardness = 1.5f;
        boolean suitableTool = true;

        if (client.world != null && client.player != null && client.crosshairTarget instanceof BlockHitResult bhr && bhr.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = bhr.getBlockPos();
            BlockState state = client.world.getBlockState(pos);
            blockName = state.getBlock().getName().getString();
            hardness = state.getHardness(client.world, pos);
            suitableTool = client.player.canHarvest(state);
        }

        StringBuilder sb = new StringBuilder("Mining: ").append(blockName);
        if (showHardnessSetting.isEnabled() && hardness >= 0.0f) {
            sb.append(String.format(" [H: %.1f]", hardness));
        }
        if (!suitableTool) {
            sb.append(" (Slow Tool)");
        }

        String text = sb.toString();
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

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, textColorSetting.getArgb());
    }
}
