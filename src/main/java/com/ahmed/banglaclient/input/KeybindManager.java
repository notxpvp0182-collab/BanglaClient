package com.ahmed.banglaclient.input;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class KeybindManager {
    private static final KeyBinding.Category BANGLA_KEY_CATEGORY =
            KeyBinding.Category.create(Identifier.of(BanglaClient.MOD_ID, "general"));
    private final ModuleManager moduleManager;
    private final Map<Integer, Boolean> previousKeyState = new HashMap<>();
    private KeyBinding vanillaClickGuiBinding;
    private KeyBinding vanillaHudEditorBinding;
    private KeyBinding chatScrollUpBinding;
    private KeyBinding chatScrollDownBinding;
    private int chatUpHoldTicks = 0;
    private int chatDownHoldTicks = 0;

    public KeybindManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    public void initialize() {
        this.vanillaClickGuiBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "banglaclient.key.open_click_gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, BANGLA_KEY_CATEGORY));
        this.vanillaHudEditorBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "banglaclient.key.open_hud_editor", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, BANGLA_KEY_CATEGORY));
        this.chatScrollUpBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("banglaclient.key.chat_up", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UP, BANGLA_KEY_CATEGORY));
        this.chatScrollDownBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("banglaclient.key.chat_down", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, BANGLA_KEY_CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        if (client == null || client.getWindow() == null) return;
        boolean upHeld = (chatScrollUpBinding != null && chatScrollUpBinding.isPressed()) || (client.currentScreen == null && InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_UP)); if (upHeld) { chatUpHoldTicks++; if (chatUpHoldTicks == 1) com.ahmed.banglaclient.livestream.LiveStreamManager.get().scrollUp(2); else if (chatUpHoldTicks >= 30 && chatUpHoldTicks % 2 == 0) com.ahmed.banglaclient.livestream.LiveStreamManager.get().scrollUp(1); } else { chatUpHoldTicks = 0; }
        boolean downHeld = (chatScrollDownBinding != null && chatScrollDownBinding.isPressed()) || (client.currentScreen == null && InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_DOWN)); if (downHeld) { chatDownHoldTicks++; if (chatDownHoldTicks == 1) com.ahmed.banglaclient.livestream.LiveStreamManager.get().scrollDown(2); else if (chatDownHoldTicks >= 40) com.ahmed.banglaclient.livestream.LiveStreamManager.get().jumpToLiveBottom(); } else { chatDownHoldTicks = 0; }
        if (vanillaClickGuiBinding != null && vanillaClickGuiBinding.wasPressed() && client.currentScreen == null) {
            client.setScreen(new ClickGuiScreen(false));
            return;
        }
        if (vanillaHudEditorBinding != null && vanillaHudEditorBinding.wasPressed() && client.currentScreen == null) {
            client.setScreen(new HudEditorScreen(null));
            return;
        }
        if (client.currentScreen != null) {
            for (Module m : moduleManager.getAllModules()) {
                if (m.isTriggerKeybindWhileEnabledOnly()) {
                    m.onKeybindHoldState(false);
                }
            }
            previousKeyState.clear();
            return;
        }

        Window window = client.getWindow();
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getConfigManager() == null) return;

        int clickGuiKey = bangla.getConfigManager().getClickGuiKeyCode();
        if (isKeyJustPressed(window, clickGuiKey)) {
            client.setScreen(new ClickGuiScreen(false));
            return;
        }
        int hudEditorKey = bangla.getConfigManager().getHudEditorKeyCode();
        if (isKeyJustPressed(window, hudEditorKey)) {
            client.setScreen(new HudEditorScreen(null));
            return;
        }

        for (Module module : moduleManager.getAllModules()) {
            int key = module.getKeyCode();
            if (key == GLFW.GLFW_KEY_UNKNOWN || key <= 0) {
                if (module.isTriggerKeybindWhileEnabledOnly()) {
                    module.onKeybindHoldState(false);
                }
                continue;
            }
            boolean down = isKeyDown(window, key);
            boolean was = Boolean.TRUE.equals(previousKeyState.get(key));

            if (module.isTriggerKeybindWhileEnabledOnly()) {
                // Only responds to keybind when the module is ENABLED in ClickGUI!
                if (module.isEnabled()) {
                    module.onKeybindHoldState(down);
                    if (down && !was) {
                        module.onKeybindPressed();
                    }
                } else {
                    module.onKeybindHoldState(false);
                }
            } else if (module.isHoldKeybind()) {
                if (down != module.isEnabled()) module.setEnabled(down);
            } else if (down && !was) {
                module.toggle();
                if (bangla.getNotificationManager() != null) {
                    bangla.getNotificationManager().pushInfo(module.getName(), module.isEnabled() ? "Module Enabled" : "Module Disabled");
                }
            }
        }

        if (clickGuiKey > 0) previousKeyState.put(clickGuiKey, isKeyDown(window, clickGuiKey));
        if (hudEditorKey > 0) previousKeyState.put(hudEditorKey, isKeyDown(window, hudEditorKey));
        for (Module m : moduleManager.getAllModules()) {
            if (m.getKeyCode() > 0) previousKeyState.put(m.getKeyCode(), isKeyDown(window, m.getKeyCode()));
        }
    }

    private boolean isKeyJustPressed(Window window, int key) {
        return key > 0 && isKeyDown(window, key) && !Boolean.TRUE.equals(previousKeyState.get(key));
    }

    private static boolean isKeyDown(Window window, int key) {
        return window != null && key > 0 && InputUtil.isKeyPressed(window, key);
    }

    public List<Module> findConflictsForKey(int keyCode, Module excludeModule) {
        List<Module> conflicts = new ArrayList<>();
        if (keyCode <= 0) return conflicts;
        for (Module m : moduleManager.getAllModules()) {
            if (m != excludeModule && m.getKeyCode() == keyCode) conflicts.add(m);
        }
        return conflicts;
    }

    public static String formatKeyName(int keyCode) {
        if (keyCode <= 0) return "NONE";
        return InputUtil.fromKeyCode(new KeyInput(keyCode, 0, 0)).getLocalizedText().getString().toUpperCase();
    }
}
