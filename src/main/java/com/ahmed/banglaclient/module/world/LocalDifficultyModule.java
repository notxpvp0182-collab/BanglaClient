package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.world.LocalDifficulty;

/**
 * Module 73: Local Difficulty
 * Displays the current chunk's regional/local difficulty and optional clamped difficulty factor (`0.00–1.00`).
 */
public final class LocalDifficultyModule extends HudModule {
    private final BooleanSetting showClampedFactorSetting;

    public LocalDifficultyModule() {
        super(
                "local_difficulty",
                "Local Difficulty",
                "Displays current regional chunk difficulty and clamped difficulty factor.",
                ModuleCategory.WORLD,
                160.0f,
                350.0f,
                116.0f,
                20.0f,
                false
        );
        this.showClampedFactorSetting = addSetting(new BooleanSetting(
                "show_clamped", "Show Clamped Factor", "Append clamped difficulty multiplier (0.00 to 1.00)", true
        ));
        addAliases("regionaldifficulty", "chunkdifficulty");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        float localDiff = 2.25f;
        float clampedDiff = 0.12f;

        if (client.world != null && client.player != null) {
            LocalDifficulty ld = new LocalDifficulty(client.world.getDifficulty(), client.world.getTimeOfDay(), 0L, 1.0f);
            localDiff = ld.getLocalDifficulty();
            clampedDiff = ld.getClampedLocalDifficulty();
        }

        String text = showClampedFactorSetting.isEnabled()
                ? String.format("Local Diff: %.2f (%.2f)", localDiff, clampedDiff)
                : String.format("Local Diff: %.2f", localDiff);

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
