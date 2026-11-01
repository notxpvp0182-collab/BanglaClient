package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;

import java.util.EnumMap;
import java.util.Map;

/**
 * Module 56: Low Durability Alert
 * Notifies the user with a visual HUD banner, toast alert, and optional sound cue when any equipped armor
 * or held tool drops below a configurable durability percentage threshold.
 */
public final class LowDurabilityAlertModule extends HudModule {
    private final NumberSetting thresholdPercentSetting;
    private final BooleanSetting playWarningSoundSetting;

    private final Map<EquipmentSlot, Boolean> alreadyAlertedSlots = new EnumMap<>(EquipmentSlot.class);
    private String activeWarningText = null;

    public LowDurabilityAlertModule() {
        super(
                "low_durability_alert",
                "Low Durability Alert",
                "Alerts the player when equipped armor or held tools drop below a durability threshold.",
                ModuleCategory.INVENTORY,
                590.0f,
                228.0f,
                138.0f,
                22.0f,
                false
        );
        this.thresholdPercentSetting = addSetting(new NumberSetting(
                "threshold_pct", "Warning Threshold", "Durability percentage that triggers the alert",
                12.0, 3.0, 35.0, 1.0, "%"
        ));
        this.playWarningSoundSetting = addSetting(new BooleanSetting(
                "play_sound", "Play Warning Chime", "Play a subtle note-block chime when durability crosses the threshold", true
        ));
        addAliases("durabilitywarning", "armorbreak", "toolbreak");
    }

    @Override
    public void onTick() {
        if (client.player == null) {
            activeWarningText = null;
            alreadyAlertedSlots.clear();
            return;
        }

        int threshold = thresholdPercentSetting.getIntValue();
        String worstWarning = null;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack s = client.player.getEquippedStack(slot);
            if (s.isEmpty() || !s.isDamageable() || s.getMaxDamage() <= 0) {
                alreadyAlertedSlots.put(slot, false);
                continue;
            }
            int rem = s.getMaxDamage() - s.getDamage();
            int pct = Math.round((rem * 100.0f) / s.getMaxDamage());

            if (pct <= threshold) {
                worstWarning = "LOW: " + s.getName().getString() + " (" + pct + "%)";
                if (!Boolean.TRUE.equals(alreadyAlertedSlots.get(slot))) {
                    alreadyAlertedSlots.put(slot, true);
                    if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
                        BanglaClient.getInstance().getNotificationManager().pushWarning(
                                "Low Durability!",
                                s.getName().getString() + " is at " + pct + "% (" + rem + " uses left)"
                        );
                    }
                    if (playWarningSoundSetting.isEnabled()) {
                        client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.7f, 0.8f);
                    }
                }
            } else if (pct > threshold + 2) {
                alreadyAlertedSlots.put(slot, false);
            }
        }

        this.activeWarningText = worstWarning;
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        String text = activeWarningText;
        if (text == null) {
            if (!preview) {
                return;
            }
            text = "LOW: Diamond Pickaxe (9%)";
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
                    backgroundColorSetting.getArgb(), false, true, showBorderSetting.isEnabled()
            );
        }

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, ThemeTokens.STATUS_ERROR);
    }
}
