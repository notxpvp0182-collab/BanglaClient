package com.ahmed.banglaclient.gui.components;

import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.ColorUtils;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;

/**
 * AMOLED Glass Module Card with Unique Per-Module Custom Icon, Small-Caps Font, Favorite Star, and Smooth Exit Animation.
 */
public final class ModuleCardWidget {
    private final Module module;
    private final Animation toggleAnim;
    private final Animation visibilityAnim = new Animation(1.0f, 180L, Easing.EASE_OUT_CUBIC);
    private boolean lastEnabledState;
    private boolean removingFromFilteredView = false;

    public ModuleCardWidget(Module module) {
        this.module = module;
        this.lastEnabledState = module.isEnabled();
        this.toggleAnim = new Animation(lastEnabledState ? 1.0f : 0.0f, 150L, Easing.EASE_OUT_CUBIC);
    }

    public void startExitAnimation() {
        this.removingFromFilteredView = true;
        this.visibilityAnim.snapTo(1.0f);
        this.visibilityAnim.animateTo(0.0f);
    }

    public void resetExitAnimation() {
        this.removingFromFilteredView = false;
        this.visibilityAnim.snapTo(1.0f);
    }

    public boolean isRemovingFromFilteredView() {
        return removingFromFilteredView;
    }

    public boolean isExitAnimationFinished() {
        return removingFromFilteredView && visibilityAnim.getValue() <= 0.04f;
    }

    public void render(
            DrawContext context,
            TextRenderer textRenderer,
            int x,
            int y,
            int width,
            int height,
            double mouseX,
            double mouseY,
            boolean compact
    ) {
        if (module.isEnabled() != lastEnabledState) {
            lastEnabledState = module.isEnabled();
            toggleAnim.animateTo(lastEnabledState ? 1.0f : 0.0f);
        }

        float vis = visibilityAnim.getValue();
        int animOffsetX = Math.round((1.0f - vis) * 18.0f);
        int drawX = x + animOffsetX;

        float en = toggleAnim.getValue();
        boolean hovered = !removingFromFilteredView && MathUtils.isHovered(mouseX, mouseY, drawX, y, width, height);
        Theme theme = GlassRenderer.getTheme();

        int surface = ColorUtils.lerpColor(theme.getGlassSurface(), theme.getGlassElevated(), en * 0.65f);
        surface = ColorUtils.multiplyAlpha(surface, vis);
        GlassRenderer.drawCustomGlassPanel(context, drawX, y, width, height, surface, hovered, module.isEnabled(), true);

        // 1. Unique Per-Module Custom Vector Icon
        int iconY = y + (height - 10) / 2;
        int iconCol = ColorUtils.multiplyAlpha(module.isEnabled() ? 0xFFFFFFFF : (hovered ? 0xFFFFFFFF : theme.getTextSecondary()), vis);
        GlassRenderer.drawModuleIcon(context, drawX + 6, iconY, 10, module.getId(), iconCol);

        // 2. Small-Caps Module Name
        int textX = drawX + 20;
        int maxTextW = Math.max(20, width - 74);
        String smallCapsName = GlassRenderer.toSmallCaps(module.getName());
        String name = textRenderer.trimToWidth(smallCapsName, maxTextW);
        int nameCol = ColorUtils.multiplyAlpha(module.isEnabled() ? 0xFFFFFFFF : (hovered ? 0xFFFFFFFF : theme.getTextSecondary()), vis);
        context.drawTextWithShadow(textRenderer, name, textX, y + (height - 8) / 2, nameCol);

        // 3. Favorite Star [★ / ☆]
        int starX = drawX + width - 48;
        int iconTextY = y + (height - 8) / 2;
        boolean starHov = MathUtils.isHovered(mouseX, mouseY, starX - 2, y + 2, 11, height - 4);
        int starCol = ColorUtils.multiplyAlpha(module.isFavorite() ? 0xFFFFFFFF : (starHov ? 0xFFE2E8F0 : theme.getTextMuted()), vis);
        context.drawTextWithShadow(textRenderer, module.isFavorite() ? "★" : "☆", starX, iconTextY, starCol);

        // 4. Settings Gear [⚙]
        int gearX = drawX + width - 36;
        boolean gearHov = MathUtils.isHovered(mouseX, mouseY, gearX - 2, y + 2, 11, height - 4);
        context.drawTextWithShadow(textRenderer, "⚙", gearX, iconTextY, ColorUtils.multiplyAlpha(gearHov ? 0xFFFFFFFF : theme.getTextMuted(), vis));

        // 5. High-Contrast AMOLED Pill Switch
        int swW = 20;
        int swH = 10;
        int swX = drawX + width - swW - 5;
        int swY = y + (height - swH) / 2;

        int trackCol = ColorUtils.multiplyAlpha(ColorUtils.lerpColor(0xFF07080B, 0x4CFFFFFF, en), vis);
        int borderCol = ColorUtils.multiplyAlpha(ColorUtils.lerpColor(0x38FFFFFF, 0xEEFFFFFF, en), vis);
        context.fill(swX + 1, swY, swX + swW - 1, swY + swH, trackCol);
        GlassRenderer.drawOutlineBorder(context, swX, swY, swW, swH, borderCol);

        int knobSize = 6;
        int knobX = swX + 2 + Math.round((swW - knobSize - 4) * en);
        int knobY = swY + 2;
        int knobCol = ColorUtils.multiplyAlpha(ColorUtils.lerpColor(0xFF788296, 0xFFFFFFFF, en), vis);
        context.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, knobCol);
    }

    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button,
            int x,
            int y,
            int width,
            int height,
            boolean compact,
            Consumer<Module> openSettingsCallback
    ) {
        if (removingFromFilteredView || !MathUtils.isHovered(mouseX, mouseY, x, y, width, height)) {
            return false;
        }
        if (button == 1) {
            GlassRenderer.playClickSound();
            if (openSettingsCallback != null) openSettingsCallback.accept(module);
            return true;
        }
        if (button == 0) {
            int starX = x + width - 48;
            if (MathUtils.isHovered(mouseX, mouseY, starX - 2, y, 12, height)) {
                GlassRenderer.playClickSound();
                module.toggleFavorite();
                return true;
            }
            int gearX = x + width - 36;
            if (MathUtils.isHovered(mouseX, mouseY, gearX - 2, y, 12, height)) {
                GlassRenderer.playClickSound();
                if (openSettingsCallback != null) openSettingsCallback.accept(module);
                return true;
            }
            GlassRenderer.playClickSound();
            module.toggle();
            return true;
        }
        return false;
    }

    public Module getModule() { return module; }
}
