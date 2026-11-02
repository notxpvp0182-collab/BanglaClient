package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * "Anchor Optimizer" module.
 * Optimizes client-side Respawn Anchor charge-state visibility and interaction feedback
 * while preserving normal server-side rules.
 */
public final class AnchorOptimizerModule extends HudModule {
    private final BooleanSetting showChargeBadgeSetting;

    public AnchorOptimizerModule() {
        super(
                "anchor_optimizer",
                "Anchor Optimizer",
                "Optimizes Respawn Anchor charge-level visibility and client interaction feedback.",
                ModuleCategory.COMBAT,
                300.0f,
                368.0f,
                112.0f,
                20.0f,
                false
        );
        this.showChargeBadgeSetting = addSetting(new BooleanSetting(
                "show_charge_hud", "Show Targeted Anchor Charges", "Display live charge count (0/4 to 4/4) when looking at a Respawn Anchor", true
        ));
        addAliases("anchor", "respawnanchor", "anchoropt");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        if (!showChargeBadgeSetting.isEnabled() && !preview) return;

        int charges = -1;
        if (client != null && client.world != null && client.crosshairTarget instanceof BlockHitResult bhr && bhr.getType() == HitResult.Type.BLOCK) {
            BlockState state = client.world.getBlockState(bhr.getBlockPos());
            if (state.isOf(Blocks.RESPAWN_ANCHOR) && state.contains(RespawnAnchorBlock.CHARGES)) {
                charges = state.get(RespawnAnchorBlock.CHARGES);
            }
        } else if (preview) {
            charges = 3;
        }

        if (charges < 0) return;

        String text = "Anchor Charges: " + charges + " / 4";
        int pad = paddingSetting.getIntValue();
        int boxW = client.textRenderer.getWidth(text) + pad * 2;
        int boxH = client.textRenderer.fontHeight + pad * 2;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(context, x, y, boxW, boxH, backgroundColorSetting.getArgb(), false, true, showBorderSetting.isEnabled());
        }
        int col = charges == 4 ? ThemeTokens.STATUS_ERROR : (charges > 0 ? ThemeTokens.STATUS_WARNING : textColorSetting.getArgb());
        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, col);
    }
}
