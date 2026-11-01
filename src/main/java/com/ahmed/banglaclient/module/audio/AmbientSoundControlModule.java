package com.ahmed.banglaclient.module.audio;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import net.minecraft.sound.SoundCategory;

/**
 * Module 97: Ambient Sound Control
 * Customizes ambient environment/cave sound categories and neutral creature volume.
 */
public final class AmbientSoundControlModule extends Module {
    private final NumberSetting ambientCategoryLevelSetting;
    private final NumberSetting neutralCreatureLevelSetting;
    private final BooleanSetting muteCaveSpooksSetting;

    private double previousAmbientVolume = 1.0;

    public AmbientSoundControlModule() {
        super(
                "ambient_sound_control",
                "Ambient Sound Control",
                "Customizes cave/environment ambient loops and passive creature sound levels.",
                ModuleCategory.AUDIO
        );
        this.ambientCategoryLevelSetting = addSetting(new NumberSetting(
                "ambient_level", "Ambient / Cave Volume", "Volume percentage for SoundCategory.AMBIENT",
                25.0, 0.0, 100.0, 5.0, "%"
        ));
        this.neutralCreatureLevelSetting = addSetting(new NumberSetting(
                "neutral_level", "Passive Mob Volume", "Volume percentage for friendly/neutral creatures",
                75.0, 0.0, 100.0, 5.0, "%"
        ));
        this.muteCaveSpooksSetting = addSetting(new BooleanSetting(
                "mute_cave_chimes", "Silence Cave Eerie Sounds", "Force Ambient category to 0% to silence cave noises", false
        ));
        addAliases("ambience", "cavesounds", "ambientvolume");
    }

    @Override
    protected void onEnable() {
        if (client != null && client.options != null) {
            previousAmbientVolume = client.options.getSoundVolume(SoundCategory.AMBIENT);
        }
    }

    @Override
    protected void onDisable() {
        if (client != null && client.options != null) {
            var opt = client.options.getSoundVolumeOption(SoundCategory.AMBIENT);
            if (opt != null) {
                opt.setValue(previousAmbientVolume);
            }
        }
    }

    @Override
    public void onTick() {
        if (client == null || client.options == null) {
            return;
        }
        double targetAmbient = muteCaveSpooksSetting.isEnabled()
                ? 0.0
                : (ambientCategoryLevelSetting.getValue() / 100.0);
        double targetNeutral = neutralCreatureLevelSetting.getValue() / 100.0;

        var ambOpt = client.options.getSoundVolumeOption(SoundCategory.AMBIENT);
        if (ambOpt != null && Math.abs(ambOpt.getValue() - targetAmbient) > 0.01) {
            ambOpt.setValue(targetAmbient);
        }

        var neuOpt = client.options.getSoundVolumeOption(SoundCategory.NEUTRAL);
        if (neuOpt != null && Math.abs(neuOpt.getValue() - targetNeutral) > 0.01) {
            neuOpt.setValue(targetNeutral);
        }
    }
}
