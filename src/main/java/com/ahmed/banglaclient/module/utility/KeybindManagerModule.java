package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.gui.KeybindManagerScreen;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import org.lwjgl.glfw.GLFW;

/**
 * Module 119: Keybind Manager
 * Opens the dedicated `KeybindManagerScreen` to manage all client and module keybinds and inspect conflicts.
 */
public final class KeybindManagerModule extends Module {

    public KeybindManagerModule() {
        super(
                "keybind_manager",
                "Keybind Manager",
                "Opens the dedicated Keybind Manager & Conflict Inspector screen.",
                ModuleCategory.UTILITY,
                GLFW.GLFW_KEY_K,
                false
        );
        addAliases("keybinds", "hotkeys", "binds", "shortcuts");
    }

    @Override
    protected void onEnable() {
        if (client != null) {
            client.setScreen(new KeybindManagerScreen(client.currentScreen));
        }
        setEnabled(false);
    }
}
