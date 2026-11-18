package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class UnfocusedFpsLimiterModule extends Module {
    private final NumberSetting unfocusedCapSetting;
    private final BooleanSetting menuCapEnabledSetting;
    private final NumberSetting menuCapSetting;
    private int savedUserMaxFps = -1;

    public UnfocusedFpsLimiterModule() {
        super("unfocused_fps_limiter", "Unfocused & Menu FPS Limiter",
                "Lowers FPS cap when the game window is unfocused or in menus, restoring full FPS in-game.",
                ModuleCategory.PERFORMANCE);
        this.unfocusedCapSetting = addSetting(new NumberSetting("unfocused_fps", "Unfocused FPS Limit", "Max FPS when window is unfocused", 20.0, 10.0, 60.0, 5.0, " FPS"));
        this.menuCapEnabledSetting = addSetting(new BooleanSetting("limit_menu", "Limit Menu FPS", "Apply a separate FPS cap while in menus", false));
        this.menuCapSetting = addSetting(new NumberSetting("menu_fps", "Menu FPS Limit", "Max FPS while on main/pause menus", 60.0, 30.0, 120.0, 10.0, " FPS"));
        addAliases("unfocusedfps", "menufpslimiter", "backgroundmode", "batterysaver", "afkoptimization");
    }

    @Override
    protected void onDisable() {
        restoreUserFps();
    }

    @Override
    public void onTick() {
        if (client == null || client.options == null) return;
        int currentCap = client.options.getMaxFps().getValue();
        boolean focused = client.isWindowFocused();
        boolean inMenu = client.currentScreen != null;

        if (!focused) {
            if (savedUserMaxFps < 0) savedUserMaxFps = currentCap;
            int target = unfocusedCapSetting.getIntValue();
            if (currentCap != target) client.options.getMaxFps().setValue(target);
        } else if (inMenu && menuCapEnabledSetting.isEnabled()) {
            if (savedUserMaxFps < 0) savedUserMaxFps = currentCap;
            int target = menuCapSetting.getIntValue();
            if (currentCap != target) client.options.getMaxFps().setValue(target);
        } else {
            restoreUserFps();
        }
    }

    private void restoreUserFps() {
        if (savedUserMaxFps > 0 && client != null && client.options != null) {
            client.options.getMaxFps().setValue(savedUserMaxFps);
            savedUserMaxFps = -1;
        }
    }
}
