package com.ahmed.banglaclient.gui.components;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * Renders an individual Bento-style Liquid Glass module card inside the Click GUI.
 * Supports:
 * - Left-click on card or toggle switch -> Toggles module ON/OFF
 * - Right-click on card OR Left-click on dedicated [⚙] button -> Opens ModuleSettingsScreen
 * - Left-click on [★] button -> Toggles Favorite status
 */
public final class ModuleCardWidget {
    private final Module module;
    private final GlassToggleSwitch toggleSwitch;

    public ModuleCardWidget(Module module) {
        this.module = module;
        this.toggleSwitch = new GlassToggleSwitch(module.isEnabled());
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
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : theme.getAccentPrimary();

        boolean cardHovered = MathUtils.isHovered(mouseX, mouseY, x, y, width, height);
        GlassRenderer.drawGlassPanel(context, x, y, width, height, true, cardHovered, module.isEnabled());

        // 1. Module Name & Favorite Star [★]
        int titleY = y + (compact ? 7 : 9);
        context.drawTextWithShadow(textRenderer, module.getName(), x + 10, titleY, theme.getTextPrimary());

        int starX = x + width - GlassToggleSwitch.DEFAULT_WIDTH - 24;
        boolean starHovered = MathUtils.isHovered(mouseX, mouseY, starX - 2, titleY - 2, 12, 12);
        int starColor = module.isFavorite()
                ? ThemeTokens.STATUS_WARNING
                : (starHovered ? theme.getTextPrimary() : theme.getTextMuted());
        context.drawTextWithShadow(textRenderer, module.isFavorite() ? "★" : "☆", starX, titleY, starColor);

        // 2. ON/OFF Toggle Switch (Top-Right)
        int switchX = x + width - GlassToggleSwitch.DEFAULT_WIDTH - 8;
        int switchY = y + (compact ? 5 : 7);
        boolean switchHovered = MathUtils.isHovered(mouseX, mouseY, switchX, switchY,
                GlassToggleSwitch.DEFAULT_WIDTH, GlassToggleSwitch.DEFAULT_HEIGHT);
        toggleSwitch.render(context, switchX, switchY, module.isEnabled(), switchHovered);

        // 3. Description Text
        if (!compact) {
            String desc = textRenderer.trimToWidth(module.getDescription(), width - 20);
            context.drawTextWithShadow(textRenderer, desc, x + 10, y + 23, theme.getTextSecondary());
        }

        // 4. Bottom Row: Category Pill, Keybind Badge, and Accessible Settings [⚙ Config] Button
        int footerY = y + height - 16;

        // Category pill
        String catText = module.getCategory().getSidebarLabel();
        int catW = textRenderer.getWidth(catText) + 10;
        GlassRenderer.drawPillBadge(context, x + 10, footerY, catW, 12, theme.getTextSecondary(), false);
        context.drawTextWithShadow(textRenderer, catText, x + 15, footerY + 2, theme.getTextMuted());

        // Keybind indicator badge (if bound)
        int nextBadgeX = x + 10 + catW + 5;
        if (module.getKeyCode() != GLFW.GLFW_KEY_UNKNOWN && module.getKeyCode() > 0) {
            String keyName = InputUtil.fromKeyCode(module.getKeyCode(), 0).getLocalizedText().getString().toUpperCase();
            int keyW = textRenderer.getWidth(keyName) + 10;
            GlassRenderer.drawPillBadge(context, nextBadgeX, footerY, keyW, 12, accent, true);
            context.drawTextWithShadow(textRenderer, keyName, nextBadgeX + 5, footerY + 2, accent);
        }

        // Dedicated Accessible Settings Button (Left-clickable in addition to Right-click anywhere)
        String cfgLabel = "⚙ Options";
        int cfgW = textRenderer.getWidth(cfgLabel) + 10;
        int cfgX = x + width - cfgW - 8;
        boolean cfgHovered = MathUtils.isHovered(mouseX, mouseY, cfgX, footerY, cfgW, 12);
        GlassRenderer.drawPillBadge(context, cfgX, footerY, cfgW, 12, cfgHovered ? accent : theme.getTextSecondary(), cfgHovered);
        context.drawTextWithShadow(textRenderer, cfgLabel, cfgX + 5, footerY + 2,
                cfgHovered ? accent : theme.getTextSecondary());
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

        // Right-click anywhere on the card opens the module's settings screen
        if (button == 1) {
            if (openSettingsCallback != null) {
                openSettingsCallback.accept(module);
            }
            return true;
        }

        if (button == 0) {
            int titleY = y + (compact ? 7 : 9);
            int starX = x + width - GlassToggleSwitch.DEFAULT_WIDTH - 24;

            // 1. Check Favorite Star click
            if (MathUtils.isHovered(mouseX, mouseY, starX - 2, titleY - 2, 12, 12)) {
                module.toggleFavorite();
                return true;
            }

            // 2. Check Accessible [⚙ Options] button click
            int footerY = y + height - 16;
            int cfgW = 52;
            int cfgX = x + width - cfgW - 8;
            if (MathUtils.isHovered(mouseX, mouseY, cfgX, footerY, cfgW, 13)) {
                if (openSettingsCallback != null) {
                    openSettingsCallback.accept(module);
                }
                return true;
            }

            // 3. Left-click anywhere else on the card toggles the module ON/OFF
            module.toggle();
            return true;
        }

        return false;
    }

    public Module getModule() {
        return module;
    }
}
