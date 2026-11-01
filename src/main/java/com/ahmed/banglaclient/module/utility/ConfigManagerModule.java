package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.StringSetting;

/**
 * Module 118: Config Manager
 * Allows saving, loading, exporting to clipboard, importing from clipboard, and resetting client configurations.
 */
public final class ConfigManagerModule extends Module {
    private final StringSetting profileNameSetting;
    private final BooleanSetting saveProfileActionSetting;
    private final BooleanSetting loadProfileActionSetting;
    private final BooleanSetting exportClipboardActionSetting;
    private final BooleanSetting importClipboardActionSetting;

    public ConfigManagerModule() {
        super(
                "config_manager",
                "Config Manager",
                "Saves, loads, imports, exports, and resets BANGLA CLIENT configuration profiles.",
                ModuleCategory.UTILITY
        );
        this.profileNameSetting = addSetting(new StringSetting(
                "profile_name", "Target Profile Name", "Profile name for Save / Load actions", "Default", 28
        ));

        this.saveProfileActionSetting = addSetting(new BooleanSetting(
                "save_action", "Action: Save Profile", "Toggle ON to save current client settings to the target profile", false
        ));
        this.saveProfileActionSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val)) {
                saveNamedProfile();
                saveProfileActionSetting.setValue(false);
            }
        });

        this.loadProfileActionSetting = addSetting(new BooleanSetting(
                "load_action", "Action: Load Profile", "Toggle ON to load the target profile from disk", false
        ));
        this.loadProfileActionSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val)) {
                loadNamedProfile();
                loadProfileActionSetting.setValue(false);
            }
        });

        this.exportClipboardActionSetting = addSetting(new BooleanSetting(
                "export_clipboard", "Action: Export Config to Clipboard", "Toggle ON to copy full JSON profile to clipboard", false
        ));
        this.exportClipboardActionSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val)) {
                exportToClipboard();
                exportClipboardActionSetting.setValue(false);
            }
        });

        this.importClipboardActionSetting = addSetting(new BooleanSetting(
                "import_clipboard", "Action: Import Config from Clipboard", "Toggle ON to apply JSON profile from clipboard", false
        ));
        this.importClipboardActionSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val)) {
                importFromClipboard();
                importClipboardActionSetting.setValue(false);
            }
        });
        addAliases("profiles", "configs", "saveconfig", "loadconfig", "import", "export");
    }

    private void saveNamedProfile() {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getProfileManager() == null) {
            return;
        }
        String name = profileNameSetting.getValue();
        if (bangla.getProfileManager().saveProfile(name)) {
            bangla.getNotificationManager().pushSuccess("Profile Saved", "Saved profile '" + name + "'.");
        }
    }

    private void loadNamedProfile() {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getProfileManager() == null) {
            return;
        }
        String name = profileNameSetting.getValue();
        if (bangla.getProfileManager().loadProfile(name)) {
            bangla.getNotificationManager().pushSuccess("Profile Loaded", "Loaded profile '" + name + "'.");
        } else {
            bangla.getNotificationManager().pushWarning("Profile Not Found", "No saved profile named '" + name + "'.");
        }
    }

    private void exportToClipboard() {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getProfileManager() == null || client == null || client.keyboard == null) {
            return;
        }
        String json = bangla.getProfileManager().exportToJsonString();
        client.keyboard.setClipboard(json);
        bangla.getNotificationManager().pushSuccess("Config Exported", "Copied JSON profile to clipboard.");
    }

    private void importFromClipboard() {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getProfileManager() == null || client == null || client.keyboard == null) {
            return;
        }
        String clip = client.keyboard.getClipboard();
        if (bangla.getProfileManager().importFromJsonString(clip)) {
            bangla.getNotificationManager().pushSuccess("Config Imported", "Applied configuration from clipboard.");
        } else {
            bangla.getNotificationManager().pushWarning("Import Failed", "Clipboard does not contain a valid BANGLA CLIENT JSON profile.");
        }
    }
}
