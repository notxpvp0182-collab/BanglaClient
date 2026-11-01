package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Module 17: Potion HUD
 * Displays active status effects, amplifier numerals, and remaining duration timers.
 */
public final class PotionHudModule extends HudModule {
    private final BooleanSetting colorByEffectSetting;

    public PotionHudModule() {
        super(
                "potion_hud",
                "Potion HUD",
                "Displays active potion effects, amplifier levels, and duration timers.",
                ModuleCategory.COMBAT,
                300.0f,
                92.0f,
                120.0f,
                48.0f,
                false
        );
        this.colorByEffectSetting = addSetting(new BooleanSetting(
                "effect_color", "Use Potion Color", "Color each effect name using its native potion tint", true
        ));
        addAliases("potions", "effects", "status", "buffs");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        List<StatusEffectInstance> effects = new ArrayList<>();
        if (client.player != null) {
            Collection<StatusEffectInstance> active = client.player.getStatusEffects();
            effects.addAll(active);
        }

        int pad = paddingSetting.getIntValue();
        int x = Math.round(getPosX());
        int y = Math.round(getPosY());
        Theme theme = GlassRenderer.getTheme();

        if (effects.isEmpty()) {
            if (!preview) {
                setWidth(110);
                setHeight(24);
                return;
            }
            String sample1 = "Speed II (01:24)";
            String sample2 = "Strength I (00:45)";
            int w = Math.max(client.textRenderer.getWidth(sample1), client.textRenderer.getWidth(sample2)) + pad * 2;
            int h = 24 + pad * 2;
            setWidth(w);
            setHeight(h);
            if (showBackgroundSetting.isEnabled()) {
                GlassRenderer.drawCustomGlassPanel(context, x, y, w, h, backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled());
            }
            context.drawTextWithShadow(client.textRenderer, sample1, x + pad, y + pad + 1, 0xFF38BDF8);
            context.drawTextWithShadow(client.textRenderer, sample2, x + pad, y + pad + 13, 0xFFF87171);
            return;
        }

        int maxLineW = 80;
        List<String> lines = new ArrayList<>(effects.size());
        List<Integer> colors = new ArrayList<>(effects.size());

        for (StatusEffectInstance instance : effects) {
            String effectName = instance.getEffectType().value().getName().getString();
            String numeral = toRomanNumeral(instance.getAmplifier() + 1);
            Text durationText = StatusEffectUtil.getDurationText(instance, 1.0f, 20.0f);
            String fullLine = effectName + (numeral.isEmpty() ? "" : " " + numeral) + " (" + durationText.getString() + ")";
            lines.add(fullLine);
            maxLineW = Math.max(maxLineW, client.textRenderer.getWidth(fullLine));

            int effectRgb = instance.getEffectType().value().getColor() | 0xFF000000;
            colors.add(colorByEffectSetting.isEnabled() ? effectRgb : textColorSetting.getArgb());
        }

        int boxW = maxLineW + pad * 2;
        int boxH = lines.size() * 12 + pad * 2 - 2;
        setWidth(boxW);
        setHeight(boxH);

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        int drawY = y + pad + 1;
        for (int i = 0; i < lines.size(); i++) {
            context.drawTextWithShadow(client.textRenderer, lines.get(i), x + pad, drawY, colors.get(i));
            drawY += 12;
        }
    }

    private static String toRomanNumeral(int level) {
        if (level <= 1) {
            return "";
        }
        return switch (level) {
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(level);
        };
    }
}
