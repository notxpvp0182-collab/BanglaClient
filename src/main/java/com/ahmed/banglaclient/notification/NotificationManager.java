package com.ahmed.banglaclient.notification;

import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Thread-safe manager and HUD renderer for BANGLA CLIENT toast notifications.
 */
public final class NotificationManager {
    private static final int MAX_ACTIVE_NOTIFICATIONS = 5;
    private static final int CARD_WIDTH = 210;
    private static final int CARD_HEIGHT = 38;

    private final ConcurrentLinkedDeque<Notification> activeNotifications = new ConcurrentLinkedDeque<>();
    private boolean enabled = true;

    public void push(String title, String message, NotificationType type, long durationMillis) {
        if (!enabled) {
            return;
        }
        while (activeNotifications.size() >= MAX_ACTIVE_NOTIFICATIONS) {
            activeNotifications.pollFirst();
        }
        activeNotifications.addLast(new Notification(title, message, type, durationMillis));
    }

    public void pushInfo(String title, String message) {
        push(title, message, NotificationType.INFO, 2800L);
    }

    public void pushSuccess(String title, String message) {
        push(title, message, NotificationType.SUCCESS, 2600L);
    }

    public void pushWarning(String title, String message) {
        push(title, message, NotificationType.WARNING, 3500L);
    }

    public void render(DrawContext context, int screenWidth, int screenHeight) {
        if (!enabled || activeNotifications.isEmpty() || context == null) {
            return;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc.textRenderer;
        Theme theme = GlassRenderer.getTheme();

        int baseY = screenHeight - CARD_HEIGHT - 14;
        Iterator<Notification> iterator = activeNotifications.descendingIterator();
        int index = 0;

        while (iterator.hasNext()) {
            Notification n = iterator.next();
            if (n.isExpired()) {
                activeNotifications.remove(n);
                continue;
            }

            float slide = n.getAnimationSlideFactor();
            int x = Math.round(screenWidth - (CARD_WIDTH + 12) * slide);
            int y = baseY - index * (CARD_HEIGHT + 6);

            GlassRenderer.drawGlassPanel(context, x, y, CARD_WIDTH, CARD_HEIGHT, true, false, false);

            // Left semantic accent bar
            int accent = n.getType().getAccentColor();
            context.fill(x + 1, y + 4, x + 4, y + CARD_HEIGHT - 4, accent);

            // Title & Message
            context.drawTextWithShadow(tr, n.getTitle(), x + 10, y + 7, theme.getTextPrimary());
            String msg = tr.trimToWidth(n.getMessage(), CARD_WIDTH - 18);
            context.drawTextWithShadow(tr, msg, x + 10, y + 20, theme.getTextSecondary());

            // Bottom life progress bar
            int progressWidth = Math.round((CARD_WIDTH - 4) * (1.0f - n.getLifeProgress()));
            if (progressWidth > 0) {
                context.fill(x + 2, y + CARD_HEIGHT - 2, x + 2 + progressWidth, y + CARD_HEIGHT - 1, accent);
            }

            index++;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getActiveCount() {
        return activeNotifications.size();
    }
}
