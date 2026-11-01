package com.ahmed.banglaclient.config;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.core.PerformancePreset;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * Manages structured JSON configuration files (`modules.json`, `keybinds.json`,
 * `hud-layout.json`, `gui.json`, `theme.json`, and `menu.json`).
 */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path configDir;
    private final ModuleManager moduleManager;

    private final Path modulesFile;
    private final Path keybindsFile;
    private final Path hudLayoutFile;
    private final Path guiFile;
    private final Path themeFile;
    private final Path menuFile;

    private boolean customMainMenuEnabled = true;
    private boolean reducedMotion = false;
    private int clickGuiKeyCode = org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
    private int hudEditorKeyCode = org.lwjgl.glfw.GLFW.GLFW_KEY_H;
    private String activeThemeId = "red_theme";

    public ConfigManager(Path configDir, ModuleManager moduleManager) {
        this.configDir = configDir;
        this.moduleManager = moduleManager;

        this.modulesFile = configDir.resolve("modules.json");
        this.keybindsFile = configDir.resolve("keybinds.json");
        this.hudLayoutFile = configDir.resolve("hud-layout.json");
        this.guiFile = configDir.resolve("gui.json");
        this.themeFile = configDir.resolve("theme.json");
        this.menuFile = configDir.resolve("menu.json");
    }

    /**
     * Loads all configuration files safely from disk.
     */
    public synchronized void loadAll() {
        loadGuiAndSystemConfig();
        loadThemeConfig();
        loadMenuConfig();
        loadModulesConfig();
        loadKeybindsConfig();
        loadHudLayoutConfig();
        BanglaClient.LOGGER.info("Loaded BANGLA CLIENT configurations from {}", configDir);
    }

    /**
     * Saves all configuration files atomically to disk.
     */
    public synchronized void saveAll() {
        saveGuiAndSystemConfig();
        saveThemeConfig();
        saveMenuConfig();
        saveModulesConfig();
        saveKeybindsConfig();
        saveHudLayoutConfig();
    }

    public void loadModulesConfig() {
        JsonObject root = ConfigValidator.readSafeJsonObject(modulesFile);
        ConfigValidator.checkAndMigrateSchema(modulesFile, root);
        if (!root.has("modules") || !root.get("modules").isJsonObject()) {
            return;
        }
        JsonObject modulesObj = root.getAsJsonObject("modules");
        for (Map.Entry<String, JsonElement> entry : modulesObj.entrySet()) {
            moduleManager.getModuleById(entry.getKey()).ifPresent(module -> {
                if (entry.getValue().isJsonObject()) {
                    module.fromJson(entry.getValue().getAsJsonObject());
                }
            });
        }
    }

    public void saveModulesConfig() {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", ConfigValidator.CURRENT_SCHEMA_VERSION);
        JsonObject modulesObj = new JsonObject();
        for (Module module : moduleManager.getAllModules()) {
            modulesObj.add(module.getId(), module.toJson());
        }
        root.add("modules", modulesObj);
        writeAtomically(modulesFile, root);
    }

    public void loadKeybindsConfig() {
        JsonObject root = ConfigValidator.readSafeJsonObject(keybindsFile);
        if (root.has("clickGuiKey") && root.get("clickGuiKey").isJsonPrimitive()) {
            this.clickGuiKeyCode = root.get("clickGuiKey").getAsInt();
        }
        if (root.has("hudEditorKey") && root.get("hudEditorKey").isJsonPrimitive()) {
            this.hudEditorKeyCode = root.get("hudEditorKey").getAsInt();
        }
        if (root.has("moduleKeybinds") && root.get("moduleKeybinds").isJsonObject()) {
            JsonObject binds = root.getAsJsonObject("moduleKeybinds");
            for (Map.Entry<String, JsonElement> entry : binds.entrySet()) {
                moduleManager.getModuleById(entry.getKey()).ifPresent(module -> {
                    if (entry.getValue().isJsonPrimitive()) {
                        module.setKeyCode(entry.getValue().getAsInt());
                    }
                });
            }
        }
    }

    public void saveKeybindsConfig() {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", ConfigValidator.CURRENT_SCHEMA_VERSION);
        root.addProperty("clickGuiKey", clickGuiKeyCode);
        root.addProperty("hudEditorKey", hudEditorKeyCode);
        JsonObject binds = new JsonObject();
        for (Module module : moduleManager.getAllModules()) {
            binds.addProperty(module.getId(), module.getKeyCode());
        }
        root.add("moduleKeybinds", binds);
        writeAtomically(keybindsFile, root);
    }

    public void loadHudLayoutConfig() {
        JsonObject root = ConfigValidator.readSafeJsonObject(hudLayoutFile);
        if (!root.has("widgets") || !root.get("widgets").isJsonObject()) {
            return;
        }
        JsonObject widgetsObj = root.getAsJsonObject("widgets");
        for (HudModule hudModule : moduleManager.getHudModules()) {
            if (widgetsObj.has(hudModule.getId()) && widgetsObj.get(hudModule.getId()).isJsonObject()) {
                JsonObject posObj = widgetsObj.getAsJsonObject(hudModule.getId());
                if (posObj.has("x") && posObj.has("y")) {
                    hudModule.setPosition(posObj.get("x").getAsFloat(), posObj.get("y").getAsFloat());
                }
            }
        }
    }

    public void saveHudLayoutConfig() {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", ConfigValidator.CURRENT_SCHEMA_VERSION);
        JsonObject widgetsObj = new JsonObject();
        for (HudModule hudModule : moduleManager.getHudModules()) {
            JsonObject posObj = new JsonObject();
            posObj.addProperty("x", hudModule.getPosX());
            posObj.addProperty("y", hudModule.getPosY());
            posObj.addProperty("scale", hudModule.getScale());
            widgetsObj.add(hudModule.getId(), posObj);
        }
        root.add("widgets", widgetsObj);
        writeAtomically(hudLayoutFile, root);
    }

    public void loadGuiAndSystemConfig() {
        JsonObject root = ConfigValidator.readSafeJsonObject(guiFile);
        if (root.has("performancePreset") && root.get("performancePreset").isJsonPrimitive()) {
            String presetName = root.get("performancePreset").getAsString();
            for (PerformancePreset preset : PerformancePreset.values()) {
                if (preset.name().equalsIgnoreCase(presetName)) {
                    BanglaClient.getInstance().setPerformancePreset(preset);
                    break;
                }
            }
        }
        if (root.has("reducedMotion") && root.get("reducedMotion").isJsonPrimitive()) {
            this.reducedMotion = root.get("reducedMotion").getAsBoolean();
        }
    }

    public void saveGuiAndSystemConfig() {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", ConfigValidator.CURRENT_SCHEMA_VERSION);
        root.addProperty("performancePreset", BanglaClient.getInstance().getPerformancePreset().name());
        root.addProperty("reducedMotion", reducedMotion);
        writeAtomically(guiFile, root);
    }

    public void loadThemeConfig() {
        JsonObject root = ConfigValidator.readSafeJsonObject(themeFile);
        if (root.has("activeThemeId") && root.get("activeThemeId").isJsonPrimitive()) {
            this.activeThemeId = root.get("activeThemeId").getAsString();
        }
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null) {
            var tm = BanglaClient.getInstance().getThemeManager();
            if (root.has("priHue")) tm.setCustomPrimaryHue(root.get("priHue").getAsFloat());
            if (root.has("secHue")) tm.setCustomSecondaryHue(root.get("secHue").getAsFloat());
            if (root.has("glassAlpha")) tm.setCustomGlassAlpha(root.get("glassAlpha").getAsFloat());
            if (root.has("tintBri")) tm.setCustomTintBrightness(root.get("tintBri").getAsFloat());
            if (root.has("borderAlpha")) tm.setCustomBorderAlpha(root.get("borderAlpha").getAsFloat());
            if (root.has("textBri")) tm.setCustomTextBrightness(root.get("textBri").getAsFloat());
        }
    }

    public void saveThemeConfig() {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", ConfigValidator.CURRENT_SCHEMA_VERSION);
        root.addProperty("activeThemeId", activeThemeId);
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null) {
            var tm = BanglaClient.getInstance().getThemeManager();
            root.addProperty("priHue", tm.getCustomPrimaryHue());
            root.addProperty("secHue", tm.getCustomSecondaryHue());
            root.addProperty("glassAlpha", tm.getCustomGlassAlpha());
            root.addProperty("tintBri", tm.getCustomTintBrightness());
            root.addProperty("borderAlpha", tm.getCustomBorderAlpha());
            root.addProperty("textBri", tm.getCustomTextBrightness());
        }
        writeAtomically(themeFile, root);
    }

    public void loadMenuConfig() {
        JsonObject root = ConfigValidator.readSafeJsonObject(menuFile);
        if (root.has("customMainMenuEnabled") && root.get("customMainMenuEnabled").isJsonPrimitive()) {
            this.customMainMenuEnabled = root.get("customMainMenuEnabled").getAsBoolean();
        }
    }

    public void saveMenuConfig() {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", ConfigValidator.CURRENT_SCHEMA_VERSION);
        root.addProperty("customMainMenuEnabled", customMainMenuEnabled);
        writeAtomically(menuFile, root);
    }

    public void resetAllToDefaults() {
        for (Module module : moduleManager.getAllModules()) {
            module.resetSettingsToDefault();
            if (module instanceof HudModule hud) {
                hud.resetPosition();
            }
        }
        this.customMainMenuEnabled = true;
        this.reducedMotion = false;
        this.clickGuiKeyCode = org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
        this.hudEditorKeyCode = org.lwjgl.glfw.GLFW.GLFW_KEY_H;
        BanglaClient.getInstance().setPerformancePreset(PerformancePreset.BALANCED);
        saveAll();
    }

    public static void writeAtomically(Path targetFile, JsonObject content) {
        if (targetFile == null || content == null) {
            return;
        }
        try {
            Files.createDirectories(targetFile.getParent());
            Path tempFile = targetFile.resolveSibling(targetFile.getFileName().toString() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                GSON.toJson(content, writer);
            }
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            BanglaClient.LOGGER.error("Failed to save configuration file {}", targetFile, e);
        }
    }

    public boolean isCustomMainMenuEnabled() {
        return customMainMenuEnabled;
    }

    public void setCustomMainMenuEnabled(boolean customMainMenuEnabled) {
        this.customMainMenuEnabled = customMainMenuEnabled;
        saveMenuConfig();
    }

    public boolean isReducedMotion() {
        return reducedMotion;
    }

    public void setReducedMotion(boolean reducedMotion) {
        this.reducedMotion = reducedMotion;
        saveGuiAndSystemConfig();
    }

    public int getClickGuiKeyCode() {
        return clickGuiKeyCode;
    }

    public void setClickGuiKeyCode(int clickGuiKeyCode) {
        this.clickGuiKeyCode = clickGuiKeyCode;
        saveKeybindsConfig();
    }

    public int getHudEditorKeyCode() {
        return hudEditorKeyCode;
    }

    public void setHudEditorKeyCode(int hudEditorKeyCode) {
        this.hudEditorKeyCode = hudEditorKeyCode;
        saveKeybindsConfig();
    }

    public String getActiveThemeId() {
        return activeThemeId;
    }

    public void setActiveThemeId(String activeThemeId) {
        if (activeThemeId != null && !activeThemeId.isBlank()) {
            this.activeThemeId = activeThemeId;
            saveThemeConfig();
        }
    }
}
