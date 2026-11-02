package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

/**
 * Separate "Tab List Heads" module controlling player head rendering and BC identity
 * badges inside the multiplayer Tab List without overlapping ping or team formatting.
 */
public final class TabListHeadsModule extends Module {
    private final BooleanSetting showBcTabBadgeSetting;
    private final BooleanSetting forceOfflineHeadsSetting;

    public TabListHeadsModule() {
        super(
                "tab_list_heads",
                "Tab List Heads",
                "Displays player skin heads and BC identity icons in the multiplayer Tab List.",
                ModuleCategory.HUD,
                -1,
                true
        );
        this.showBcTabBadgeSetting = addSetting(new BooleanSetting(
                "bc_tab_badge", "Show BC Identity in Tab List", "Display [BC] icon before verified BANGLA CLIENT users in Tab List", true
        ));
        this.forceOfflineHeadsSetting = addSetting(new BooleanSetting(
                "offline_heads", "Render Heads on All Servers", "Ensure player skin heads remain visible even on offline-mode servers", true
        ));
        addAliases("tabheads", "tablist", "playerlistheads");
    }

    public boolean shouldShowBcTabBadge() {
        return isEnabled() && showBcTabBadgeSetting.isEnabled();
    }

    public boolean shouldForceOfflineHeads() {
        return isEnabled() && forceOfflineHeadsSetting.isEnabled();
    }
}
