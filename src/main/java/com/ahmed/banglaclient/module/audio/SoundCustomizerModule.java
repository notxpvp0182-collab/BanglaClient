package com.ahmed.banglaclient.module.audio;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import net.minecraft.sound.SoundCategory;

/**
 * Module 93: Sound Customizer
 * Provides supported individual sound-category volume controls (Weather, Blocks, Hostile Creatures,
 * Friendly Creatures, Players, and Records/Jukeboxes) directly from the BANGLA CLIENT interface.
 */
public final class SoundCustomizerModule extends Module {
    private final NumberSetting weatherVolumeSetting;
    private final NumberSetting blocksVolumeSetting;
    private final NumberSetting hostileVolumeSetting;
    private final NumberSetting playersVolumeSetting;
    private final NumberSetting recordsVolumeSetting;

    public SoundCustomizerModule() {
        super(
                "sound_customizer",
                "Sound Customizer",
                "Provides fine-grained individual sound-category volume controls.",
                ModuleCategory.AUDIO
        );
        this.weatherVolumeSetting = addSetting(new NumberSetting(
                "weather_vol", "Weather Volume", "Volume level for rain and thunder",
                50.0, 0.0, 100.0, 5.0, "%"
        ));
        this.blocksVolumeSetting = addSetting(new NumberSetting(
                "blocks_vol", "Blocks Volume", "Volume level for block placement, mining, and pistons",
                80.0, 0.0, 100.0, 5.0, "%"
        ));
        this.hostileVolumeSetting = addSetting(new NumberSetting(
                "hostile_vol", "Hostile Mobs Volume", "Volume level for hostile monsters",
                100.0, 0.0, 100.0, 5.0, "%"
        ));
        this.playersVolumeSetting = addSetting(new NumberSetting(
                "players_vol", "Players Volume", "Volume level for player footsteps and combat sounds",
                100.0, 0.0, 100.0, 5.0, "%"
        ));
        this.recordsVolumeSetting = addSetting(new NumberSetting(
                "records_vol", "Jukebox / Note Blocks", "Volume level for music discs and note blocks",
                100.0, 0.0, 100.0, 5.0, "%"
        ));
        addAliases("soundmixer", "audiomixer", "volumesliders");
    }

    @Override
    public void onTick() {
        if (client == null || client.options == null) {
            return;
        }
        syncCategory(SoundCategory.WEATHER, weatherVolumeSetting.getValue() / 100.0);
        syncCategory(SoundCategory.BLOCKS, blocksVolumeSetting.getValue() / 100.0);
        syncCategory(SoundCategory.HOSTILE, hostileVolumeSetting.getValue() / 100.0);
        syncCategory(SoundCategory.PLAYERS, playersVolumeSetting.getValue() / 100.0);
        syncCategory(SoundCategory.RECORDS, recordsVolumeSetting.getValue() / 100.0);
    }

    private void syncCategory(SoundCategory category, double target) {
        var opt = client.options.getSoundVolumeOption(category);
        if (opt != null && Math.abs(opt.getValue() - target) > 0.01) {
            opt.setValue(target);
        }
    }
}
