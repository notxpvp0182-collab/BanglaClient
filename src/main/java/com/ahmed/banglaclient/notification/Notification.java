package com.ahmed.banglaclient.notification;

import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;

/**
 * Represents a single animated Liquid Glass toast notification.
 */
public final class Notification {
    private final String title;
    private final String message;
    private final NotificationType type;
    private final long createdAtMillis;
    private final long durationMillis;
    private final Animation slideAnimation;
    private boolean closingTriggered;

    public Notification(String title, String message, NotificationType type, long durationMillis) {
        this.title = title != null ? title : "BANGLA CLIENT";
        this.message = message != null ? message : "";
        this.type = type != null ? type : NotificationType.INFO;
        this.createdAtMillis = System.currentTimeMillis();
        this.durationMillis = Math.max(800L, durationMillis);
        this.slideAnimation = new Animation(0.0f, 220L, Easing.EASE_OUT_CUBIC);
        this.slideAnimation.animateTo(1.0f);
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getType() {
        return type;
    }

    public float getLifeProgress() {
        long elapsed = System.currentTimeMillis() - createdAtMillis;
        return Math.min(1.0f, Math.max(0.0f, (float) elapsed / (float) durationMillis));
    }

    public float getAnimationSlideFactor() {
        long elapsed = System.currentTimeMillis() - createdAtMillis;
        if (!closingTriggered && elapsed >= durationMillis - 220L) {
            closingTriggered = true;
            slideAnimation.animateTo(0.0f);
        }
        return slideAnimation.getValue();
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - createdAtMillis) >= durationMillis;
    }
}
