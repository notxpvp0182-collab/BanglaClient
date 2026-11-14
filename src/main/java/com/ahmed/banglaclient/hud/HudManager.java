package com.ahmed.banglaclient.hud;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleManager;
import com.ahmed.banglaclient.module.visual.ZoomModule;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class HudManager {
    private final ModuleManager moduleManager;
    private boolean hudVisible = true;

    public HudManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    public void renderInGameHud(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options.hudHidden) return;
        if (client.currentScreen instanceof HudEditorScreen) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        if (hudVisible) {
            for (HudModule hudModule : moduleManager.getHudModules()) {
                if (!hudModule.isEnabled()) continue;
                clampWidgetToScreen(hudModule, screenWidth, screenHeight);
                renderScaledWidget(context, hudModule, tickDelta, false);
            }

            // Render Live Zoom Magnification Pill when actively zooming
            var zoomOpt = moduleManager.getModule(ZoomModule.class);
            if (zoomOpt.isPresent() && zoomOpt.get().shouldShowZoomHud()) {
                float div = zoomOpt.get().getFovDivisor();
                String zoomText = String.format("Zoom: %.1fx", div);
                int tw = client.textRenderer.getWidth(zoomText) + 12;
                int zx = (screenWidth - tw) / 2;
                int zy = screenHeight - 58;
                GlassRenderer.drawPillBadge(context, zx, zy, tw, 15, 0xFFFFFFFF, true);
                context.drawTextWithShadow(client.textRenderer, zoomText, zx + 6, zy + 4, 0xFFFFFFFF);
            }
        }

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
        matrices.pushMatrix();
        matrices.translate(widget.getPosX(), widget.getPosY());
        matrices.scale(scale, scale);
        matrices.translate(-widget.getPosX(), -widget.getPosY());
        try {
            widget.renderHud(context, tickDelta, preview);
        } finally {
            matrices.popMatrix();
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

    public boolean isHudVisible() { return hudVisible; }
    public void setHudVisible(boolean hudVisible) { this.hudVisible = hudVisible; }
}
