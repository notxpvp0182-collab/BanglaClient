package com.ahmed.banglaclient.module;

import com.ahmed.banglaclient.module.setting.Setting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Base class for all 120 modules in BANGLA CLIENT.
 * Manages lifecycle, settings registration, search matching, keybinds, and JSON state serialization.
 */
public abstract class Module {
    protected final MinecraftClient client = MinecraftClient.getInstance();

    private final String id;
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private final List<String> searchAliases = new ArrayList<>();
    private final List<Setting<?>> settings = new ArrayList<>();

    private boolean enabled;
    private boolean favorite;
    private int keyCode;
    private boolean holdKeybind;
    private long lastUsedTimestamp;

    protected Module(
            String id,
            String name,
            String description,
            ModuleCategory category,
            int defaultKeyCode,
            boolean defaultEnabled
    ) {
        this.id = Objects.requireNonNull(id, "Module id cannot be null").toLowerCase(Locale.ROOT);
        this.name = Objects.requireNonNull(name, "Module name cannot be null");
        this.description = Objects.requireNonNull(description, "Module description cannot be null");
        this.category = Objects.requireNonNull(category, "Module category cannot be null");
        this.keyCode = defaultKeyCode;
        this.enabled = false;
        if (defaultEnabled) {
            this.enabled = true;
        }
    }

    protected Module(String id, String name, String description, ModuleCategory category) {
        this(id, name, description, category, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    protected final <S extends Setting<?>> S addSetting(S setting) {
        if (setting != null && !settings.contains(setting)) {
            settings.add(setting);
        }
        return setting;
    }

    protected final void addAliases(String... aliases) {
        if (aliases != null) {
            for (String alias : aliases) {
                if (alias != null && !alias.isBlank()) {
                    searchAliases.add(alias.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
    }

    public final String getId() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final ModuleCategory getCategory() {
        return category;
    }

    public final List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    public final boolean hasSettings() {
        return !settings.isEmpty();
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(boolean targetState) {
        if (this.enabled == targetState) {
            return;
        }
        this.enabled = targetState;
        this.lastUsedTimestamp = System.currentTimeMillis();
        if (this.enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public final void toggle() {
        setEnabled(!this.enabled);
    }

    public final boolean isFavorite() {
        return favorite;
    }

    public final void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public final void toggleFavorite() {
        this.favorite = !this.favorite;
    }

    public final int getKeyCode() {
        return keyCode;
    }

    public final void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public final void clearKeybind() {
        this.keyCode = GLFW.GLFW_KEY_UNKNOWN;
    }

    public final boolean isHoldKeybind() {
        return holdKeybind;
    }

    public final void setHoldKeybind(boolean holdKeybind) {
        this.holdKeybind = holdKeybind;
    }

    public final long getLastUsedTimestamp() {
        return lastUsedTimestamp;
    }

    /**
     * Checks whether this module matches a user search query by name, category, description, or alias.
     */
    public boolean matchesSearch(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return true;
        }
        String query = rawQuery.trim().toLowerCase(Locale.ROOT);
        if (name.toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        if (id.contains(query)) {
            return true;
        }
        if (category.getSidebarLabel().toLowerCase(Locale.ROOT).contains(query)
                || category.getFullTitle().toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        if (description.toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        for (String alias : searchAliases) {
            if (alias.contains(query)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resets all settings of this module to their factory defaults.
     */
    public void resetSettingsToDefault() {
        for (Setting<?> setting : settings) {
            setting.resetToDefault();
        }
    }

    /**
     * Lifecycle hook invoked when the module transitions from OFF to ON.
     */
    protected void onEnable() {
        // Subclasses override when initialization is needed
    }

    /**
     * Lifecycle hook invoked when the module transitions from ON to OFF.
     */
    protected void onDisable() {
        // Subclasses override when cleanup is needed
    }

    /**
     * Called every client tick (20 times/sec) while the module is enabled.
     */
    public void onTick() {
        // Subclasses override when tick updates are needed
    }

    /**
     * Serializes the module's state and all its settings to a JsonObject.
     */
    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("enabled", enabled);
        root.addProperty("favorite", favorite);
        root.addProperty("keyCode", keyCode);
        root.addProperty("holdKeybind", holdKeybind);
        root.addProperty("lastUsed", lastUsedTimestamp);

        JsonObject settingsObj = new JsonObject();
        for (Setting<?> setting : settings) {
            settingsObj.add(setting.getId(), setting.toJson());
        }
        root.add("settings", settingsObj);
        return root;
    }

    /**
     * Restores the module's state and all its settings from a JsonObject.
     */
    public void fromJson(JsonObject root) {
        if (root == null) {
            return;
        }
        if (root.has("favorite") && root.get("favorite").isJsonPrimitive()) {
            this.favorite = root.get("favorite").getAsBoolean();
        }
        if (root.has("keyCode") && root.get("keyCode").isJsonPrimitive()) {
            this.keyCode = root.get("keyCode").getAsInt();
        }
        if (root.has("holdKeybind") && root.get("holdKeybind").isJsonPrimitive()) {
            this.holdKeybind = root.get("holdKeybind").getAsBoolean();
        }
        if (root.has("lastUsed") && root.get("lastUsed").isJsonPrimitive()) {
            this.lastUsedTimestamp = root.get("lastUsed").getAsLong();
        }
        if (root.has("settings") && root.get("settings").isJsonObject()) {
            JsonObject settingsObj = root.getAsJsonObject("settings");
            for (Setting<?> setting : settings) {
                if (settingsObj.has(setting.getId())) {
                    JsonElement val = settingsObj.get(setting.getId());
                    setting.fromJson(val);
                }
            }
        }
        if (root.has("enabled") && root.get("enabled").isJsonPrimitive()) {
            setEnabled(root.get("enabled").getAsBoolean());
        }
    }
}
