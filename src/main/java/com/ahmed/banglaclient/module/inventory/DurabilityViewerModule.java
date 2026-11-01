package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Module 50: Durability Viewer
 * Appends exact numeric and percentage durability to item tooltips via Fabric's `ItemTooltipCallback`
 * and displays a lowest-durability summary badge on the HUD.
 */
public final class DurabilityViewerModule extends HudModule {
    private final BooleanSetting appendToTooltipsSetting;
    private final BooleanSetting showHudSummarySetting;

    private static boolean tooltipHookRegistered = false;
    private static DurabilityViewerModule instance;

    public DurabilityViewerModule() {
        super(
                "durability_viewer",
                "Durability Viewer",
                "Appends exact durability numbers to item tooltips and shows a HUD durability summary.",
                ModuleCategory.INVENTORY,
                590.0f,
                40.0f,
                118.0f,
                20.0f,
                false
        );
        this.appendToTooltipsSetting = addSetting(new BooleanSetting(
                "tooltip_durability", "Append to Item Tooltips", "Show exact Durability: X / Y (Z%) on item hover tooltips", true
        ));
        this.showHudSummarySetting = addSetting(new BooleanSetting(
                "hud_summary", "Show Lowest Gear HUD", "Display the lowest durability percentage among equipped gear on HUD", true
        ));
        instance = this;
        registerTooltipHookOnce();
        addAliases("tooltips", "exactdurability", "gearhealth");
    }

    private static synchronized void registerTooltipHookOnce() {
        if (tooltipHookRegistered) {
            return;
        }
        tooltipHookRegistered = true;
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            if (instance != null && instance.isEnabled() && instance.appendToTooltipsSetting.isEnabled()) {
                if (stack != null && stack.isDamageable() && stack.getMaxDamage() > 0) {
                    int max = stack.getMaxDamage();
                    int rem = max - stack.getDamage();
                    int pct = Math.round((rem * 100.0f) / max);
                    Formatting color = pct <= 15 ? Formatting.RED : (pct <= 40 ? Formatting.YELLOW : Formatting.AQUA);
                    lines.add(Text.literal(String.format("Durability: %d / %d (%d%%)", rem, max, pct)).formatted(color));
                }
            }
        });
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        if (!showHudSummarySetting.isEnabled() && !preview) {
            return;
        }

        int lowestPct = 100;
        String lowestItemName = "All Gear";

        if (client.player != null) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack s = client.player.getEquippedStack(slot);
                if (!s.isEmpty() && s.isDamageable() && s.getMaxDamage() > 0) {
                    int rem = s.getMaxDamage() - s.getDamage();
                    int pct = Math.round((rem * 100.0f) / s.getMaxDamage());
                    if (pct <= lowestPct) {
                        lowestPct = pct;
                        lowestItemName = s.getName().getString();
                    }
                }
            }
        }

        String text = client.textRenderer.trimToWidth(lowestItemName, 76) + ": " + lowestPct + "%";
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

        int color = lowestPct <= 15
                ? ThemeTokens.STATUS_ERROR
                : (lowestPct <= 40 ? ThemeTokens.STATUS_WARNING : textColorSetting.getArgb());
        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, color);
    }
}
