package com.ahmed.banglaclient.module.movement;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * Module 63: Block Speed Display
 * Displays the actual measured or estimated vanilla block-breaking progress percentage and remaining seconds.
 * Strictly read-only: never modifies block-breaking speed.
 */
public final class BlockSpeedDisplayModule extends HudModule {
    private final BooleanSetting showProgressBarSetting;

    private BlockPos activeMiningPos = null;
    private float accumulatedProgress = 0.0f;

    public BlockSpeedDisplayModule() {
        super(
                "block_speed_display",
                "Block Speed Display",
                "Displays measured vanilla block-breaking progress percentage and estimated time remaining.",
                ModuleCategory.MOVEMENT,
                10.0f,
                318.0f,
                122.0f,
                26.0f,
                false
        );
        this.showProgressBarSetting = addSetting(new BooleanSetting(
                "show_bar", "Show Progress Bar", "Render a visual mining progress bar below the percentage", true
        ));
        addAliases("miningspeed", "breakprogress", "breaktime");
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
        boolean isMining = client.interactionManager != null && client.interactionManager.isBreakingBlock();
        if (!isMining && !preview) {
            return;
        }

        float progress = preview && !isMining ? 0.64f : accumulatedProgress;
        float remainingSec = 0.45f;

        if (isMining && activeMiningPos != null && client.world != null && client.player != null) {
            BlockState state = client.world.getBlockState(activeMiningPos);
            float deltaPerTick = state.calcBlockBreakingDelta(client.player, client.world, activeMiningPos);
            if (deltaPerTick > 0.0f) {
                float ticksLeft = Math.max(0.0f, (1.0f - progress) / deltaPerTick);
                remainingSec = ticksLeft / 20.0f;
            }
        }

        int pct = Math.min(100, Math.round(progress * 100.0f));
        String text = String.format("Break: %d%% (%.1fs)", pct, remainingSec);

        int pad = paddingSetting.getIntValue();
        int barExtra = showProgressBarSetting.isEnabled() ? 6 : 0;
        int boxW = Math.max(106, client.textRenderer.getWidth(text) + pad * 2);
        int boxH = client.textRenderer.fontHeight + pad * 2 + barExtra;

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

        if (showProgressBarSetting.isEnabled()) {
            int barX = x + pad;
            int barY = y + boxH - pad - 3;
            int barW = boxW - pad * 2;
            context.fill(barX, barY, barX + barW, barY + 3, 0x44FFFFFF);

            int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                    ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                    : ThemeTokens.DEFAULT_ACCENT_PRIMARY;
            int fillW = Math.round(barW * Math.min(1.0f, progress));
            if (fillW > 0) {
                context.fill(barX, barY, barX + fillW, barY + 3, accent);
            }
        }
    }
}
