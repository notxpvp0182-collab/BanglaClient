package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

/**
 * Module 44: Hotbar HUD
 * Provides a customizable Liquid Glass hotbar information display with smooth sliding slot selection.
 */
public final class HotbarHudModule extends HudModule {
    private final BooleanSetting smoothSelectorSetting;
    private final BooleanSetting showOffhandSlotSetting;

    private float animatedSlotX = 0.0f;

    public HotbarHudModule() {
        super(
                "hotbar_hud",
                "Hotbar HUD",
                "Provides a customizable Liquid Glass hotbar display with smooth slot selection.",
                ModuleCategory.INVENTORY,
                415.0f,
                136.0f,
                188.0f,
                26.0f,
                false
        );
        this.smoothSelectorSetting = addSetting(new BooleanSetting(
                "smooth_select", "Smooth Slot Highlight", "Interpolate the active slot highlight smoothly across slots", true
        ));
        this.showOffhandSlotSetting = addSetting(new BooleanSetting(
                "show_offhand", "Show Offhand Slot", "Include the offhand slot to the left of the hotbar", true
        ));
        addAliases("hotbar", "quickbar", "slotbar");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int slotSize = 20;
        int pad = paddingSetting.getIntValue();
        int selectedSlot = client.player != null ? client.player.getInventory().getSelectedSlot() : 0;

        boolean hasOffhand = showOffhandSlotSetting.isEnabled()
                && (preview || (client.player != null && !client.player.getOffHandStack().isEmpty()));
        int offhandExtraW = hasOffhand ? (slotSize + 4) : 0;

        int boxW = 9 * slotSize + pad * 2 + offhandExtraW;
        int boxH = slotSize + pad * 2;

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

        int mainStartX = x + pad + offhandExtraW;
        float targetX = selectedSlot * slotSize;
        if (smoothSelectorSetting.isEnabled()) {
            animatedSlotX = MathUtils.lerp(animatedSlotX, targetX, 0.28f);
        } else {
            animatedSlotX = targetX;
        }

        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : ThemeTokens.DEFAULT_ACCENT_PRIMARY;

        // Draw Offhand Slot if active
        if (hasOffhand) {
            int ox = x + pad;
            int oy = y + pad;
            GlassRenderer.drawPillBadge(context, ox, oy, slotSize, slotSize, accent, false);
            if (client.player != null) {
                ItemStack off = client.player.getOffHandStack();
                context.drawItem(off, ox + 2, oy + 2);
                context.drawStackOverlay(client.textRenderer, off, ox + 2, oy + 2);
            }
        }

        // Draw Animated Active Slot Highlight
        int selX = mainStartX + Math.round(animatedSlotX);
        int selY = y + pad;
        GlassRenderer.drawPillBadge(context, selX, selY, slotSize, slotSize, accent, true);

        // Draw 9 Hotbar Items
        for (int i = 0; i < 9; i++) {
            int sx = mainStartX + i * slotSize;
            int sy = y + pad;
            if (client.player != null) {
                ItemStack stack = client.player.getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    context.drawItem(stack, sx + 2, sy + 2);
                    context.drawStackOverlay(client.textRenderer, stack, sx + 2, sy + 2);
                }
            }
        }
    }
}
