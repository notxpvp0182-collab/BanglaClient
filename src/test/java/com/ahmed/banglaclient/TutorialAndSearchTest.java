package com.ahmed.banglaclient;

import com.ahmed.banglaclient.gui.components.SmoothScrollContainer;
import com.ahmed.banglaclient.tutorial.TutorialManager;
import com.ahmed.banglaclient.tutorial.TutorialStep;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

/**
 * Automated JUnit 5 verification tests for the 14-step Tutorial Center and SmoothScrollContainer limits.
 */
public final class TutorialAndSearchTest {

    @TempDir
    Path tempDir;

    @Test
    public void testAll14TutorialStepsAndPersistence() {
        TutorialManager tm = new TutorialManager(tempDir);
        List<TutorialStep> steps = tm.getSteps();

        Assertions.assertEquals(14, steps.size(), "Tutorial Center must contain all 14 required sections");
        Assertions.assertEquals(0, tm.getCurrentStepIndex());
        Assertions.assertFalse(tm.isCompletedOnboarding());

        tm.nextStep();
        Assertions.assertEquals(1, tm.getCurrentStepIndex());

        // Reload from disk to verify persistence
        TutorialManager reloaded = new TutorialManager(tempDir);
        Assertions.assertEquals(1, reloaded.getCurrentStepIndex());

        reloaded.skipTutorial();
        Assertions.assertTrue(reloaded.isCompletedOnboarding());

        reloaded.restartTutorial();
        Assertions.assertEquals(0, reloaded.getCurrentStepIndex());
        Assertions.assertFalse(reloaded.isCompletedOnboarding());
    }

    @Test
    public void testSmoothScrollContainerBounds() {
        SmoothScrollContainer scroll = new SmoothScrollContainer();
        scroll.updateDimensions(100.0f, 350.0f);
        Assertions.assertTrue(scroll.isScrollable());

        boolean scrolled = scroll.onMouseScrolled(10, 10, -2.0, 0, 0, 200, 100);
        Assertions.assertTrue(scrolled);

        scroll.resetScroll();
        Assertions.assertEquals(0.0f, scroll.getScrollOffset(), 0.001f);
    }
}
