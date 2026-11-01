package com.ahmed.banglaclient.module.audio;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import net.minecraft.sound.SoundCategory;

/**
 * Module 95: Music Notifications
 * Pushes a sleek Liquid Glass toast notification whenever the background music context changes.
 */
public final class MusicNotificationsModule extends Module {
    private String lastAnnouncedTrack = "";

    public MusicNotificationsModule() {
        super(
                "music_notifications",
                "Music Notifications",
                "Notifies the player when the background music track or biome soundtrack changes.",
                ModuleCategory.AUDIO
        );
        addAliases("songnotify", "musictoast", "trackalert");
    }

    @Override
    protected void onDisable() {
        lastAnnouncedTrack = "";
    }

    @Override
    public void onTick() {
        if (client == null || client.options == null) {
            return;
        }
        if (client.options.getSoundVolume(SoundCategory.MUSIC) <= 0.001f) {
            return;
        }
        if (BanglaClient.getInstance() == null || BanglaClient.getInstance().getModuleManager() == null) {
            return;
        }

        var musicModOpt = BanglaClient.getInstance().getModuleManager().getModule(MusicDisplayModule.class);
        if (musicModOpt.isPresent()) {
            String currentTrack = musicModOpt.get().resolveCurrentMusicContextName();
            if (!currentTrack.isEmpty() && !currentTrack.equals(lastAnnouncedTrack)) {
                lastAnnouncedTrack = currentTrack;
                if (BanglaClient.getInstance().getNotificationManager() != null) {
                    BanglaClient.getInstance().getNotificationManager().pushInfo(
                            "Now Playing ♫",
                            currentTrack
                    );
                }
            }
        }
    }
}
