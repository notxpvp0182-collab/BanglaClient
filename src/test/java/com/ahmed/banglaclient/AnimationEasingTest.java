package com.ahmed.banglaclient;

import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.util.ColorUtils;
import com.ahmed.banglaclient.util.MathUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Automated JUnit 5 verification tests for BANGLA CLIENT mathematical easing, color interpolation,
 * and animation state transitions.
 */
public final class AnimationEasingTest {

    @Test
    public void testEasingBoundaryInvariants() {
        for (Easing easing : Easing.values()) {
            Assertions.assertEquals(0.0f, easing.apply(0.0f), 0.0001f, easing.name() + " at t=0");
            Assertions.assertEquals(1.0f, easing.apply(1.0f), 0.0001f, easing.name() + " at t=1");
        }
    }

    @Test
    public void testColorUtilsInterpolationAndAlpha() {
        int base = 0xFF38BDF8;
        int halfAlpha = ColorUtils.withAlpha(base, 0.5f);
        Assertions.assertEquals(128, ColorUtils.getAlpha(halfAlpha), 1);
        Assertions.assertEquals(0x38, ColorUtils.getRed(halfAlpha));

        int mid = ColorUtils.lerpColor(0xFF000000, 0xFFFFFFFF, 0.5f);
        Assertions.assertEquals(128, ColorUtils.getRed(mid), 1);
        Assertions.assertEquals(128, ColorUtils.getGreen(mid), 1);
        Assertions.assertEquals(128, ColorUtils.getBlue(mid), 1);
    }

    @Test
    public void testAnimationSnapAndTargetRedirection() {
        Animation anim = new Animation(0.0f, 150L, Easing.EASE_OUT_CUBIC);
        Assertions.assertEquals(0.0f, anim.getValue(), 0.001f);

        anim.snapTo(1.0f);
        Assertions.assertEquals(1.0f, anim.getValue(), 0.001f);
        Assertions.assertTrue(anim.isFinished());

        Assertions.assertEquals(5.0f, MathUtils.clamp(12.0f, 0.0f, 5.0f), 0.001f);
    }
}
