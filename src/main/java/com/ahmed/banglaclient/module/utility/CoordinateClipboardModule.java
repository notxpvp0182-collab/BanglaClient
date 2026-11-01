package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

/**
 * Module 116: Coordinate Clipboard
 * Copies the player's current X, Y, Z coordinates (and optional dimension name) directly to the OS clipboard
 * when toggled or triggered via keybind, then returns cleanly to standby.
 */
public final class CoordinateClipboardModule extends Module {
    private final BooleanSetting includeDimensionSetting;
    private final BooleanSetting formatAsTeleportCommandSetting;

    public CoordinateClipboardModule() {
        super(
                "coordinate_clipboard",
                "Coordinate Clipboard",
                "Copies your current X, Y, Z coordinates directly to the system clipboard.",
                ModuleCategory.UTILITY
        );
        this.includeDimensionSetting = addSetting(new BooleanSetting(
                "include_dim", "Include Dimension Name", "Append (Overworld / The Nether / The End) to copied coordinates", true
        ));
        this.formatAsTeleportCommandSetting = addSetting(new BooleanSetting(
                "tp_command", "Format as /tp Command", "Copy as '/tp X Y Z' for creative/admin convenience", false
        ));
        addAliases("copycoords", "clipboardcoords", "sharecoords");
    }

    @Override
    protected void onEnable() {
        copyCoordinatesNow();
        setEnabled(false);
    }

    public void copyCoordinatesNow() {
        if (client == null || client.player == null || client.keyboard == null) {
            return;
        }
        int x = client.player.getBlockX();
        int y = client.player.getBlockY();
        int z = client.player.getBlockZ();

        String formatted;
        if (formatAsTeleportCommandSetting.isEnabled()) {
            formatted = String.format("/tp %d %d %d", x, y, z);
        } else {
            formatted = String.format("X: %d, Y: %d, Z: %d", x, y, z);
            if (includeDimensionSetting.isEnabled() && client.world != null) {
                String dim = client.world.getRegistryKey().getValue().getPath();
                formatted += " (" + dim + ")";
            }
        }

        client.keyboard.setClipboard(formatted);
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
            BanglaClient.getInstance().getNotificationManager().pushSuccess("Copied Coordinates", formatted);
        }
    }
}
