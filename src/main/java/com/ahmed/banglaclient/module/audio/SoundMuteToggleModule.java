package com.ahmed.banglaclient.module.audio;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import net.minecraft.sound.SoundCategory;
import org.lwjgl.glfw.GLFW;

/**
 * Module 96: Sound Mute Toggle
 * Provides quick one-key sound muting (and optional automatic mute when the Minecraft window is unfocused),
 * restoring the user's previous Master volume cleanly when unmuted.
 */
public final class SoundMuteToggleModule extends Module {
    private final BooleanSetting muteWhenUnfocusedSetting;

    private double savedMasterVolume = 1.0;
    private boolean currentlyMutedByUnfocus = false;

    public SoundMuteToggleModule() {
        super(
                "sound_mute_toggle",
                "Sound Mute Toggle",
                "Provides instant master audio muting or automatic muting when the game window is unfocused.",
                ModuleCategory.AUDIO,
                GLFW.GLFW_KEY_M,
                false
        );
        this.muteWhenUnfocusedSetting = addSetting(new BooleanSetting(
                "unfocus_only", "Only Mute When Window Unfocused",
                "Automatically mute audio when Alt-Tabbed instead of muting while playing", false
        ));
        addAliases("mute", "audiomute", "unfocusmute", "silence");
    }

    @Override
    protected void onEnable() {
        if (client == null || client.options == null) {
            return;
        }
        var masterOpt = client.options.getSoundVolumeOption(SoundCategory.MASTER);
        if (masterOpt != null && masterOpt.getValue() > 0.01) {
            savedMasterVolume = masterOpt.getValue();
        }
        if (!muteWhenUnfocusedSetting.isEnabled() && masterOpt != null) {
            masterOpt.setValue(0.0);
        }
    }

    @Override
    protected void onDisable() {
        restoreSavedVolume();
        currentlyMutedByUnfocus = false;
    }

    @Override
    public void onTick() {
        if (client == null || client.options == null) {
            return;
        }
        var masterOpt = client.options.getSoundVolumeOption(SoundCategory.MASTER);
        if (masterOpt == null) {
            return;
        }

        if (muteWhenUnfocusedSetting.isEnabled()) {
            boolean focused = client.isWindowFocused();
            if (!focused && !currentlyMutedByUnfocus) {
                if (masterOpt.getValue() > 0.01) {
                    savedMasterVolume = masterOpt.getValue();
                }
                masterOpt.setValue(0.0);
                currentlyMutedByUnfocus = true;
            } else if (focused && currentlyMutedByUnfocus) {
                restoreSavedVolume();
                currentlyMutedByUnfocus = false;
            }
        } else {
            if (masterOpt.getValue() > 0.0) {
                masterOpt.setValue(0.0);
            }
        }
    }

    private void restoreSavedVolume() {
        if (client != null && client.options != null) {
            var masterOpt = client.options.getSoundVolumeOption(SoundCategory.MASTER);
            if (masterOpt != null) {
                masterOpt.setValue(Math.max(0.05, savedMasterVolume));
            }
        }
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
            BanglaClient.getInstance().getNotificationManager().pushInfo(
                    "Audio Restored",
                    "Master volume restored to " + Math.round(savedMasterVolume * 100.0) + "%"
            );
        }
    }
}
