package com.ahmed.banglaclient.hud;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Coordinates in-game rendering of all enabled HudModule widgets and toast notifications.
 */
public final class HudManager {
    private final ModuleManager moduleManager;
    private boolean hudVisible = true;

    public HudManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    /**
     * Invoked every frame via Fabric's HudRenderCallback.
     */
    public void renderInGameHud(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options.hudHidden) {
            return;
        }
        // If HudEditorScreen is open, it renders the widgets interactively with selection guides
        if (client.currentScreen instanceof HudEditorScreen) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        if (hudVisible) {
            for (HudModule hudModule : moduleManager.getHudModules()) {
                if (!hudModule.isEnabled()) {
                    continue;
                }
                clampWidgetToScreen(hudModule, screenWidth, screenHeight);
                renderScaledWidget(context, hudModule, tickDelta, false);
            }
        }

        // Always render active toast notifications in the bottom-right corner
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
            BanglaClient.getInstance().getNotificationManager().render(context, screenWidth, screenHeight);
        }
    }

    public void renderScaledWidget(DrawContext context, HudModule widget, float tickDelta, boolean preview) {
        float scale = widget.getScale();
        if (Math.abs(scale - 1.0f) < 0.001f) {
            widget.renderHud(context, tickDelta, preview);
            return;
        }
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(widget.getPosX(), widget.getPosY(), 0.0f);
        matrices.scale(scale, scale, 1.0f);
        matrices.translate(-widget.getPosX(), -widget.getPosY(), 0.0f);
        try {
            widget.renderHud(context, tickDelta, preview);
        } finally {
            matrices.pop();
        }
    }

    public static void clampWidgetToScreen(HudModule widget, int screenWidth, int screenHeight) {
        float maxW = Math.max(0.0f, screenWidth - widget.getScaledWidth());
        float maxH = Math.max(0.0f, screenHeight - widget.getScaledHeight());
        if (widget.getPosX() < 0.0f || widget.getPosX() > maxW) {
            widget.setPosX(Math.max(0.0f, Math.min(widget.getPosX(), maxW)));
        }
        if (widget.getPosY() < 0.0f || widget.getPosY() > maxH) {
            widget.setPosY(Math.max(0.0f, Math.min(widget.getPosY(), maxH)));
        }
    }

    public void resetAllPositions() {
        for (HudModule widget : moduleManager.getHudModules()) {
            widget.resetPosition();
        }
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getConfigManager() != null) {
            BanglaClient.getInstance().getConfigManager().saveHudLayoutConfig();
        }
    }

    public boolean isHudVisible() {
        return hudVisible;
    }

    public void setHudVisible(boolean hudVisible) {
        this.hudVisible = hudVisible;
    }
}
