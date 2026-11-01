package com.ahmed.banglaclient.module.movement;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

/**
 * Module 59: Toggle Sneak
 * Provides a configurable sneak toggle that automatically pauses when a GUI screen (Inventory, Chat, Chest)
 * is open or while flying/swimming, respecting vanilla movement rules.
 */
public final class ToggleSneakModule extends Module {
    private final BooleanSetting pauseInScreensSetting;
    private final BooleanSetting pauseWhileFlyingSetting;

    public ToggleSneakModule() {
        super(
                "toggle_sneak",
                "Toggle Sneak",
                "Provides a legitimate sneak toggle that pauses cleanly when opening GUI screens.",
                ModuleCategory.MOVEMENT
        );
        this.pauseInScreensSetting = addSetting(new BooleanSetting(
                "pause_in_gui", "Pause in Screens", "Release sneak automatically while inventory or chat is open", true
        ));
        this.pauseWhileFlyingSetting = addSetting(new BooleanSetting(
                "pause_flying", "Pause While Flying", "Do not descend continuously when flying in Creative mode", true
        ));
        addAliases("togglesneak", "crouch", "sneaktoggle");
    }

    @Override
    protected void onDisable() {
        if (client != null && client.options != null) {
            client.options.sneakKey.setPressed(false);
        }
    }

    @Override
    public void onTick() {
        if (client == null || client.player == null || client.options == null) {
            return;
        }
        if (pauseInScreensSetting.isEnabled() && client.currentScreen != null) {
            client.options.sneakKey.setPressed(false);
            return;
        }
        if (pauseWhileFlyingSetting.isEnabled() && client.player.getAbilities().flying) {
            return;
        }
        client.options.sneakKey.setPressed(true);
    }
}
