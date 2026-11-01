package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import net.minecraft.client.gui.screen.DeathScreen;

/**
 * Module 90: Auto Respawn
 * Provides a configurable convenience option for respawning after death, with a configurable delay
 * (so the player can read their death message) and optional death coordinate logging.
 */
public final class AutoRespawnModule extends Module {
    private final NumberSetting respawnDelayTicksSetting;
    private final BooleanSetting notifyDeathCoordsSetting;

    private int ticksInDeathScreen = 0;
    private boolean notifiedThisDeath = false;

    public AutoRespawnModule() {
        super(
                "auto_respawn",
                "Auto Respawn",
                "Automatically clicks Respawn after a configurable delay when the Death Screen opens.",
                ModuleCategory.CHAT
        );
        this.respawnDelayTicksSetting = addSetting(new NumberSetting(
                "delay_ticks", "Respawn Delay", "Delay in ticks before requesting respawn (20 ticks = 1.0s)",
                20.0, 5.0, 100.0, 5.0, "t"
        ));
        this.notifyDeathCoordsSetting = addSetting(new BooleanSetting(
                "log_coords", "Notify Death Coordinates", "Show a toast notification with your X, Y, Z death position", true
        ));
        addAliases("respawn", "fastrespawn", "deathscreen");
    }

    @Override
    protected void onDisable() {
        ticksInDeathScreen = 0;
        notifiedThisDeath = false;
    }

    @Override
    public void onTick() {
        if (client == null || client.player == null) {
            return;
        }
        if (!(client.currentScreen instanceof DeathScreen)) {
            ticksInDeathScreen = 0;
            notifiedThisDeath = false;
            return;
        }

        if (notifyDeathCoordsSetting.isEnabled() && !notifiedThisDeath) {
            notifiedThisDeath = true;
            String coords = String.format("Died at X: %d, Y: %d, Z: %d",
                    client.player.getBlockX(), client.player.getBlockY(), client.player.getBlockZ());
            if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
                BanglaClient.getInstance().getNotificationManager().pushWarning("Death Location", coords);
            }
        }

        ticksInDeathScreen++;
        if (ticksInDeathScreen >= respawnDelayTicksSetting.getIntValue()) {
            ticksInDeathScreen = 0;
            client.player.requestRespawn();
            client.setScreen(null);
        }
    }
}
