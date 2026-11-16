package dev.ahmed.liquidglass.ui.components;

import dev.ahmed.liquidglass.ui.animation.Animation;
import dev.ahmed.liquidglass.ui.render.UIRenderer;
import dev.ahmed.liquidglass.ui.theme.ThemeManager;
import dev.ahmed.liquidglass.ui.theme.ThemeTokens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public final class GlassSlider {
    private final String label;
    private final double min, max, step;
    private final String suffix;
    private final DoubleSupplier getter;
    private final DoubleConsumer setter;
    private final Animation fillAnim;
    private final Animation hoverAnim;
    private boolean dragging = false;

    public GlassSlider(String label, double min, double max, double step, String suffix,
                       DoubleSupplier getter, DoubleConsumer setter) {
        this.label = label;
        this.min = min;
        this.max = max;
        this.step = step;
        this.suffix = suffix;
        this.getter = getter;
        this.setter = setter;
        float initialNorm = (float) ((getter.getAsDouble() - min) / Math.max(0.0001, max - min));
        this.fillAnim = new Animation(initialNorm, 0.10f, Animation.Easing.EASE_OUT_CUBIC);
        this.hoverAnim = new Animation(0.0f, 0.14f, Animation.Easing.EASE_OUT_CUBIC);
    }

    public void render(DrawContext ctx, float x, float y, float w, float h, float mouseX, float mouseY, float dt) {
        ThemeTokens t = ThemeManager.get().tokens();
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        double currentVal = getter.getAsDouble();
        float norm = (float) Math.max(0.0, Math.min(1.0, (currentVal - min) / Math.max(0.0001, max - min)));

        fillAnim.animateTo(norm);
        hoverAnim.animateTo((hovered || dragging) ? 1.0f : 0.0f);
        float fill = fillAnim.update(dt);
        float hv = hoverAnim.update(dt);

        UIRenderer.drawRoundedRect(ctx, x, y, w, h, 7.5f, UIRenderer.lerpColor(t.bgGlassWeak(), t.bgGlass(), hv));
        UIRenderer.drawRoundedBorder(ctx, x, y, w, h, 7.5f, 1.0f,
            UIRenderer.lerpColor(t.borderBase(), t.borderActive(), hv * 0.7f));

        UIRenderer.drawText(ctx, label, x + 9.0f, y + 4.0f, t.textSecondary(), 1.0f, false, false);
        String formatted = (step >= 1.0 ? String.valueOf(Math.round(currentVal)) : String.format(Locale.ROOT, "%.1f", currentVal)) + suffix;
        int valW = MinecraftClient.getInstance().textRenderer.getWidth(formatted);
        UIRenderer.drawText(ctx, formatted, x + w - valW - 9.0f, y + 4.0f, t.textPrimary(), 1.0f, false, false);

        float trackX = x + 9.0f, trackY = y + h - 7.0f, trackW = w - 18.0f;
        UIRenderer.drawRoundedRect(ctx, trackX, trackY, trackW, 3.5f, 1.75f, 0xFF151821);
        UIRenderer.drawRoundedRect(ctx, trackX, trackY, Math.max(3.5f, trackW * fill), 3.5f, 1.75f, t.accentPrimary());

        float thumbSize = 7.5f + hv * 1.0f;
        float thumbX = trackX + trackW * fill - thumbSize * 0.5f;
        float thumbY = trackY + 1.75f - thumbSize * 0.5f;
        if (hv > 0.05f) {
            UIRenderer.drawRoundedRect(ctx, thumbX - 1.5f, thumbY - 1.5f, thumbSize + 3.0f, thumbSize + 3.0f, (thumbSize + 3.0f) * 0.5f,
                UIRenderer.withAlpha(t.accentActive(), (int) (70 * hv)));
        }
        UIRenderer.drawRoundedRect(ctx, thumbX, thumbY, thumbSize, thumbSize, thumbSize * 0.5f, t.accentActive());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, float x, float y, float w, float h) {
        if (button == 0 && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
            dragging = true;
            updateFromMouse(mouseX, x + 9.0f, w - 18.0f);
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, float x, float w) {
        if (dragging) {
            updateFromMouse(mouseX, x + 9.0f, w - 18.0f);
            return true;
        }
        return false;
    }

    public void mouseReleased() { dragging = false; }

    private void updateFromMouse(double mouseX, float trackX, float trackW) {
        double ratio = Math.max(0.0, Math.min(1.0, (mouseX - trackX) / Math.max(1.0, trackW)));
        double raw = min + ratio * (max - min);
        if (step > 0) raw = Math.round(raw / step) * step;
        setter.accept(Math.max(min, Math.min(max, raw)));
    }
}
