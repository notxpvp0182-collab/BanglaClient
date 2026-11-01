package com.ahmed.banglaclient.module;

/**
 * Defines the 10 primary module categories displayed in the BANGLA CLIENT Click GUI sidebar.
 */
public enum ModuleCategory {
    HUD("HUD", "HUD & Information", "On-screen telemetry, counters, and status overlays"),
    COMBAT("PvP Information", "PvP & Combat Information", "Legitimate combat feedback, armor, and cooldown metrics"),
    VISUAL("Visual", "Visual Enhancement", "Camera, lighting, weather, and overlay customization"),
    INVENTORY("Inventory", "Inventory & Item Information", "Equipment, durability, container, and slot displays"),
    MOVEMENT("Movement", "Movement & Player Information", "Sprint/sneak toggles and movement telemetry"),
    WORLD("World", "World & Environment", "Time, moon phase, light level, chunk, and biome data"),
    CHAT("Chat", "Chat & Communication", "Timestamps, filtering, search, copy, and notifications"),
    AUDIO("Audio", "Audio & Sound", "Per-category volume controls, mute toggles, and music info"),
    PERFORMANCE("Performance", "Performance & Optimization", "Frame/memory graphs, particle limits, and FPS warnings"),
    UTILITY("Utility", "Utility & Convenience", "Waypoints, screenshots, configs, keybinds, and shortcuts");

    private final String sidebarLabel;
    private final String fullTitle;
    private final String description;

    ModuleCategory(String sidebarLabel, String fullTitle, String description) {
        this.sidebarLabel = sidebarLabel;
        this.fullTitle = fullTitle;
        this.description = description;
    }

    public String getSidebarLabel() {
        return sidebarLabel;
    }

    public String getFullTitle() {
        return fullTitle;
    }

    public String getDescription() {
        return description;
    }
}
