package com.ahmed.banglaclient.integration;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Inspects all ClickableWidgets registered on Minecraft's TitleScreen (including buttons injected
 * by Mod Menu, Replay Mod, Iris, or other Fabric mods via ScreenEvents) so BANGLA CLIENT's custom
 * main menu preserves 100% compatibility, click actions, tooltips, and enabled states.
 */
public final class MainMenuButtonBridge {

    private MainMenuButtonBridge() {
    }

    /**
     * Collects any third-party mod buttons added to TitleScreen that are not part of the standard
     * vanilla button set, allowing the custom BANGLA CLIENT menu to render and trigger them cleanly.
     */
    public static List<ClickableWidget> collectThirdPartyButtons(Screen titleScreen) {
        List<ClickableWidget> externalButtons = new ArrayList<>();
        if (titleScreen == null) {
            return externalButtons;
        }

        for (Element child : titleScreen.children()) {
            if (child instanceof ClickableWidget widget && widget.visible) {
                String label = widget.getMessage() != null ? widget.getMessage().getString().trim() : "";
                if (!isStandardVanillaButton(label, widget.getWidth())) {
                    externalButtons.add(widget);
                }
            }
        }
        return externalButtons;
    }

    private static boolean isStandardVanillaButton(String rawLabel, int width) {
        if (rawLabel == null) {
            return false;
        }
        String lower = rawLabel.toLowerCase(Locale.ROOT);
        if (lower.contains("singleplayer")
                || lower.contains("multiplayer")
                || lower.contains("minecraft realms")
                || lower.equals("realms")
                || lower.contains("options")
                || lower.contains("quit game")
                || lower.contains("language")
                || lower.contains("accessibility")
                || lower.contains("copyright")) {
            return true;
        }
        // Vanilla 20x20 icon buttons (Language / Accessibility) when their text is empty or icon-only
        return rawLabel.isEmpty() && width == 20;
    }
}
