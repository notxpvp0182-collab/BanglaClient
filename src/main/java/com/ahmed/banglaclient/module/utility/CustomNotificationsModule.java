package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

/**
 * Module 111: Custom Notifications
 * Controls BANGLA CLIENT's reusable toast notification system and allows sending a test notification.
 */
public final class CustomNotificationsModule extends Module {
    private final BooleanSetting enableModuleToggleToastsSetting;
    private final BooleanSetting sendTestToastActionSetting;

    public CustomNotificationsModule() {
        super(
                "custom_notifications",
                "Custom Notifications",
                "Configures the client's Liquid Glass toast notification system.",
                ModuleCategory.UTILITY,
                -1,
                true
        );
        this.enableModuleToggleToastsSetting = addSetting(new BooleanSetting(
                "toggle_toasts", "Show Module Toggle Toasts", "Display a notification card when toggling modules via keybinds", true
        ));
        this.sendTestToastActionSetting = addSetting(new BooleanSetting(
                "test_toast", "Action: Send Test Notification", "Toggle ON to preview a sample Liquid Glass notification", false
        ));
        this.sendTestToastActionSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val)) {
                if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
                    BanglaClient.getInstance().getNotificationManager().pushInfo(
                            "BANGLA CLIENT",
                            "Liquid Glass notification system is active."
                    );
                }
                sendTestToastActionSetting.setValue(false);
            }
        });
        addAliases("notifications", "toasts", "alerts");
    }

    @Override
    protected void onEnable() {
        syncState();
    }

    @Override
    protected void onDisable() {
        syncState();
    }

    private void syncState() {
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
            BanglaClient.getInstance().getNotificationManager().setEnabled(isEnabled() && (enableModuleToggleToastsSetting == null || enableModuleToggleToastsSetting.isEnabled()));
        }
    }
}
