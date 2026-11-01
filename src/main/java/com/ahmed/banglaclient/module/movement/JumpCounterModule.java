package com.ahmed.banglaclient.module.movement;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 65: Jump Counter
 * Counts player jumps during the play session and displays the total on the HUD.
 */
public final class JumpCounterModule extends HudModule {
    private boolean wasOnGround = true;

    public JumpCounterModule() {
        super(
                "jump_counter",
                "Jump Counter",
                "Counts and displays total player jumps during the current session.",
                ModuleCategory.MOVEMENT,
                160.0f,
                158.0f,
                88.0f,
                20.0f,
                false
        );
        addAliases("jumps", "jumpcount", "hops");
    }

    @Override
    public void onTick() {
        if (client.player == null) {
            wasOnGround = true;
            return;
        }
        boolean onGround = client.player.isOnGround();
        if (wasOnGround && !onGround && client.player.getVelocity().y > 0.35 && client.options.jumpKey.isPressed()) {
            if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getSessionTracker() != null) {
                BanglaClient.getInstance().getSessionTracker().recordJump();
            }
        }
        wasOnGround = onGround;
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int jumps = 0;
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getSessionTracker() != null) {
            jumps = BanglaClient.getInstance().getSessionTracker().getSessionJumps();
        }
        if (preview && jumps == 0) {
            jumps = 42;
        }

        String text = "Jumps: " + jumps;
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
