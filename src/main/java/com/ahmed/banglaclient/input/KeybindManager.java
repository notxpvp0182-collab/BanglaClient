package com.ahmed.banglaclient.input;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized keyboard and mouse shortcut manager for BANGLA CLIENT.
 * Handles edge-triggered and hold-mode keybinds, registers native Minecraft Controls bindings,
 * prevents triggering while typing in GUI screens, and detects duplicate keybind conflicts.
 */
public final class KeybindManager {
    private final ModuleManager moduleManager;
    private final Map<Integer, Boolean> previousKeyState = new HashMap<>();

    private KeyBinding vanillaClickGuiBinding;
    private KeyBinding vanillaHudEditorBinding;

    public KeybindManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    public void initialize() {
        this.vanillaClickGuiBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "banglaclient.key.open_click_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "banglaclient.key.category"
        ));
        this.vanillaHudEditorBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "banglaclient.key.open_hud_editor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "banglaclient.key.category"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        if (client == null || client.getWindow() == null) {
            return;
        }

        // Consume native KeyBinding presses if triggered while in-game
        if (vanillaClickGuiBinding != null && vanillaClickGuiBinding.wasPressed() && client.currentScreen == null) {
            client.setScreen(new ClickGuiScreen(false));
            return;
        }
        if (vanillaHudEditorBinding != null && vanillaHudEditorBinding.wasPressed() && client.currentScreen == null) {
            client.setScreen(new HudEditorScreen(null));
            return;
        }

        if (client.currentScreen != null) {
            previousKeyState.clear();
            return;
        }

        long windowHandle = client.getWindow().getHandle();
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getConfigManager() == null) {
            return;
        }

        // 1. Check Click GUI Keybind from ConfigManager
        int clickGuiKey = bangla.getConfigManager().getClickGuiKeyCode();
        if (isKeyJustPressed(windowHandle, clickGuiKey)) {
            client.setScreen(new ClickGuiScreen(false));
            return;
        }

        // 2. Check HUD Editor Keybind from ConfigManager
        int hudEditorKey = bangla.getConfigManager().getHudEditorKeyCode();
        if (isKeyJustPressed(windowHandle, hudEditorKey)) {
            client.setScreen(new HudEditorScreen(null));
            return;
        }

        // 3. Check all 120 Module Keybinds
        for (Module module : moduleManager.getAllModules()) {
            int key = module.getKeyCode();
            if (key == GLFW.GLFW_KEY_UNKNOWN || key <= 0) {
                continue;
            }
            boolean currentlyDown = isKeyDown(windowHandle, key);
            boolean wasDown = Boolean.TRUE.equals(previousKeyState.get(key));

            if (module.isHoldKeybind()) {
                if (currentlyDown != module.isEnabled()) {
                    module.setEnabled(currentlyDown);
                }
            } else if (currentlyDown && !wasDown) {
                module.toggle();
                if (bangla.getNotificationManager() != null) {
                    bangla.getNotificationManager().pushInfo(
                            module.getName(),
                            module.isEnabled() ? "Module Enabled" : "Module Disabled"
                    );
                }
            }
        }

        updateTrackedKeyStates(windowHandle, clickGuiKey, hudEditorKey);
    }

    private void updateTrackedKeyStates(long windowHandle, int clickGuiKey, int hudEditorKey) {
        if (clickGuiKey > 0) {
            previousKeyState.put(clickGuiKey, isKeyDown(windowHandle, clickGuiKey));
        }
        if (hudEditorKey > 0) {
            previousKeyState.put(hudEditorKey, isKeyDown(windowHandle, hudEditorKey));
        }
        for (Module module : moduleManager.getAllModules()) {
            int key = module.getKeyCode();
            if (key > 0) {
                previousKeyState.put(key, isKeyDown(windowHandle, key));
            }
        }
    }

    private boolean isKeyJustPressed(long windowHandle, int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN || keyCode <= 0) {
            return false;
        }
        boolean down = isKeyDown(windowHandle, keyCode);
        boolean wasDown = Boolean.TRUE.equals(previousKeyState.get(keyCode));
        return down && !wasDown;
    }

    private static boolean isKeyDown(long windowHandle, int keyCode) {
        if (keyCode <= 0) {
            return false;
        }
        return InputUtil.isKeyPressed(windowHandle, keyCode);
    }

    public List<Module> findConflictsForKey(int keyCode, Module excludeModule) {
        List<Module> conflicts = new ArrayList<>();
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN || keyCode <= 0) {
            return conflicts;
        }
        for (Module module : moduleManager.getAllModules()) {
            if (module != excludeModule && module.getKeyCode() == keyCode) {
                conflicts.add(module);
            }
        }
        return conflicts;
    }

    public static String formatKeyName(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN || keyCode <= 0) {
            return "NONE";
        }
        return InputUtil.fromKeyCode(keyCode, 0).getLocalizedText().getString().toUpperCase();
    }
}
