package com.ahmed.banglaclient.config;

import com.ahmed.banglaclient.BanglaClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Validates JSON configuration files, handles schema version checks, and creates safe `.bak` backups.
 */
public final class ConfigValidator {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    private ConfigValidator() {
    }

    /**
     * Safely reads and parses a JSON file into a JsonObject.
     * If the file is corrupted, backs it up as `.corrupt.bak` and returns an empty JsonObject.
     */
    public static JsonObject readSafeJsonObject(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            return new JsonObject();
        }
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (parsed != null && parsed.isJsonObject()) {
                return parsed.getAsJsonObject();
            }
        } catch (Exception e) {
            BanglaClient.LOGGER.warn("Corrupted or unreadable config file at {}. Creating backup and using defaults.", filePath, e);
            createBackup(filePath, ".corrupt.bak");
        }
        return new JsonObject();
    }

    /**
     * Creates a backup copy of a configuration file before migration or recovery.
     */
    public static void createBackup(Path sourceFile, String suffix) {
        if (sourceFile == null || !Files.exists(sourceFile)) {
            return;
        }
        try {
            Path backupPath = sourceFile.resolveSibling(sourceFile.getFileName().toString() + suffix);
            Files.copy(sourceFile, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            BanglaClient.LOGGER.error("Failed to create backup for {}", sourceFile, e);
        }
    }

    /**
     * Ensures the root object contains a valid schema version header, backing up if upgrading.
     */
    public static boolean checkAndMigrateSchema(Path filePath, JsonObject root) {
        if (root == null || root.size() == 0) {
            return false;
        }
        int fileVersion = root.has("schemaVersion") && root.get("schemaVersion").isJsonPrimitive()
                ? root.get("schemaVersion").getAsInt()
                : 0;
        if (fileVersion > 0 && fileVersion < CURRENT_SCHEMA_VERSION) {
            createBackup(filePath, ".v" + fileVersion + ".bak");
            root.addProperty("schemaVersion", CURRENT_SCHEMA_VERSION);
            return true;
        }
        return false;
    }
}
