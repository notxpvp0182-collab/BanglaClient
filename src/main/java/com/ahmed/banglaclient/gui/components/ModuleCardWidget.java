package com.ahmed.banglaclient.gui.components;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.input.KeybindManager;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.ColorUtils;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

/**
 * PYES-structured, Aura-styled Module Card:
 * - Subtle top-left corner bracket (`┌`)
 * - Left monochrome vector icon in a frosted badge
 * - Crisp white title + wrapped 2-line soft gray description
 * - Top-right quick [★] Favorite and [⚙] Settings icons
 * - Bottom full-width Aura-styled ENABLED / DISABLED action bar with smooth transition
 */
public final class ModuleCardWidget {
    private final Module module;
    private final Animation hoverAnim = new Animation(0.0f, 140L, Easing.EASE_OUT_CUBIC);
    private final Animation enabledAnim;
    private boolean lastEnabledState;

    public ModuleCardWidget(Module module) {
        this.module = module;
        this.lastEnabledState = module.isEnabled();
        this.enabledAnim = new Animation(lastEnabledState ? 1.0f : 0.0f, 180L, Easing.EASE_OUT_CUBIC);
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
            enabledAnim.animateTo(lastEnabledState ? 1.0f : 0.0f);
        }

        boolean cardHovered = MathUtils.isHovered(mouseX, mouseY, x, y, width, height);
        hoverAnim.animateTo(cardHovered ? 1.0f : 0.0f);

        float hov = hoverAnim.getValue();
        float en = enabledAnim.getValue();

        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : theme.getAccentPrimary();

        // Card Base Surface (Aura Dark Frosted Charcoal)
        int surfaceColor = ColorUtils.lerpColor(theme.getGlassSurface(), theme.getGlassElevated(), hov * 0.65f + en * 0.35f);
        GlassRenderer.drawCustomGlassPanel(context, x, y, width, height, surfaceColor, cardHovered, module.isEnabled(), true);

        // Top-left PYES structural corner bracket in restrained Aura silver
        int bracketCol = ColorUtils.lerpColor(theme.getBorderSubtle(), accent, Math.max(hov * 0.6f, en));
        GlassRenderer.drawCornerBracket(context, x, y, 7, bracketCol);

        // Left Monochrome Vector Icon Box
        int iconBoxSize = compact ? 18 : 22;
        int iconBoxX = x + 8;
        int iconBoxY = y + 8;
        int iconBg = ColorUtils.withAlpha(accent, 0.06f + en * 0.08f);
        GlassRenderer.drawCustomGlassPanel(context, iconBoxX, iconBoxY, iconBoxSize, iconBoxSize, iconBg, false, module.isEnabled(), true);
        int iconColor = ColorUtils.lerpColor(theme.getTextSecondary(), theme.getTextPrimary(), en);
        GlassRenderer.drawVectorIcon(
                context,
                iconBoxX + (iconBoxSize - 10) / 2,
                iconBoxY + (iconBoxSize - 10) / 2,
                10,
                module.getCategory().name(),
                iconColor
        );

        // Module Name & Top-Right Controls ([★] [⚙])
        int textLeftX = iconBoxX + iconBoxSize + 7;
        int maxTitleW = Math.max(24, width - (textLeftX - x) - 30);
        String trimmedTitle = textRenderer.trimToWidth(module.getName(), maxTitleW);
        context.drawTextWithShadow(textRenderer, trimmedTitle, textLeftX, y + 8, theme.getTextPrimary());

        // Top-Right Settings [⚙] & Favorite [★]
        int gearX = x + width - 15;
        int gearY = y + 7;
        boolean gearHov = MathUtils.isHovered(mouseX, mouseY, gearX - 2, gearY - 2, 12, 12);
        context.drawTextWithShadow(textRenderer, "⚙", gearX, gearY,
                gearHov ? theme.getTextPrimary() : theme.getTextMuted());

        int starX = gearX - 11;
        boolean starHov = MathUtils.isHovered(mouseX, mouseY, starX - 2, gearY - 2, 11, 12);
        int starCol = module.isFavorite()
                ? ThemeTokens.STATUS_WARNING
                : (starHov ? theme.getTextPrimary() : theme.getTextMuted());
        context.drawTextWithShadow(textRenderer, module.isFavorite() ? "★" : "☆", starX, gearY, starCol);

        // Description (Up to 2 lines in standard mode, 1 line in compact mode)
        if (!compact) {
            int descMaxW = Math.max(30, width - (textLeftX - x) - 6);
            List<OrderedText> lines = textRenderer.wrapLines(Text.literal(module.getDescription()), descMaxW);
            int maxLines = Math.min(2, lines.size());
            for (int i = 0; i < maxLines; i++) {
                context.drawTextWithShadow(textRenderer, lines.get(i), textLeftX, y + 19 + (i * 9), theme.getTextSecondary());
            }
        }

        // Bottom Full-Width PYES Action Button styled in Restrained Aura Frosted Glass
        int btnH = compact ? 13 : 15;
        int btnX = x + 7;
        int btnY = y + height - btnH - 6;
        int btnW = width - 14;
        boolean btnHov = MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH);

        int disabledFill = ColorUtils.withAlpha(theme.getBackgroundSecondary(), 0.75f);
        int enabledFill = ColorUtils.withAlpha(accent, 0.22f);
        int buttonBg = ColorUtils.lerpColor(disabledFill, enabledFill, en);
        if (btnHov) {
            buttonBg = ColorUtils.scaleBrightness(buttonBg, 1.18f);
        }

        int buttonBorder = ColorUtils.lerpColor(
                theme.getBorderSubtle(),
                ColorUtils.withAlpha(accent, 0.62f),
                en
        );

        context.fill(btnX + 1, btnY, btnX + btnW - 1, btnY + btnH, buttonBg);
        context.fill(btnX, btnY + 1, btnX + 1, btnY + btnH - 1, buttonBg);
        context.fill(btnX + btnW - 1, btnY + 1, btnX + btnW, btnY + btnH - 1, buttonBg);
        GlassRenderer.drawOutlineBorder(context, btnX, btnY, btnW, btnH, buttonBorder);

        String stateLabel = module.isEnabled() ? "ENABLED" : "DISABLED";
        if (module.getKeyCode() != GLFW.GLFW_KEY_UNKNOWN && module.getKeyCode() > 0 && width >= 135) {
            stateLabel += " [" + KeybindManager.formatKeyName(module.getKeyCode()) + "]";
        }
        int lw = textRenderer.getWidth(stateLabel);
        int labelCol = ColorUtils.lerpColor(theme.getTextMuted(), theme.getTextPrimary(), en);
        context.drawTextWithShadow(textRenderer, stateLabel, btnX + (btnW - lw) / 2, btnY + (btnH - 8) / 2, labelCol);
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
            if (openSettingsCallback != null) {
                openSettingsCallback.accept(module);
            }
            return true;
        }

        if (button == 0) {
            int gearX = x + width - 15;
            int gearY = y + 7;
            if (MathUtils.isHovered(mouseX, mouseY, gearX - 2, gearY - 2, 13, 13)) {
                if (openSettingsCallback != null) {
                    openSettingsCallback.accept(module);
                }
                return true;
            }

            int starX = gearX - 11;
            if (MathUtils.isHovered(mouseX, mouseY, starX - 2, gearY - 2, 11, 13)) {
                module.toggleFavorite();
                return true;
            }

            module.toggle();
            return true;
        }

        return false;
    }

    public Module getModule() {
        return module;
    }
}
