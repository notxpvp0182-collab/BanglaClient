package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Module 02: CPS Counter
 * Displays left-click and right-click clicks per second using a 1000ms rolling time window.
 */
public final class CpsCounterModule extends HudModule {
    private static final long WINDOW_MILLIS = 1000L;

    private final BooleanSetting showRightClickSetting;
    private final Deque<Long> leftClickTimestamps = new ArrayDeque<>();
    private final Deque<Long> rightClickTimestamps = new ArrayDeque<>();

    private boolean wasLeftPressed = false;
    private boolean wasRightPressed = false;

    public CpsCounterModule() {
        super(
                "cps_counter",
                "CPS Counter",
                "Displays left-click and right-click clicks per second over a 1-second rolling window.",
                ModuleCategory.HUD,
                10.0f,
                34.0f,
                76.0f,
                20.0f,
                false
        );
        this.showRightClickSetting = addSetting(new BooleanSetting(
                "show_rmb", "Show Right-Click CPS", "Display both LMB and RMB clicks per second", true
        ));
        addAliases("clicks", "clickspeed", "lmb", "rmb");
    }

    /**
     * Records a mouse button press from either polling or mixin hook.
     */
    public void recordMouseClick(int button) {
        long now = System.currentTimeMillis();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            leftClickTimestamps.addLast(now);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            rightClickTimestamps.addLast(now);
        }
    }

    public int getLeftCps() {
        pruneOldClicks(leftClickTimestamps, System.currentTimeMillis());
        return leftClickTimestamps.size();
    }

    public int getRightCps() {
        pruneOldClicks(rightClickTimestamps, System.currentTimeMillis());
        return rightClickTimestamps.size();
    }

    private void pollHardwareButtons() {
        if (client == null || client.getWindow() == null || client.currentScreen != null) {
            wasLeftPressed = false;
            wasRightPressed = false;
            return;
        }
        long handle = client.getWindow().getHandle();
        boolean leftDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        if (leftDown && !wasLeftPressed) {
            recordMouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        }
        if (rightDown && !wasRightPressed) {
            recordMouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        }

        wasLeftPressed = leftDown;
        wasRightPressed = rightDown;
    }

    private static void pruneOldClicks(Deque<Long> deque, long now) {
        while (!deque.isEmpty() && (now - deque.peekFirst()) > WINDOW_MILLIS) {
            deque.pollFirst();
        }
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        pollHardwareButtons();

        int lmb = getLeftCps();
        int rmb = getRightCps();
        String text = showRightClickSetting.isEnabled()
                ? (lmb + " | " + rmb + " CPS")
                : (lmb + " CPS");

        int pad = paddingSetting.getIntValue();
        int textW = client.textRenderer.getWidth(text);
        int boxW = textW + pad * 2;
        int boxH = client.textRenderer.fontHeight + pad * 2;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, textColorSetting.getArgb());
    }
}
