package com.ahmed.banglaclient.module.audio;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundCategory;

/**
 * Module 99: Sound Category HUD
 * Displays a compact overview card of key audio category volume percentages (Master, Music, Hostile, Players, Blocks).
 */
public final class SoundCategoryHudModule extends HudModule {

    public SoundCategoryHudModule() {
        super(
                "sound_category_hud",
                "Sound Category HUD",
                "Displays a compact overview of Master, Music, Hostile, Players, and Blocks audio levels.",
                ModuleCategory.AUDIO,
                440.0f,
                60.0f,
                122.0f,
                64.0f,
                false
        );
        addAliases("audiocategories", "soundhud", "mixerhud");
    }

    private int getCatPct(SoundCategory cat) {
        if (client == null || client.options == null) {
            return 100;
        }
        return Math.round(client.options.getSoundVolume(cat) * 100.0f);
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        String[] labels = {
                "Master: " + getCatPct(SoundCategory.MASTER) + "%",
                "Music: " + getCatPct(SoundCategory.MUSIC) + "%",
                "Hostile: " + getCatPct(SoundCategory.HOSTILE) + "%",
                "Players: " + getCatPct(SoundCategory.PLAYERS) + "%",
                "Blocks: " + getCatPct(SoundCategory.BLOCKS) + "%"
        };

        int pad = paddingSetting.getIntValue();
        int maxW = 92;
        for (String line : labels) {
            maxW = Math.max(maxW, client.textRenderer.getWidth(line));
        }

        int boxW = maxW + pad * 2;
        int boxH = labels.length * 11 + pad * 2;
        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());
        Theme theme = GlassRenderer.getTheme();

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        int drawY = y + pad + 1;
        for (int i = 0; i < labels.length; i++) {
            int color = (i == 0) ? textColorSetting.getArgb() : theme.getTextSecondary();
            context.drawTextWithShadow(client.textRenderer, labels[i], x + pad, drawY, color);
            drawY += 11;
        }
    }
}
