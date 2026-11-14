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
 * Compact 2D Aura Module Card (NO description text, ultra-clean icon + title + settings gear + Aura switch).
 */
public final class ModuleCardWidget {
    private final Module module;
    private final Animation toggleAnim;
    private boolean lastEnabledState;

    public ModuleCardWidget(Module module) {
        this.module = module;
        this.lastEnabledState = module.isEnabled();
        this.toggleAnim = new Animation(lastEnabledState ? 1.0f : 0.0f, 150L, Easing.EASE_OUT_CUBIC);
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

        float en = toggleAnim.getValue();
        boolean hovered = MathUtils.isHovered(mouseX, mouseY, x, y, width, height);
        Theme theme = GlassRenderer.getTheme();

        int surface = ColorUtils.lerpColor(theme.getGlassSurface(), theme.getGlassElevated(), en * 0.6f);
        GlassRenderer.drawCustomGlassPanel(context, x, y, width, height, surface, hovered, module.isEnabled(), true);

        // 1. Left 2D Monochrome Vector Icon
        int iconY = y + (height - 10) / 2;
        int iconCol = module.isEnabled() ? 0xFFFFFFFF : (hovered ? theme.getTextPrimary() : theme.getTextSecondary());
        GlassRenderer.drawVectorIcon(context, x + 7, iconY, 10, module.getCategory().name(), iconCol);

        // 2. Module Name (No description!)
        int textX = x + 22;
        int maxTextW = Math.max(24, width - 64);
        String name = textRenderer.trimToWidth(module.getName(), maxTextW);
        int nameCol = module.isEnabled() ? 0xFFFFFFFF : (hovered ? theme.getTextPrimary() : theme.getTextSecondary());
        context.drawTextWithShadow(textRenderer, name, textX, y + (height - 8) / 2, nameCol);

        // 3. Settings Gear [⚙]
        int gearX = x + width - 36;
        int gearY = y + (height - 8) / 2;
        boolean gearHov = MathUtils.isHovered(mouseX, mouseY, gearX - 2, y + 2, 12, height - 4);
        context.drawTextWithShadow(textRenderer, "⚙", gearX, gearY, gearHov ? 0xFFFFFFFF : theme.getTextMuted());

        // 4. Compact 2D Aura Pill Switch (20x10)
        int swW = 20;
        int swH = 10;
        int swX = x + width - swW - 6;
        int swY = y + (height - swH) / 2;

        int trackCol = ColorUtils.lerpColor(0xFF0A0B0E, 0x44FFFFFF, en);
        int borderCol = ColorUtils.lerpColor(0x33FFFFFF, 0xCCFFFFFF, en);
        context.fill(swX + 1, swY, swX + swW - 1, swY + swH, trackCol);
        GlassRenderer.drawOutlineBorder(context, swX, swY, swW, swH, borderCol);

        int knobSize = 6;
        int knobX = swX + 2 + Math.round((swW - knobSize - 4) * en);
        int knobY = swY + 2;
        int knobCol = ColorUtils.lerpColor(0xFF64748B, 0xFFFFFFFF, en);
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
        if (!MathUtils.isHovered(mouseX, mouseY, x, y, width, height)) {
            return false;
        }
        if (button == 1) {
            if (openSettingsCallback != null) openSettingsCallback.accept(module);
            return true;
        }
        if (button == 0) {
            int gearX = x + width - 36;
            if (MathUtils.isHovered(mouseX, mouseY, gearX - 3, y, 14, height)) {
                if (openSettingsCallback != null) openSettingsCallback.accept(module);
                return true;
            }
            module.toggle();
            return true;
        }
        return false;
    }

    public Module getModule() { return module; }
}
