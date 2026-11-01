package com.ahmed.banglaclient.integration;

import com.ahmed.banglaclient.BanglaClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.List;
import java.util.Locale;

/**
 * Safe runtime integration bridge for Replay Mod and Vanilla Statistics.
 * Never fakes Replay Mod functionality; only exposes Replay actions when `replaymod` is loaded in FabricLoader.
 */
public final class ReplayModIntegration {

    private ReplayModIntegration() {
    }

    public static boolean isReplayModLoaded() {
        return FabricLoader.getInstance().isModLoaded("replaymod");
    }

    /**
     * Triggers Replay Mod's Replay Viewer button if injected onto the TitleScreen by Replay Mod.
     */
    public static boolean openReplayViewerIfAvailable(Screen titleScreen) {
        if (!isReplayModLoaded() || titleScreen == null) {
            return false;
        }
        List<ClickableWidget> externalButtons = MainMenuButtonBridge.collectThirdPartyButtons(titleScreen);
        for (ClickableWidget widget : externalButtons) {
            String msg = widget.getMessage() != null ? widget.getMessage().getString().toLowerCase(Locale.ROOT) : "";
            if (msg.contains("replay")) {
                widget.onClick(widget.getX() + 2, widget.getY() + 2);
                return true;
            }
        }
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
            BanglaClient.getInstance().getNotificationManager().pushInfo(
                    "Replay Mod",
                    "Use the Replay button in the Mod Extensions dock on the left."
            );
        }
        return false;
    }

    /**
     * Opens the vanilla Statistics screen when in-game player stat handler is available,
     * or notifies the user if called from the title screen before joining a world.
     */
    public static void openStatisticsScreen(Screen parentScreen) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) {
            return;
        }
        if (mc.player != null && mc.player.getStatHandler() != null) {
            mc.setScreen(new StatsScreen(parentScreen, mc.player.getStatHandler()));
        } else if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
            BanglaClient.getInstance().getNotificationManager().pushInfo(
                    "Statistics",
                    "Join a Singleplayer or Multiplayer world to view world-specific player statistics."
            );
        }
    }
}
