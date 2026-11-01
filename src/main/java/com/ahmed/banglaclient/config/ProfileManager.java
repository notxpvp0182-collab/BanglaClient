package com.ahmed.banglaclient.config;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Manages named configuration profiles (`profiles/*.json`) and JSON clipboard import/export.
 */
public final class ProfileManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path profilesDir;
    private final ModuleManager moduleManager;
    private final ConfigManager configManager;
    private String activeProfileName = "Default";

    public ProfileManager(Path profilesDir, ModuleManager moduleManager, ConfigManager configManager) {
        this.profilesDir = profilesDir;
        this.moduleManager = moduleManager;
        this.configManager = configManager;
    }

    public List<String> listAvailableProfiles() {
        List<String> names = new ArrayList<>();
        names.add("Default");
        if (!Files.exists(profilesDir)) {
            return names;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(profilesDir, "*.json")) {
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString();
                String profileName = fileName.substring(0, fileName.length() - 5);
                if (!profileName.equalsIgnoreCase("Default") && !names.contains(profileName)) {
                    names.add(profileName);
                }
            }
        } catch (IOException e) {
            BanglaClient.LOGGER.error("Failed to list profiles in {}", profilesDir, e);
        }
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public boolean saveProfile(String rawName) {
        String safeName = sanitizeProfileName(rawName);
        if (safeName.isEmpty()) {
            return false;
        }
        Path file = profilesDir.resolve(safeName + ".json");
        JsonObject snapshot = createFullSnapshot(safeName);
        ConfigManager.writeAtomically(file, snapshot);
        this.activeProfileName = safeName;
        return true;
    }

    public boolean loadProfile(String rawName) {
        String safeName = sanitizeProfileName(rawName);
        if (safeName.isEmpty()) {
            return false;
        }
        Path file = profilesDir.resolve(safeName + ".json");
        if (!Files.exists(file)) {
            return false;
        }
        JsonObject root = ConfigValidator.readSafeJsonObject(file);
        if (applySnapshot(root)) {
            this.activeProfileName = safeName;
            configManager.saveAll();
            return true;
        }
        return false;
    }

    public boolean deleteProfile(String rawName) {
        String safeName = sanitizeProfileName(rawName);
        if (safeName.isEmpty() || safeName.equalsIgnoreCase("Default")) {
            return false;
        }
        Path file = profilesDir.resolve(safeName + ".json");
        try {
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            BanglaClient.LOGGER.error("Failed to delete profile {}", safeName, e);
            return false;
        }
    }

    public String exportToJsonString() {
        return GSON.toJson(createFullSnapshot(activeProfileName));
    }

    public boolean importFromJsonString(String jsonText) {
        if (jsonText == null || jsonText.isBlank()) {
            return false;
        }
        try {
            JsonElement element = JsonParser.parseString(jsonText);
            if (element != null && element.isJsonObject()) {
                boolean applied = applySnapshot(element.getAsJsonObject());
                if (applied) {
                    configManager.saveAll();
                }
                return applied;
            }
        } catch (Exception e) {
            BanglaClient.LOGGER.warn("Invalid JSON provided for profile import", e);
        }
        return false;
    }

    private JsonObject createFullSnapshot(String profileName) {
        JsonObject root = new JsonObject();
        root.addProperty("client", BanglaClient.CLIENT_NAME);
        root.addProperty("version", BanglaClient.CLIENT_VERSION);
        root.addProperty("schemaVersion", ConfigValidator.CURRENT_SCHEMA_VERSION);
        root.addProperty("profileName", profileName);

        JsonObject modulesObj = new JsonObject();
        for (Module module : moduleManager.getAllModules()) {
            modulesObj.add(module.getId(), module.toJson());
        }
        root.add("modules", modulesObj);
        return root;
    }

    private boolean applySnapshot(JsonObject root) {
        if (root == null || !root.has("modules") || !root.get("modules").isJsonObject()) {
            return false;
        }
        JsonObject modulesObj = root.getAsJsonObject("modules");
        for (Map.Entry<String, JsonElement> entry : modulesObj.entrySet()) {
            moduleManager.getModuleById(entry.getKey()).ifPresent(module -> {
                if (entry.getValue().isJsonObject()) {
                    module.fromJson(entry.getValue().getAsJsonObject());
                }
            });
        }
        return true;
    }

    private static String sanitizeProfileName(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().replaceAll("[^a-zA-Z0-9_\\- ]", "").trim();
    }

    public String getActiveProfileName() {
        return activeProfileName;
    }

    public String getProfileFileName(String profileName) {
        return sanitizeProfileName(profileName).toLowerCase(Locale.ROOT) + ".json";
    }
}
