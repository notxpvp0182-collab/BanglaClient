package com.ahmed.banglaclient.module.audio;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundCategory;

/**
 * Module 92: Sound Volume HUD
 * Displays the current Master sound volume percentage and optional subtitles status on the HUD.
 */
public final class SoundVolumeHudModule extends HudModule {
    private final BooleanSetting showSubtitlesStateSetting;

    public SoundVolumeHudModule() {
        super(
                "sound_volume_hud",
                "Sound Volume HUD",
                "Displays current Master audio volume percentage and subtitle status.",
                ModuleCategory.AUDIO,
                440.0f,
                10.0f,
                108.0f,
                20.0f,
                false
        );
        this.showSubtitlesStateSetting = addSetting(new BooleanSetting(
                "show_subtitles", "Show Subtitles Status", "Append whether Closed Captions / Subtitles are enabled", false
        ));
        addAliases("volumehud", "mastervolume", "audiohud");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int masterPct = 100;
        boolean subtitles = false;

        if (client != null && client.options != null) {
            float rawVol = client.options.getSoundVolume(SoundCategory.MASTER);
            masterPct = Math.round(rawVol * 100.0f);
            subtitles = Boolean.TRUE.equals(client.options.getShowSubtitles().getValue());
        }

        StringBuilder sb = new StringBuilder();
        if (masterPct <= 0) {
            sb.append("Audio: MUTED (0%)");
        } else {
            sb.append("Volume: ").append(masterPct).append("%");
        }
        if (showSubtitlesStateSetting.isEnabled()) {
            sb.append(subtitles ? " [CC: ON]" : " [CC: OFF]");
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

        int color = masterPct <= 0 ? ThemeTokens.STATUS_ERROR : textColorSetting.getArgb();
        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, color);
    }
}
