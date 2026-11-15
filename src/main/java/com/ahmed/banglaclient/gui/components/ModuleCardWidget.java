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
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

/**
 * PYES-Structured Module Card in AMOLED Aura Style:
 * - Left Framed Unique Custom Module Icon
 * - Module Name (NO default inline description)
 * - Top-Right [?] Info Button (click to toggle reading the module's description!), [★] Favorite, [⚙] Settings
 * - Bottom Full-Width ENABLED / DISABLED Action Bar
 */
public final class ModuleCardWidget {
    private final Module module;
    private final Animation toggleAnim;
    private final Animation visibilityAnim = new Animation(1.0f, 180L, Easing.EASE_OUT_CUBIC);
    private boolean lastEnabledState;
    private boolean removingFromFilteredView = false;
    private boolean showingDescription = false;

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

    public boolean isRemovingFromFilteredView() { return removingFromFilteredView; }
    public boolean isExitAnimationFinished() { return removingFromFilteredView && visibilityAnim.getValue() <= 0.04f; }

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
        int drawX = x + Math.round((1.0f - vis) * 16.0f);
        float en = toggleAnim.getValue();
        boolean hovered = !removingFromFilteredView && MathUtils.isHovered(mouseX, mouseY, drawX, y, width, height);
        Theme theme = GlassRenderer.getTheme();

        int surface = ColorUtils.multiplyAlpha(ColorUtils.lerpColor(theme.getGlassSurface(), theme.getGlassElevated(), en * 0.65f), vis);
        GlassRenderer.drawCustomGlassPanel(context, drawX, y, width, height, surface, hovered, module.isEnabled(), true);

        // PYES-style top-left corner bracket (`┌`)
        int bracketCol = ColorUtils.multiplyAlpha(module.isEnabled() ? 0xFFFFFFFF : 0x55FFFFFF, vis);
        context.fill(drawX, y, drawX + 6, y + 1, bracketCol);
        context.fill(drawX, y + 1, drawX + 1, y + 6, bracketCol);

        // Top-right controls: [?] Info, [★] Fav, [⚙] Settings
        int gearX = drawX + width - 13;
        int starX = gearX - 11;
        int infoX = starX - 11;
        int topIconsY = y + 5;

        boolean infoHov = MathUtils.isHovered(mouseX, mouseY, infoX - 1, topIconsY - 1, 10, 10);
        context.drawTextWithShadow(textRenderer, "?", infoX + 1, topIconsY,
                ColorUtils.multiplyAlpha(showingDescription || infoHov ? 0xFFFFFFFF : theme.getTextMuted(), vis));

        boolean starHov = MathUtils.isHovered(mouseX, mouseY, starX - 1, topIconsY - 1, 10, 10);
        context.drawTextWithShadow(textRenderer, module.isFavorite() ? "★" : "☆", starX, topIconsY,
                ColorUtils.multiplyAlpha(module.isFavorite() || starHov ? 0xFFFFFFFF : theme.getTextMuted(), vis));

        boolean gearHov = MathUtils.isHovered(mouseX, mouseY, gearX - 1, topIconsY - 1, 10, 10);
        context.drawTextWithShadow(textRenderer, "⚙", gearX, topIconsY,
                ColorUtils.multiplyAlpha(gearHov ? 0xFFFFFFFF : theme.getTextMuted(), vis));

        if (showingDescription) {
            // Show module description when [?] is toggled ON
            List<OrderedText> lines = textRenderer.wrapLines(Text.literal(module.getDescription()), width - 12);
            int maxL = Math.min(2, lines.size());
            for (int i = 0; i < maxL; i++) {
                context.drawTextWithShadow(textRenderer, lines.get(i), drawX + 6, y + 15 + i * 9,
                        ColorUtils.multiplyAlpha(0xFFE2E8F0, vis));
            }
        } else {
            // PYES-style Framed Icon Box + Module Title
            int boxSize = 18;
            int boxX = drawX + 6;
            int boxY = y + 6;
            GlassRenderer.drawCustomGlassPanel(context, boxX, boxY, boxSize, boxSize, ColorUtils.multiplyAlpha(0xFF06070A, vis), false, module.isEnabled(), true);
            int iconCol = ColorUtils.multiplyAlpha(module.isEnabled() ? 0xFFFFFFFF : (hovered ? 0xFFFFFFFF : theme.getTextSecondary()), vis);
            GlassRenderer.drawModuleIcon(context, boxX + 4, boxY + 4, 10, module.getId(), iconCol);

            int textX = boxX + boxSize + 6;
            int maxTextW = Math.max(20, infoX - textX - 3);
            String smallCapsName = GlassRenderer.toSmallCaps(module.getName());
            String trimmed = textRenderer.trimToWidth(smallCapsName, maxTextW);
            int nameCol = ColorUtils.multiplyAlpha(module.isEnabled() ? 0xFFFFFFFF : (hovered ? 0xFFFFFFFF : theme.getTextSecondary()), vis);
            context.drawTextWithShadow(textRenderer, trimmed, textX, boxY + 5, nameCol);
        }

        // PYES-style Bottom Full-Width ENABLED / DISABLED Bar
        int btnH = 12;
        int btnX = drawX + 6;
        int btnY = y + height - btnH - 5;
        int btnW = width - 12;

        int barFill = ColorUtils.multiplyAlpha(ColorUtils.lerpColor(0xFF07080B, 0x38FFFFFF, en), vis);
        int barBorder = ColorUtils.multiplyAlpha(ColorUtils.lerpColor(0x28FFFFFF, 0xCCFFFFFF, en), vis);
        context.fill(btnX + 1, btnY, btnX + btnW - 1, btnY + btnH, barFill);
        GlassRenderer.drawOutlineBorder(context, btnX, btnY, btnW, btnH, barBorder);

        String statusStr = GlassRenderer.toSmallCaps(module.isEnabled() ? "ENABLED" : "DISABLED");
        int sw = textRenderer.getWidth(statusStr);
        int statusCol = ColorUtils.multiplyAlpha(module.isEnabled() ? 0xFFFFFFFF : theme.getTextMuted(), vis);
        context.drawTextWithShadow(textRenderer, statusStr, btnX + (btnW - sw) / 2, btnY + 2, statusCol);
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
            int gearX = x + width - 13;
            int starX = gearX - 11;
            int infoX = starX - 11;
            int topIconsY = y + 5;

            if (MathUtils.isHovered(mouseX, mouseY, infoX - 2, topIconsY - 2, 11, 12)) {
                GlassRenderer.playClickSound();
                showingDescription = !showingDescription;
                return true;
            }
            if (MathUtils.isHovered(mouseX, mouseY, starX - 2, topIconsY - 2, 11, 12)) {
                GlassRenderer.playClickSound();
                module.toggleFavorite();
                return true;
            }
            if (MathUtils.isHovered(mouseX, mouseY, gearX - 2, topIconsY - 2, 11, 12)) {
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
