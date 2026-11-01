package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.StringSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Module 115: Waypoint Manager
 * Allows creating, saving, deleting, and displaying user-created waypoints (plus optional automatic
 * Last Death waypoint creation), persisted inside `modules.json`.
 */
public final class WaypointManagerModule extends HudModule {
    public record Waypoint(String name, int x, int y, int z, String dimension) {
    }

    private final StringSetting newWaypointNameSetting;
    private final BooleanSetting createWaypointAtPlayerActionSetting;
    private final BooleanSetting clearAllWaypointsActionSetting;
    private final BooleanSetting autoSaveDeathWaypointSetting;

    private final List<Waypoint> waypoints = new ArrayList<>();
    private boolean recordedCurrentDeath = false;

    public WaypointManagerModule() {
        super(
                "waypoint_manager",
                "Waypoint Manager",
                "Creates, saves, deletes, and displays user waypoints and distances on the HUD.",
                ModuleCategory.UTILITY,
                290.0f,
                380.0f,
                148.0f,
                52.0f,
                false
        );
        this.newWaypointNameSetting = addSetting(new StringSetting(
                "wp_name", "New Waypoint Name", "Label for the next waypoint created", "Base", 24
        ));
        this.createWaypointAtPlayerActionSetting = addSetting(new BooleanSetting(
                "add_here", "Action: Add Waypoint Here", "Toggle ON to save a waypoint at your current X, Y, Z position", false
        ));
        this.createWaypointAtPlayerActionSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val)) {
                addWaypointAtCurrentPlayer(newWaypointNameSetting.getValue());
                createWaypointAtPlayerActionSetting.setValue(false);
            }
        });

        this.clearAllWaypointsActionSetting = addSetting(new BooleanSetting(
                "clear_all", "Action: Delete All Waypoints", "Toggle ON to clear all saved waypoints", false
        ));
        this.clearAllWaypointsActionSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val)) {
                waypoints.clear();
                clearAllWaypointsActionSetting.setValue(false);
            }
        });

        this.autoSaveDeathWaypointSetting = addSetting(new BooleanSetting(
                "death_waypoint", "Auto-Save Last Death Point", "Automatically save a 'Last Death' waypoint when you die", true
        ));
        addAliases("waypoints", "markers", "bookmarks", "deathpoint");
    }

    public void addWaypointAtCurrentPlayer(String label) {
        if (client == null || client.player == null || client.world == null) {
            return;
        }
        String cleanLabel = (label == null || label.isBlank()) ? ("WP #" + (waypoints.size() + 1)) : label.trim();
        String dim = client.world.getRegistryKey().getValue().getPath();
        Waypoint wp = new Waypoint(cleanLabel, client.player.getBlockX(), client.player.getBlockY(), client.player.getBlockZ(), dim);
        waypoints.removeIf(existing -> existing.name().equalsIgnoreCase(cleanLabel));
        waypoints.add(wp);

        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
            BanglaClient.getInstance().getNotificationManager().pushSuccess(
                    "Waypoint Saved",
                    String.format("%s [%d, %d, %d]", cleanLabel, wp.x(), wp.y(), wp.z())
            );
        }
    }

    @Override
    public void onTick() {
        if (client == null || client.player == null) {
            return;
        }
        if (client.currentScreen instanceof DeathScreen) {
            if (autoSaveDeathWaypointSetting.isEnabled() && !recordedCurrentDeath) {
                recordedCurrentDeath = true;
                addWaypointAtCurrentPlayer("Last Death");
            }
        } else {
            recordedCurrentDeath = false;
        }
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        if (waypoints.isEmpty() && !preview) {
            return;
        }

        List<String> lines = new ArrayList<>();
        lines.add("Waypoints (" + waypoints.size() + "):");

        if (waypoints.isEmpty()) {
            lines.add("• Base [120, 64, -340] (45m)");
        } else {
            String currentDim = client.world != null ? client.world.getRegistryKey().getValue().getPath() : "overworld";
            int shown = 0;
            for (int i = waypoints.size() - 1; i >= 0 && shown < 4; i--) {
                Waypoint wp = waypoints.get(i);
                if (!wp.dimension().equals(currentDim)) {
                    continue;
                }
                double dist = 0.0;
                if (client.player != null) {
                    double dx = client.player.getX() - wp.x();
                    double dy = client.player.getY() - wp.y();
                    double dz = client.player.getZ() - wp.z();
                    dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                }
                lines.add(String.format("• %s [%d, %d, %d] (%.0fm)", wp.name(), wp.x(), wp.y(), wp.z(), dist));
                shown++;
            }
        }

        int pad = paddingSetting.getIntValue();
        int maxW = 112;
        for (String line : lines) {
            maxW = Math.max(maxW, client.textRenderer.getWidth(line));
        }

        int boxW = maxW + pad * 2;
        int boxH = lines.size() * 11 + pad * 2;
        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());
        Theme theme = GlassRenderer.getTheme();

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        int drawY = y + pad + 1;
        for (int i = 0; i < lines.size(); i++) {
            int color = (i == 0) ? ThemeTokens.DEFAULT_ACCENT_PRIMARY : (lines.get(i).contains("Last Death") ? ThemeTokens.STATUS_WARNING : theme.getTextPrimary());
            context.drawTextWithShadow(client.textRenderer, lines.get(i), x + pad, drawY, color);
            drawY += 11;
        }
    }

    public List<Waypoint> getWaypoints() {
        return Collections.unmodifiableList(waypoints);
    }

    @Override
    public JsonObject toJson() {
        JsonObject root = super.toJson();
        JsonArray arr = new JsonArray();
        for (Waypoint wp : waypoints) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", wp.name());
            obj.addProperty("x", wp.x());
            obj.addProperty("y", wp.y());
            obj.addProperty("z", wp.z());
            obj.addProperty("dim", wp.dimension());
            arr.add(obj);
        }
        root.add("savedWaypoints", arr);
        return root;
    }

    @Override
    public void fromJson(JsonObject root) {
        super.fromJson(root);
        waypoints.clear();
        if (root != null && root.has("savedWaypoints") && root.get("savedWaypoints").isJsonArray()) {
            for (JsonElement el : root.getAsJsonArray("savedWaypoints")) {
                if (el.isJsonObject()) {
                    JsonObject obj = el.getAsJsonObject();
                    String name = obj.has("name") ? obj.get("name").getAsString() : "WP";
                    int x = obj.has("x") ? obj.get("x").getAsInt() : 0;
                    int y = obj.has("y") ? obj.get("y").getAsInt() : 64;
                    int z = obj.has("z") ? obj.get("z").getAsInt() : 0;
                    String dim = obj.has("dim") ? obj.get("dim").getAsString() : "overworld";
                    waypoints.add(new Waypoint(name, x, y, z, dim));
                }
            }
        }
    }
}
