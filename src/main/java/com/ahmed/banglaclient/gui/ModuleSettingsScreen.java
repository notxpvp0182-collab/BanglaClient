package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
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
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/**
 * Compact 2D Aura Module Settings Screen with smooth open & close animation.
 */
public final class ModuleSettingsScreen extends Screen {
    private final Screen parentScreen;
    private final Module module;
    private final SmoothScrollContainer scrollContainer = new SmoothScrollContainer();
    private final Animation openAnim = new Animation(0.0f, 150L, Easing.EASE_OUT_CUBIC);
    private boolean closing = false;

    private final Map<BooleanSetting, GlassToggleSwitch> booleanSwitches = new HashMap<>();
    private final Map<NumberSetting, GlassSliderWidget> numberSliders = new HashMap<>();
    private final Map<EnumSetting<?>, GlassDropdownWidget<?>> enumDropdowns = new HashMap<>();
    private final Map<ColorSetting, GlassColorPickerWidget> colorPickers = new HashMap<>();

    private boolean listeningForKeybind = false;
    private StringSetting focusedStringSetting = null;

    private int panelX, panelY, panelWidth, panelHeight;

    public ModuleSettingsScreen(Screen parentScreen, Module module) {
        super(Text.literal(module.getName()));
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
        this.panelWidth = Math.min(300, Math.max(220, (int) (this.width * 0.58f)));
        this.panelHeight = Math.min(195, Math.max(140, (int) (this.height * 0.64f)));
        this.panelX = (this.width - this.panelWidth) / 2;
        this.panelY = (this.height - this.panelHeight) / 2;
        this.closing = false;
        this.openAnim.snapTo(0.0f);
        this.openAnim.animateTo(1.0f);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float animVal = openAnim.getValue();
        if (closing && animVal <= 0.02f) {
            performActualClose();
            return;
        }

        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, animVal);
        Theme theme = GlassRenderer.getTheme();

        Matrix3x2fStack matrices = context.getMatrices();
        float scale = 0.94f + (0.06f * animVal);
        boolean scaling = Math.abs(scale - 1.0f) > 0.001f;
        if (scaling) {
            float cx = this.width * 0.5f;
            float cy = this.height * 0.5f;
            matrices.pushMatrix();
            matrices.translate(cx, cy);
            matrices.scale(scale, scale);
            matrices.translate(-cx, -cy);
        }

        try {
            GlassRenderer.drawGlassPanel(context, panelX, panelY, panelWidth, panelHeight, true, false, true);

            // Compact Aura Header: [←] | Module Name | [Reset]
            boolean backHover = MathUtils.isHovered(mouseX, mouseY, panelX + 6, panelY + 6, 42, 16);
            GlassRenderer.drawPillBadge(context, panelX + 6, panelY + 6, 42, 16, 0xFFFFFFFF, backHover);
            context.drawTextWithShadow(textRenderer, "← Back", panelX + 11, panelY + 10, 0xFFFFFFFF);

            context.drawTextWithShadow(textRenderer, module.getName(), panelX + 54, panelY + 10, 0xFFFFFFFF);

            int resetW = 46;
            int resetX = panelX + panelWidth - resetW - 6;
            boolean resetHover = MathUtils.isHovered(mouseX, mouseY, resetX, panelY + 6, resetW, 16);
            GlassRenderer.drawPillBadge(context, resetX, panelY + 6, resetW, 16, 0xFFFFFFFF, resetHover);
            context.drawTextWithShadow(textRenderer, "Reset", resetX + 10, panelY + 10, resetHover ? 0xFFFFFFFF : theme.getTextSecondary());

            GlassRenderer.drawHorizontalDivider(context, panelX + 6, panelY + 26, panelWidth - 12);

            int viewY = panelY + 30;
            int viewH = panelHeight - 35;
            scrollContainer.tickAndInterpolate();
            int contentHeight = calculateContentHeight();
            scrollContainer.updateDimensions(viewH, contentHeight);

            ClipRenderer.renderClipped(context, panelX + 4, viewY, panelWidth - 10, viewH, () -> {
                int rowY = Math.round(viewY - scrollContainer.getScrollOffset()) + 2;
                int rowX = panelX + 8;
                int rowW = panelWidth - 18;

                // Keybind Row
                GlassRenderer.drawGlassPanel(context, rowX, rowY, rowW, 22, false, false, listeningForKeybind);
                context.drawTextWithShadow(textRenderer, "Keybind", rowX + 8, rowY + 7, theme.getTextPrimary());
                String keyLabel = listeningForKeybind
                        ? "Press key..."
                        : (module.getKeyCode() <= 0 ? "NONE" : com.ahmed.banglaclient.input.KeybindManager.formatKeyName(module.getKeyCode()));
                int keyBtnW = textRenderer.getWidth(keyLabel) + 12;
                int keyBtnX = rowX + rowW - keyBtnW - 6;
                GlassRenderer.drawPillBadge(context, keyBtnX, rowY + 4, keyBtnW, 14, 0xFFFFFFFF, listeningForKeybind);
                context.drawTextWithShadow(textRenderer, keyLabel, keyBtnX + 6, rowY + 7, 0xFFFFFFFF);
                rowY += 26;

                for (Setting<?> setting : module.getSettings()) {
                    if (!setting.isVisible()) continue;
                    if (setting instanceof BooleanSetting bs) {
                        GlassRenderer.drawGlassPanel(context, rowX, rowY, rowW, 22, false, false, false);
                        context.drawTextWithShadow(textRenderer, bs.getName(), rowX + 8, rowY + 7, theme.getTextPrimary());
                        GlassToggleSwitch sw = booleanSwitches.get(bs);
                        int swX = rowX + rowW - GlassToggleSwitch.DEFAULT_WIDTH - 8;
                        sw.render(context, swX, rowY + 4, bs.isEnabled(),
                                MathUtils.isHovered(mouseX, mouseY, swX, rowY + 4, GlassToggleSwitch.DEFAULT_WIDTH, GlassToggleSwitch.DEFAULT_HEIGHT));
                        rowY += 25;
                    } else if (setting instanceof NumberSetting ns) {
                        GlassRenderer.drawGlassPanel(context, rowX, rowY, rowW, 26, false, false, false);
                        GlassSliderWidget slider = numberSliders.get(ns);
                        slider.render(context, textRenderer, rowX + 8, rowY + 3, rowW - 16, 20, mouseX, mouseY);
                        rowY += 29;
                    } else if (setting instanceof EnumSetting<?> es) {
                        GlassDropdownWidget<?> dd = enumDropdowns.get(es);
                        dd.render(context, textRenderer, rowX, rowY, rowW, mouseX, mouseY);
                        rowY += dd.getTotalHeight() + 4;
                    } else if (setting instanceof ColorSetting cs) {
                        GlassColorPickerWidget cp = colorPickers.get(cs);
                        cp.render(context, textRenderer, rowX, rowY, rowW, mouseX, mouseY);
                        rowY += cp.getTotalHeight() + 4;
                    } else if (setting instanceof StringSetting ss) {
                        boolean focused = (focusedStringSetting == ss);
                        GlassRenderer.drawGlassPanel(context, rowX, rowY, rowW, 24, false, false, focused);
                        context.drawTextWithShadow(textRenderer, ss.getName() + ":", rowX + 8, rowY + 8, theme.getTextPrimary());
                        String val = ss.getValue() + (focused && (System.currentTimeMillis() / 500L) % 2L == 0L ? "|" : "");
                        int valW = textRenderer.getWidth(val);
                        context.drawTextWithShadow(textRenderer, val, rowX + rowW - valW - 8, rowY + 8, focused ? 0xFFFFFFFF : theme.getTextSecondary());
                        rowY += 27;
                    }
                }
            });

            scrollContainer.renderScrollbar(context, panelX + panelWidth - 4, viewY, 2, viewH, mouseX, mouseY);
        } finally {
            if (scaling) {
                matrices.popMatrix();
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (closing) return false;
        if (button == 0 && MathUtils.isHovered(mouseX, mouseY, panelX + 6, panelY + 6, 42, 16)) {
            close();
            return true;
        }
        int resetW = 46;
        int resetX = panelX + panelWidth - resetW - 6;
        if (button == 0 && MathUtils.isHovered(mouseX, mouseY, resetX, panelY + 6, resetW, 16)) {
            module.resetSettingsToDefault();
            if (module instanceof HudModule hud) hud.resetPosition();
            return true;
        }

        int viewY = panelY + 30;
        int viewH = panelHeight - 35;
        if (scrollContainer.onMouseClicked(mouseX, mouseY, button, panelX + panelWidth - 5, viewY, 4, viewH)) {
            return true;
        }

        if (!MathUtils.isHovered(mouseX, mouseY, panelX + 4, viewY, panelWidth - 10, viewH)) {
            focusedStringSetting = null;
            return false;
        }

        int rowY = Math.round(viewY - scrollContainer.getScrollOffset()) + 2;
        int rowX = panelX + 8;
        int rowW = panelWidth - 18;

        if (MathUtils.isHovered(mouseX, mouseY, rowX, rowY, rowW, 22)) {
            if (button == 0) listeningForKeybind = !listeningForKeybind;
            else if (button == 1) { module.clearKeybind(); listeningForKeybind = false; }
            return true;
        }
        rowY += 26;

        focusedStringSetting = null;
        for (Setting<?> setting : module.getSettings()) {
            if (!setting.isVisible()) continue;
            if (setting instanceof BooleanSetting bs) {
                if (button == 0 && MathUtils.isHovered(mouseX, mouseY, rowX, rowY, rowW, 22)) {
                    bs.toggle();
                    return true;
                }
                rowY += 25;
            } else if (setting instanceof NumberSetting ns) {
                GlassSliderWidget slider = numberSliders.get(ns);
                if (slider.mouseClicked(mouseX, mouseY, button, rowX + 8, rowY + 3, rowW - 16, 20)) return true;
                rowY += 29;
            } else if (setting instanceof EnumSetting<?> es) {
                GlassDropdownWidget<?> dd = enumDropdowns.get(es);
                int h = dd.getTotalHeight();
                if (dd.mouseClicked(mouseX, mouseY, button, rowX, rowY, rowW)) return true;
                rowY += h + 4;
            } else if (setting instanceof ColorSetting cs) {
                GlassColorPickerWidget cp = colorPickers.get(cs);
                int h = cp.getTotalHeight();
                if (cp.mouseClicked(mouseX, mouseY, button, rowX, rowY, rowW)) return true;
                rowY += h + 4;
            } else if (setting instanceof StringSetting ss) {
                if (button == 0 && MathUtils.isHovered(mouseX, mouseY, rowX, rowY, rowW, 24)) {
                    focusedStringSetting = ss;
                    return true;
                }
                rowY += 27;
            }
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int viewH = panelHeight - 35;
        return scrollContainer.onMouseDragged(mouseX, mouseY, button, viewH);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        scrollContainer.onMouseReleased(button);
        for (GlassSliderWidget slider : numberSliders.values()) slider.mouseReleased(button);
        for (GlassColorPickerWidget picker : colorPickers.values()) picker.mouseReleased(button);
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int viewY = panelY + 30;
        int viewH = panelHeight - 35;
        if (scrollContainer.onMouseScrolled(mouseX, mouseY, verticalAmount, panelX, viewY, panelWidth, viewH)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

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
                if (!current.isEmpty()) focusedStringSetting.setValue(current.substring(0, current.length() - 1));
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

    public boolean charTyped(char chr, int modifiers) {
        if (focusedStringSetting != null && chr >= 32 && chr != 127) {
            focusedStringSetting.setValue(focusedStringSetting.getValue() + chr);
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        if (!closing) {
            closing = true;
            openAnim.animateTo(0.0f);
        }
    }

    private void performActualClose() {
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getConfigManager() != null) {
            BanglaClient.getInstance().getConfigManager().saveModulesConfig();
        }
        if (this.client != null) {
            this.client.setScreen(parentScreen);
        }
    }

    @Override public boolean mouseClicked(net.minecraft.client.gui.Click c, boolean d) { return mouseClicked(c.x(), c.y(), c.button()) || super.mouseClicked(c, d); }
    @Override public boolean mouseDragged(net.minecraft.client.gui.Click c, double dx, double dy) { return mouseDragged(c.x(), c.y(), c.button(), dx, dy) || super.mouseDragged(c, dx, dy); }
    @Override public boolean mouseReleased(net.minecraft.client.gui.Click c) { return mouseReleased(c.x(), c.y(), c.button()) || super.mouseReleased(c); }
    @Override public boolean keyPressed(net.minecraft.client.input.KeyInput k) { return keyPressed(k.key(), k.scancode(), k.modifiers()) || super.keyPressed(k); }
    @Override public boolean charTyped(net.minecraft.client.input.CharInput ch) { return charTyped((char) ch.codepoint(), ch.modifiers()) || super.charTyped(ch); }

    @Override
    public boolean shouldPause() { return false; }

    private int calculateContentHeight() {
        int total = 28;
        for (Setting<?> setting : module.getSettings()) {
            if (!setting.isVisible()) continue;
            if (setting instanceof BooleanSetting) total += 25;
            else if (setting instanceof NumberSetting) total += 29;
            else if (setting instanceof EnumSetting<?> es) total += enumDropdowns.get(es).getTotalHeight() + 4;
            else if (setting instanceof ColorSetting cs) total += colorPickers.get(cs).getTotalHeight() + 4;
            else if (setting instanceof StringSetting) total += 27;
        }
        return total + 8;
    }
}
