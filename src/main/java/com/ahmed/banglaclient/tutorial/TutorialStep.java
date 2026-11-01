package com.ahmed.banglaclient.tutorial;

public record TutorialStep(
        int stepNumber,
        String id,
        String title,
        String summary,
        String[] bulletInstructions,
        String interactiveTip
) {
}
