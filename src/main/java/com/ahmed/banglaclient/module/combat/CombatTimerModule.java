package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;

/**
 * Module 27: Combat Timer
 * Displays timing information related to the player's own attacks and damage received
 * (configurable countdown window, e.g., 15s or 30s).
 */
public final class CombatTimerModule extends HudModule {
    private final NumberSetting tagDurationSecondsSetting;

    private long lastCombatEventMillis = 0L;
    private int lastPlayerHurtTime = 0;
    private static boolean hookRegistered = false;
    private static CombatTimerModule instance;

    public CombatTimerModule() {
        super(
                "combat_timer",
                "Combat Timer",
                "Displays countdown timer since the player last attacked or took damage.",
                ModuleCategory.COMBAT,
                415.0f,
                38.0f,
                96.0f,
                20.0f,
                false
        );
        this.tagDurationSecondsSetting = addSetting(new NumberSetting(
                "tag_duration", "Combat Tag Duration", "Countdown duration in seconds after an attack",
                15.0, 5.0, 60.0, 1.0, "s"
        ));
        instance = this;
        registerHookOnce();
        addAliases("combattag", "ct", "pvptimer");
    }

    private static synchronized void registerHookOnce() {
        if (hookRegistered) {
            return;
        }
        hookRegistered = true;
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && instance != null && instance.isEnabled() && entity instanceof LivingEntity) {
                instance.lastCombatEventMillis = System.currentTimeMillis();
            }
            return ActionResult.PASS;
        });
    }

    @Override
    public void onTick() {
        if (client.player == null) {
            return;
        }
        int hurtTime = client.player.hurtTime;
        if (hurtTime > lastPlayerHurtTime && hurtTime > 0) {
            lastCombatEventMillis = System.currentTimeMillis();
        }
        lastPlayerHurtTime = hurtTime;
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        double tagTotalSec = tagDurationSecondsSetting.getValue();
        double elapsedSec = (System.currentTimeMillis() - lastCombatEventMillis) / 1000.0;
        double remainingSec = Math.max(0.0, tagTotalSec - elapsedSec);

        String text;
        int color;
        if (remainingSec > 0.0) {
            text = String.format("Combat: %.1fs", remainingSec);
            color = ThemeTokens.STATUS_WARNING;
        } else {
            text = "Combat: Safe";
            color = ThemeTokens.STATUS_SUCCESS;
        }

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

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, color);
    }
}
