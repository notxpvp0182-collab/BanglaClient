package com.ahmed.banglaclient.livestream.ui.components;

import com.ahmed.banglaclient.livestream.ui.animation.Animation;
import com.ahmed.banglaclient.livestream.ui.render.UIRenderer;
import com.ahmed.banglaclient.livestream.ui.theme.ThemeManager;
import com.ahmed.banglaclient.livestream.ui.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class GlassToggle {
    private final String label;
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;
    private final Animation knobAnim;
    private final Animation hoverAnim;

    public GlassToggle(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
        this.label = label;
        this.getter = getter;
        this.setter = setter;
        boolean initial = getter.getAsBoolean();
        this.knobAnim = new Animation(initial ? 1.0f : 0.0f, 0.18f, Animation.Easing.EASE_OUT_CUBIC);
        this.hoverAnim = new Animation(0.0f, 0.14f, Animation.Easing.EASE_OUT_CUBIC);
    }

    public void render(DrawContext ctx, float x, float y, float w, float h, float mouseX, float mouseY, float dt) {
        ThemeTokens t = ThemeManager.get().tokens();
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        boolean state = getter.getAsBoolean();

        knobAnim.animateTo(state ? 1.0f : 0.0f);
        hoverAnim.animateTo(hovered ? 1.0f : 0.0f);
        float k = knobAnim.update(dt);
        float hv = hoverAnim.update(dt);

        int rowBg = UIRenderer.lerpColor(t.bgGlassWeak(), t.bgGlass(), hv);
        UIRenderer.drawRoundedRect(ctx, x, y, w, h, 7.5f, rowBg);
        UIRenderer.drawRoundedBorder(ctx, x, y, w, h, 7.5f, 1.0f,
            UIRenderer.lerpColor(t.borderBase(), t.borderActive(), Math.max(hv * 0.65f, k * 0.35f)));

        int dotCol = UIRenderer.lerpColor(t.textMuted(), t.accentActive(), k);
        UIRenderer.drawRoundedRect(ctx, x + 6.5f, y + (h - 5.0f) * 0.5f, 2.5f, 5.0f, 1.25f, dotCol);

        UIRenderer.drawText(ctx, label, x + 13.0f, y + (h - 8.0f) * 0.5f,
            state ? t.textPrimary() : t.textSecondary(), 1.0f, false, false);

        float pillW = 25.0f, pillH = 12.5f;
        float pillX = x + w - pillW - 7.5f;
        float pillY = y + (h - pillH) * 0.5f;

        int trackColor = UIRenderer.lerpColor(0xFF161922, t.accentPrimary(), k);
        UIRenderer.drawRoundedRect(ctx, pillX, pillY, pillW, pillH, pillH * 0.5f, trackColor);
        UIRenderer.drawRoundedBorder(ctx, pillX, pillY, pillW, pillH, pillH * 0.5f, 0.8f, t.borderBase());

        float knobSize = 9.0f;
        float knobX = pillX + 1.75f + (pillW - knobSize - 3.5f) * k;
        float knobY = pillY + (pillH - knobSize) * 0.5f;
        int knobColor = UIRenderer.lerpColor(0xFF8E95A5, 0xFF0A0C10, k);
        UIRenderer.drawRoundedRect(ctx, knobX, knobY, knobSize, knobSize, knobSize * 0.5f, knobColor);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, float x, float y, float w, float h) {
        if (button == 0 && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
            setter.accept(!getter.getAsBoolean());
            return true;
        }
        return false;
    }

    public String label() { return label; }
}
