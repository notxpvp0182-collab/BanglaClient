package com.ahmed.banglaclient.module.audio;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundCategory;

/**
 * Module 94: Music Display
 * Displays available current background music contextual category/identifier and music volume status.
 */
public final class MusicDisplayModule extends HudModule {
    private final BooleanSetting hideWhenMusicMutedSetting;

    public MusicDisplayModule() {
        super(
                "music_display",
                "Music Display",
                "Displays current background music context and playback state.",
                ModuleCategory.AUDIO,
                440.0f,
                34.0f,
                132.0f,
                20.0f,
                false
        );
        this.hideWhenMusicMutedSetting = addSetting(new BooleanSetting(
                "hide_when_muted", "Hide When Music Volume is 0%", "Hide the widget when Music category volume is muted", false
        ));
        addAliases("music", "nowplaying", "song", "soundtrack");
    }

    public String resolveCurrentMusicContextName() {
        if (client == null) {
            return "Menu Theme";
        }
        float musicVol = client.options != null ? client.options.getSoundVolume(SoundCategory.MUSIC) : 1.0f;
        if (musicVol <= 0.001f) {
            return "Muted (0%)";
        }
        MusicSound musicType = client.getMusicInstance() != null ? client.getMusicInstance().music() : null;
        if (musicType == null || musicType.getSound() == null) {
            return "Ambient Soundtrack";
        }
        var keyOpt = musicType.getSound().getKey();
        if (keyOpt.isPresent()) {
            String rawPath = keyOpt.get().getValue().getPath();
            return formatSoundPath(rawPath);
        }
        return "Minecraft Soundtrack";
    }

    private static String formatSoundPath(String path) {
        if (path == null || path.isEmpty()) {
            return "Soundtrack";
        }
        String cleaned = path.replace("music.", "").replace('.', ' ').replace('_', ' ');
        String[] words = cleaned.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) {
                    sb.append(w.substring(1));
                }
                if (i < words.length - 1) {
                    sb.append(' ');
                }
            }
        }
        return sb.toString();
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        if (!preview && hideWhenMusicMutedSetting.isEnabled() && client.options != null) {
            if (client.options.getSoundVolume(SoundCategory.MUSIC) <= 0.001f) {
                return;
            }
        }

        String trackContext = resolveCurrentMusicContextName();
        String text = "♫ Music: " + trackContext;

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
