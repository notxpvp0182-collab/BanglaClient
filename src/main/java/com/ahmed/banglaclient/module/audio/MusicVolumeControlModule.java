package com.ahmed.banglaclient.module.audio;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import net.minecraft.sound.SoundCategory;

/**
 * Module 98: Music Volume Control
 * Provides quick background music volume control and optional combat-ducking (lowering music during combat).
 */
public final class MusicVolumeControlModule extends Module {
    private final NumberSetting musicVolumePercentSetting;
    private final BooleanSetting duckDuringCombatSetting;

    public MusicVolumeControlModule() {
        super(
                "music_volume_control",
                "Music Volume Control",
                "Provides quick background music volume control and optional combat audio ducking.",
                ModuleCategory.AUDIO
        );
        this.musicVolumePercentSetting = addSetting(new NumberSetting(
                "music_vol", "Music Volume", "Target volume level for background music",
                40.0, 0.0, 100.0, 5.0, "%"
        ));
        this.duckDuringCombatSetting = addSetting(new BooleanSetting(
                "combat_duck", "Duck Music When Hurt", "Temporarily lower music volume by 50% when taking damage", true
        ));
        addAliases("musicvolume", "bgm", "musicduck");
    }

    @Override
    public void onTick() {
        if (client == null || client.options == null) {
            return;
        }
        double target = musicVolumePercentSetting.getValue() / 100.0;
        if (duckDuringCombatSetting.isEnabled() && client.player != null && client.player.hurtTime > 0) {
            target *= 0.5;
        }

        var musicOpt = client.options.getSoundVolumeOption(SoundCategory.MUSIC);
        if (musicOpt != null && Math.abs(musicOpt.getValue() - target) > 0.01) {
            musicOpt.setValue(target);
        }
    }
}
