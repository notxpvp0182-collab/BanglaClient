package com.ahmed.banglaclient.module.movement;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 64: Movement Status
 * Displays the player's current physical movement posture (Idle, Walking, Sprinting, Sneaking,
 * Swimming, Crawling, Gliding, Riding Mount, or Flying).
 */
public final class MovementStatusModule extends HudModule {

    public MovementStatusModule() {
        super(
                "movement_status",
                "Movement Status",
                "Displays current movement posture (Walking, Sprinting, Sneaking, Swimming, Gliding, Riding).",
                ModuleCategory.MOVEMENT,
                160.0f,
                134.0f,
                108.0f,
                20.0f,
                false
        );
        addAliases("posture", "movestate", "state");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        String status = "Standing";
        if (client.player != null) {
            if (client.player.hasVehicle()) {
                status = "Riding Mount";
            } else if (client.player.getAbilities().flying) {
                status = "Creative Flying";
            } else if (client.player.isGliding()) {
                status = "Elytra Gliding";
            } else if (client.player.isSwimming()) {
                status = "Swimming";
            } else if (client.player.isCrawling()) {
                status = "Crawling";
            } else if (client.player.isSneaking()) {
                status = "Sneaking";
            } else if (client.player.isSprinting()) {
                status = "Sprinting";
            } else if (!client.player.isOnGround()) {
                status = client.player.getVelocity().y > 0.0 ? "Jumping" : "Airborne";
            } else {
                double dx = client.player.getX() - client.player.prevX;
                double dz = client.player.getZ() - client.player.prevZ;
                if ((dx * dx + dz * dz) > 0.0004) {
                    status = "Walking";
                }
            }
        }

        String text = "State: " + status;
        int pad = paddingSetting.getIntValue();
        int boxW = client.textRenderer.getWidth(text) + pad * 2;
        int boxH = client.textRenderer.fontHeight + pad * 2;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, textColorSetting.getArgb());
    }
}
