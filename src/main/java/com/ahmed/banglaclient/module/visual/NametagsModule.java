package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

/**
 * Separate "Nametags" module controlling local player third-person nametag visibility
 * and BANGLA CLIENT identity badge rendering on player nametags.
 */
public final class NametagsModule extends Module {
    private final BooleanSetting showOwnNametagSetting;
    private final BooleanSetting showBcIdentityBadgeSetting;

    public NametagsModule() {
        super(
                "nametags",
                "Nametags",
                "Displays your own nametag in third-person view and shows BC identity badges.",
                ModuleCategory.VISUAL,
                -1,
                true
        );
        this.showOwnNametagSetting = addSetting(new BooleanSetting(
                "own_nametag", "Show Own Nametag in 3rd Person", "Display your own player nametag when in third-person camera view", true
        ));
        this.showBcIdentityBadgeSetting = addSetting(new BooleanSetting(
                "bc_badge", "Show BC Identity Badge", "Display the [BC] emblem beside verified BANGLA CLIENT users' nametags", true
        ));
        addAliases("nametag", "ownnametag", "thirdpersonnametag", "bcnametag");
    }

    public boolean shouldShowOwnNametag() {
        return isEnabled() && showOwnNametagSetting.isEnabled();
    }

    public boolean shouldShowBcBadge() {
        return isEnabled() && showBcIdentityBadgeSetting.isEnabled();
    }
}
