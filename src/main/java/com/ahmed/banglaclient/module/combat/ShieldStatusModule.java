package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Module 25: Shield Status
 * Displays shield availability, remaining durability, blocking state, and axe-disable cooldown status.
 */
public final class ShieldStatusModule extends HudModule {
    private static final ItemStack SHIELD_ICON = new ItemStack(Items.SHIELD);

    private final BooleanSetting showDurabilitySetting;

    public ShieldStatusModule() {
        super(
                "shield_status",
                "Shield Status",
                "Displays shield availability, durability, and axe-disable cooldown status.",
                ModuleCategory.COMBAT,
                300.0f,
                310.0f,
                108.0f,
                24.0f,
                false
        );
        this.showDurabilitySetting = addSetting(new BooleanSetting(
                "show_durability", "Show Durability", "Include remaining shield durability points", true
        ));
        addAliases("shield", "blocking", "guard");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        ItemStack shieldStack = ItemStack.EMPTY;
        float cooldownProgress = 0.0f;
        boolean blocking = false;

        if (client.player != null) {
            if (client.player.getOffHandStack().isOf(Items.SHIELD)) {
                shieldStack = client.player.getOffHandStack();
            } else if (client.player.getMainHandStack().isOf(Items.SHIELD)) {
                shieldStack = client.player.getMainHandStack();
            }
            cooldownProgress = client.player.getItemCooldownManager().getCooldownProgress(SHIELD_ICON, tickDelta);
            blocking = client.player.isBlocking();
        } else if (preview) {
            shieldStack = SHIELD_ICON;
        }

        if (shieldStack.isEmpty() && !preview) {
            return;
        }

        String status;
        int statusColor;
        if (cooldownProgress > 0.0f) {
            status = String.format("DISABLED (%.0f%%)", cooldownProgress * 100.0f);
            statusColor = ThemeTokens.STATUS_ERROR;
        } else if (blocking) {
            status = "BLOCKING";
            statusColor = ThemeTokens.DEFAULT_ACCENT_PRIMARY;
        } else {
            status = "READY";
            statusColor = ThemeTokens.STATUS_SUCCESS;
        }

        if (showDurabilitySetting.isEnabled() && shieldStack.isDamageable()) {
            int rem = shieldStack.getMaxDamage() - shieldStack.getDamage();
            status += " [" + rem + "]";
        }

        int pad = paddingSetting.getIntValue();
        int boxW = 20 + client.textRenderer.getWidth(status) + pad * 2;
        int boxH = 16 + pad * 2;

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

        context.drawItem(SHIELD_ICON, x + pad, y + pad);
        context.drawTextWithShadow(client.textRenderer, status, x + pad + 20, y + pad + 4, statusColor);
    }
}
