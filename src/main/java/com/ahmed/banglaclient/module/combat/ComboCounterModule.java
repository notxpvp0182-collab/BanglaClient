package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;

/**
 * Module 19: Combo Counter
 * Displays client-observed attack sequence information.
 * Resets when the local player takes damage or after 3.5 seconds of inactivity.
 */
public final class ComboCounterModule extends HudModule {
    private static final long COMBO_EXPIRY_MILLIS = 3500L;

    private int currentCombo = 0;
    private long lastAttackMillis = 0L;
    private int lastRecordedPlayerHurtTime = 0;
    private static boolean callbackRegistered = false;
    private static ComboCounterModule activeInstance;

    public ComboCounterModule() {
        super(
                "combo_counter",
                "Combo Counter",
                "Displays client-observed consecutive attack sequence count.",
                ModuleCategory.COMBAT,
                300.0f,
                174.0f,
                78.0f,
                20.0f,
                false
        );
        activeInstance = this;
        registerAttackHookOnce();
        addAliases("combo", "hits", "streak");
    }

    private static synchronized void registerAttackHookOnce() {
        if (callbackRegistered) {
            return;
        }
        callbackRegistered = true;
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && activeInstance != null && activeInstance.isEnabled()) {
                if (entity instanceof LivingEntity living && living.hurtTime == 0) {
                    activeInstance.onLocalPlayerAttack();
                }
            }
            return ActionResult.PASS;
        });
    }

    private void onLocalPlayerAttack() {
        long now = System.currentTimeMillis();
        if (now - lastAttackMillis > COMBO_EXPIRY_MILLIS) {
            currentCombo = 1;
        } else {
            currentCombo++;
        }
        lastAttackMillis = now;
    }

    @Override
    public void onTick() {
        if (client.player == null) {
            currentCombo = 0;
            return;
        }
        int hurtTime = client.player.hurtTime;
        if (hurtTime > lastRecordedPlayerHurtTime && hurtTime > 0) {
            // Player took damage: reset combo
            currentCombo = 0;
        }
        lastRecordedPlayerHurtTime = hurtTime;

        if (currentCombo > 0 && (System.currentTimeMillis() - lastAttackMillis) > COMBO_EXPIRY_MILLIS) {
            currentCombo = 0;
        }
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        String text = currentCombo > 0 ? ("Combo: " + currentCombo) : "No Combo";
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
