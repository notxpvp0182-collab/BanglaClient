package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.components.GlassColorPickerWidget;
import com.ahmed.banglaclient.gui.components.GlassDropdownWidget;
import com.ahmed.banglaclient.gui.components.GlassSliderWidget;
import com.ahmed.banglaclient.gui.components.GlassToggleSwitch;
import com.ahmed.banglaclient.gui.components.SmoothScrollContainer;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.ColorSetting;
import com.ahmed.banglaclient.module.setting.EnumSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.module.setting.Setting;
import com.ahmed.banglaclient.module.setting.StringSetting;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.ClipRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/**
 * Dedicated Liquid Glass configuration screen for an individual module's settings, keybind,
 * and reset actions.
 */
public final class ModuleSettingsScreen extends Screen {
    private final Screen parentScreen;
    private final Module module;
    private final SmoothScrollContainer scrollContainer = new SmoothScrollContainer();

    private final Map<BooleanSetting, GlassToggleSwitch> booleanSwitches = new HashMap<>();
    private final Map<NumberSetting, GlassSliderWidget> numberSliders = new HashMap<>();
    private final Map<EnumSetting<?>, GlassDropdownWidget<?>> enumDropdowns = new HashMap<>();
    private final Map<ColorSetting, GlassColorPickerWidget> colorPickers = new HashMap<>();

    private boolean listeningForKeybind = false;
    private StringSetting focusedStringSetting = null;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public ModuleSettingsScreen(Screen parentScreen, Module module) {
        super(Text.literal(module.getName() + " Settings"));
        this.parentScreen = parentScreen;
        this.module = module;

        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BooleanSetting bs) {
                booleanSwitches.put(bs, new GlassToggleSwitch(bs.isEnabled()));
            } else if (setting instanceof NumberSetting ns) {
                numberSliders.put(ns, new GlassSliderWidget(ns));
            } else if (setting instanceof EnumSetting<?> es) {
                enumDropdowns.put(es, createDropdown(es));
            } else if (setting instanceof ColorSetting cs) {
                colorPickers.put(cs, new GlassColorPickerWidget(cs));
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static GlassDropdownWidget<?> createDropdown(EnumSetting<?> setting) {
        return new GlassDropdownWidget(setting);
    }

    @Override
    protected void init() {
        this.panelWidth = Math.min(440, this.width - 40);
        this.panelHeight = Math.min(320, this.height - 40);
        this.panelX = (this.width - this.panelWidth) / 2;
        this.panelY = (this.height - this.panelHeight) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, 1.0f);
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : theme.getAccentPrimary();

        GlassRenderer.drawGlassPanel(context, panelX, panelY, panelWidth, panelHeight, true, false, true);

        // Header: [← Back] | Module Title | [Reset Defaults]
        boolean backHover = MathUtils.isHovered(mouseX, mouseY, panelX + 10, panelY + 10, 52, 18);
        GlassRenderer.drawPillBadge(context, panelX + 10, panelY + 10, 52, 18, backHover ? accent : theme.getTextSecondary(), backHover);
        context.drawTextWithShadow(textRenderer, "← Back", panelX + 18, panelY + 15, backHover ? accent : theme.getTextPrimary());

        context.drawTextWithShadow(textRenderer, module.getName(), panelX + 72, panelY + 11, theme.getTextPrimary());
        context.drawTextWithShadow(textRenderer, module.getDescription(), panelX + 72, panelY + 22, theme.getTextSecondary());

        int resetW = 86;
        int resetX = panelX + panelWidth - resetW - 12;
        boolean resetHover = MathUtils.isHovered(mouseX, mouseY, resetX, panelY + 10, resetW, 18);
        GlassRenderer.drawPillBadge(context, resetX, panelY + 10, resetW, 18, resetHover ? accent : theme.getTextSecondary(), resetHover);
        context.drawTextWithShadow(textRenderer, "Reset Defaults", resetX + 8, panelY + 15, resetHover ? accent : theme.getTextSecondary());

        GlassRenderer.drawHorizontalDivider(context, panelX + 10, panelY + 36, panelWidth - 20);

        // Scrollable settings viewport
        int viewY = panelY + 42;
        int viewH = panelHeight - 48;
        scrollContainer.tickAndInterpolate();
        int contentHeight = calculateContentHeight();
        scrollContainer.updateDimensions(viewH, contentHeight);

        ClipRenderer.renderClipped(context, panelX + 6, viewY, panelWidth - 14, viewH, () -> {
            int rowY = Math.round(viewY - scrollContainer.getScrollOffset()) + 4;
            int rowX = panelX + 14;
            int rowW = panelWidth - 32;

            // 1. Keybind Row
            GlassRenderer.drawGlassPanel(context, rowX, rowY, rowW, 26, false, false, listeningForKeybind);
            context.drawTextWithShadow(textRenderer, "Keybind Shortcut", rowX + 10, rowY + 9, theme.getTextPrimary());
            String keyLabel;
            if (listeningForKeybind) {
                keyLabel = "Press any key... (ESC to clear)";
            } else if (module.getKeyCode() == GLFW.GLFW_KEY_UNKNOWN || module.getKeyCode() <= 0) {
                keyLabel = "NONE";
            } else {
                keyLabel = com.ahmed.banglaclient.input.KeybindManager.formatKeyName(module.getKeyCode());
            }
            int keyBtnW = textRenderer.getWidth(keyLabel) + 14;
            int keyBtnX = rowX + rowW - keyBtnW - 8;
            GlassRenderer.drawPillBadge(context, keyBtnX, rowY + 5, keyBtnW, 16, accent, listeningForKeybind);
            context.drawTextWithShadow(textRenderer, keyLabel, keyBtnX + 7, rowY + 9, accent);
            rowY += 32;

            // 2. Dynamic Settings Rows
            for (Setting<?> setting : module.getSettings()) {
                if (!setting.isVisible()) {
                    continue;
                }
                if (setting instanceof BooleanSetting bs) {
                    GlassRenderer.drawGlassPanel(context, rowX, rowY, rowW, 26, false, false, false);
                    context.drawTextWithShadow(textRenderer, bs.getName(), rowX + 10, rowY + 9, theme.getTextPrimary());
                    GlassToggleSwitch sw = booleanSwitches.get(bs);
                    int swX = rowX + rowW - GlassToggleSwitch.DEFAULT_WIDTH - 10;
                    sw.render(context, swX, rowY + 6, bs.isEnabled(),
                            MathUtils.isHovered(mouseX, mouseY, swX, rowY + 6, GlassToggleSwitch.DEFAULT_WIDTH, GlassToggleSwitch.DEFAULT_HEIGHT));
                    rowY += 30;
                } else if (setting instanceof NumberSetting ns) {
                    GlassRenderer.drawGlassPanel(context, rowX, rowY, rowW, 30, false, false, false);
                    GlassSliderWidget slider = numberSliders.get(ns);
                    slider.render(context, textRenderer, rowX + 10, rowY + 4, rowW - 20, 22, mouseX, mouseY);
                    rowY += 34;
                } else if (setting instanceof EnumSetting<?> es) {
                    GlassDropdownWidget<?> dd = enumDropdowns.get(es);
                    dd.render(context, textRenderer, rowX, rowY, rowW, mouseX, mouseY);
                    rowY += dd.getTotalHeight() + 6;
                } else if (setting instanceof ColorSetting cs) {
                    GlassColorPickerWidget cp = colorPickers.get(cs);
                    cp.render(context, textRenderer, rowX, rowY, rowW, mouseX, mouseY);
                    rowY += cp.getTotalHeight() + 6;
                } else if (setting instanceof StringSetting ss) {
                    boolean focused = (focusedStringSetting == ss);
                    GlassRenderer.drawGlassPanel(context, rowX, rowY, rowW, 28, false, false, focused);
                    context.drawTextWithShadow(textRenderer, ss.getName() + ":", rowX + 10, rowY + 10, theme.getTextPrimary());
                    String val = ss.getValue() + (focused && (System.currentTimeMillis() / 500L) % 2L == 0L ? "|" : "");
                    int valW = textRenderer.getWidth(val);
                    context.drawTextWithShadow(textRenderer, val, rowX + rowW - valW - 10, rowY + 10, focused ? accent : theme.getTextSecondary());
                    rowY += 32;
                }
            }
        });

        scrollContainer.renderScrollbar(context, panelX + panelWidth - 6, viewY, 3, viewH, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Back button
        if (button == 0 && MathUtils.isHovered(mouseX, mouseY, panelX + 10, panelY + 10, 52, 18)) {
            close();
            return true;
        }

        // Reset Defaults button
        int resetW = 86;
        int resetX = panelX + panelWidth - resetW - 12;
        if (button == 0 && MathUtils.isHovered(mouseX, mouseY, resetX, panelY + 10, resetW, 18)) {
            module.resetSettingsToDefault();
            if (module instanceof HudModule hud) {
                hud.resetPosition();
            }
            return true;
        }

        int viewY = panelY + 42;
        int viewH = panelHeight - 48;
        if (scrollContainer.onMouseClicked(mouseX, mouseY, button, panelX + panelWidth - 6, viewY, 3, viewH)) {
            return true;
        }

        if (!MathUtils.isHovered(mouseX, mouseY, panelX + 6, viewY, panelWidth - 14, viewH)) {
            focusedStringSetting = null;
            return false;
        }

        int rowY = Math.round(viewY - scrollContainer.getScrollOffset()) + 4;
        int rowX = panelX + 14;
        int rowW = panelWidth - 32;

        // Keybind row click
        if (MathUtils.isHovered(mouseX, mouseY, rowX, rowY, rowW, 26)) {
            if (button == 0) {
                listeningForKeybind = !listeningForKeybind;
            } else if (button == 1) {
                module.clearKeybind();
                listeningForKeybind = false;
            }
            return true;
        }
        rowY += 32;

        focusedStringSetting = null;
        for (Setting<?> setting : module.getSettings()) {
            if (!setting.isVisible()) {
                continue;
            }
            if (setting instanceof BooleanSetting bs) {
                if (button == 0 && MathUtils.isHovered(mouseX, mouseY, rowX, rowY, rowW, 26)) {
                    bs.toggle();
                    return true;
                }
                rowY += 30;
            } else if (setting instanceof NumberSetting ns) {
                GlassSliderWidget slider = numberSliders.get(ns);
                if (slider.mouseClicked(mouseX, mouseY, button, rowX + 10, rowY + 4, rowW - 20, 22)) {
                    return true;
                }
                rowY += 34;
            } else if (setting instanceof EnumSetting<?> es) {
                GlassDropdownWidget<?> dd = enumDropdowns.get(es);
                int h = dd.getTotalHeight();
                if (dd.mouseClicked(mouseX, mouseY, button, rowX, rowY, rowW)) {
                    return true;
                }
                rowY += h + 6;
            } else if (setting instanceof ColorSetting cs) {
                GlassColorPickerWidget cp = colorPickers.get(cs);
                int h = cp.getTotalHeight();
                if (cp.mouseClicked(mouseX, mouseY, button, rowX, rowY, rowW)) {
                    return true;
                }
                rowY += h + 6;
            } else if (setting instanceof StringSetting ss) {
                if (button == 0 && MathUtils.isHovered(mouseX, mouseY, rowX, rowY, rowW, 28)) {
                    focusedStringSetting = ss;
                    return true;
                }
                rowY += 32;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int viewH = panelHeight - 48;
        if (scrollContainer.onMouseDragged(mouseX, mouseY, button, viewH)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        scrollContainer.onMouseReleased(button);
        for (GlassSliderWidget slider : numberSliders.values()) {
            slider.mouseReleased(button);
        }
        for (GlassColorPickerWidget picker : colorPickers.values()) {
            picker.mouseReleased(button);
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int viewY = panelY + 42;
        int viewH = panelHeight - 48;
        if (scrollContainer.onMouseScrolled(mouseX, mouseY, verticalAmount, panelX, viewY, panelWidth, viewH)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningForKeybind) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                module.clearKeybind();
            } else {
                module.setKeyCode(keyCode);
            }
            listeningForKeybind = false;
            if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getConfigManager() != null) {
                BanglaClient.getInstance().getConfigManager().saveKeybindsConfig();
            }
            return true;
        }

        if (focusedStringSetting != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                focusedStringSetting = null;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                String current = focusedStringSetting.getValue();
                if (!current.isEmpty()) {
                    focusedStringSetting.setValue(current.substring(0, current.length() - 1));
                }
                return true;
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (focusedStringSetting != null && chr >= 32 && chr != 127) {
            focusedStringSetting.setValue(focusedStringSetting.getValue() + chr);
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getConfigManager() != null) {
            BanglaClient.getInstance().getConfigManager().saveModulesConfig();
        }
        if (this.client != null) {
            this.client.setScreen(parentScreen);
        }
    }

    @Override
        @Override public boolean mouseClicked(net.minecraft.client.gui.Click c, boolean d) { return mouseClicked(c.x(), c.y(), c.button()) || super.mouseClicked(c, d); }
    @Override public boolean mouseDragged(net.minecraft.client.gui.Click c, double dx, double dy) { return mouseDragged(c.x(), c.y(), c.button(), dx, dy) || super.mouseDragged(c, dx, dy); }
    @Override public boolean mouseReleased(net.minecraft.client.gui.Click c) { return mouseReleased(c.x(), c.y(), c.button()) || super.mouseReleased(c); }
    @Override public boolean keyPressed(net.minecraft.client.input.KeyInput k) { return keyPressed(k.key(), k.scancode(), k.modifiers()) || super.keyPressed(k); }
    @Override public boolean charTyped(net.minecraft.client.input.CharInput ch) { return charTyped((char) ch.codepoint(), ch.modifiers()) || super.charTyped(ch); }
    public boolean shouldPause() {
        return false;
    }

    private int calculateContentHeight() {
        int total = 36;
        for (Setting<?> setting : module.getSettings()) {
            if (!setting.isVisible()) {
                continue;
            }
            if (setting instanceof BooleanSetting) {
                total += 30;
            } else if (setting instanceof NumberSetting) {
                total += 34;
            } else if (setting instanceof EnumSetting<?> es) {
                total += enumDropdowns.get(es).getTotalHeight() + 6;
            } else if (setting instanceof ColorSetting cs) {
                total += colorPickers.get(cs).getTotalHeight() + 6;
            } else if (setting instanceof StringSetting) {
                total += 32;
            }
        }
        return total + 12;
    }
}
