package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

public final class CoordinateClipboardModule extends Module {
    private final BooleanSetting includeDimensionSetting;
    private final BooleanSetting formatAsTeleportCommandSetting;
    private final BooleanSetting copyNowActionSetting;

    public CoordinateClipboardModule() {
        super(
                "coordinate_clipboard",
                "Coordinate Clipboard",
                "Copies your current X, Y, Z coordinates to the clipboard when pressing its keybind.",
                ModuleCategory.UTILITY
        );
        setTriggerKeybindWhileEnabledOnly(true);

        this.includeDimensionSetting = addSetting(new BooleanSetting(
                "include_dim", "Include Dimension Name", "Append dimension name to copied coordinates", true
        ));
        this.formatAsTeleportCommandSetting = addSetting(new BooleanSetting(
                "tp_command", "Format as /tp Command", "Copy as '/tp X Y Z'", false
        ));
        this.copyNowActionSetting = addSetting(new BooleanSetting(
                "copy_now", "Action: Copy Coordinates Now", "Click to immediately copy coordinates to clipboard", false
        ));
        this.copyNowActionSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val)) {
                copyCoordinatesNow();
                copyNowActionSetting.setValue(false);
            }
        });
        addAliases("copycoords", "clipboardcoords", "sharecoords");
    }

    @Override
    public void onKeybindPressed() {
        if (isEnabled()) {
            copyCoordinatesNow();
        }
    }

    public void copyCoordinatesNow() {
        if (client == null || client.player == null || client.keyboard == null) return;
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
