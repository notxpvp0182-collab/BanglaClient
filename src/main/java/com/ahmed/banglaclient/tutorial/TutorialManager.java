package com.ahmed.banglaclient.tutorial;

import com.ahmed.banglaclient.config.ConfigManager;
import com.ahmed.banglaclient.config.ConfigValidator;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TutorialManager {
    private final Path tutorialFile;
    private final List<TutorialStep> steps = new ArrayList<>();
    private int currentStepIndex = 0;
    private boolean completedOnboarding = false;

    public TutorialManager(Path configDir) {
        this.tutorialFile = configDir != null ? configDir.resolve("tutorial.json") : null;
        steps.add(new TutorialStep(1, "welcome", "1. Welcome to BANGLA CLIENT", "Welcome to BANGLA CLIENT v1.0.0 (Developed by Ahmed).", new String[]{"120 Utility Modules", "Dark Liquid Glass UI"}, "Press Next to continue."));
        steps.add(new TutorialStep(2, "gui", "2. Opening Click GUI", "Press RIGHT SHIFT in-game to open the Click GUI.", new String[]{"Left-click toggles modules", "Right-click opens settings"}, "Default: RIGHT SHIFT"));
        steps.add(new TutorialStep(3, "categories", "3. Navigating Categories", "Use the left sidebar to browse 10 categories.", new String[]{"HUD, Combat, Visual, Inventory, Movement, World, Chat, Audio, Performance, Utility"}, "Scrollable sidebar"));
        steps.add(new TutorialStep(4, "search", "4. Searching Modules", "Use the top search bar to filter modules instantly.", new String[]{"Search by name, category, or alias"}, "Click 'x' to clear"));
        steps.add(new TutorialStep(5, "toggle", "5. Enabling Modules", "Click any module card to enable or disable it.", new String[]{"Star icon pins to Favorites"}, "Auto-saved"));
        steps.add(new TutorialStep(6, "settings", "6. Module Settings", "Right-click or click Options on any card.", new String[]{"Sliders, colors, dropdowns, and text"}, "Reset Defaults available"));
        steps.add(new TutorialStep(7, "keybinds", "7. Assigning Keybinds", "Bind any module to a keyboard key.", new String[]{"Press K to open Keybind Manager"}, "Conflict detection included"));
        steps.add(new TutorialStep(8, "hud_editor", "8. Using HUD Editor", "Press H in-game to open the HUD Editor.", new String[]{"Live preview of all active widgets"}, "Right-click widget for settings"));
        steps.add(new TutorialStep(9, "hud_move", "9. Moving HUD Widgets", "Drag widgets with mouse and scroll to scale.", new String[]{"Center and edge snap guides"}, "Reset All in top bar"));
        steps.add(new TutorialStep(10, "themes", "10. Changing Themes", "Select Themes in the Click GUI sidebar.", new String[]{"4 built-in Liquid Glass themes"}, "Instant theme switching"));
        steps.add(new TutorialStep(11, "configs", "11. Config Manager", "Save, load, and export profiles.", new String[]{"Stored in config/banglaclient/"}, "Clipboard import/export"));
        steps.add(new TutorialStep(12, "menu", "12. Main Menu Customization", "Switch between BANGLA CLIENT and Vanilla menu.", new String[]{"Preserves third-party mod buttons"}, "Top-right switcher"));
        steps.add(new TutorialStep(13, "performance", "13. Performance Settings", "Cycle LOW, BALANCED, and HIGH presets.", new String[]{"LOW disables blur for maximum FPS"}, "Bottom-left sidebar card"));
        steps.add(new TutorialStep(14, "troubleshooting", "14. Troubleshooting", "Reset configurations anytime from Settings.", new String[]{"Automatic backup for corrupted configs"}, "Developed by Ahmed"));
        loadProgress();
    }

    public List<TutorialStep> getSteps() { return Collections.unmodifiableList(steps); }
    public TutorialStep getCurrentStep() { return steps.get(currentStepIndex); }
    public int getCurrentStepIndex() { return currentStepIndex; }

    public void setCurrentStepIndex(int idx) {
        if (idx >= 0 && idx < steps.size()) {
            this.currentStepIndex = idx;
            saveProgress();
        }
    }

    public void nextStep() {
        if (currentStepIndex < steps.size() - 1) setCurrentStepIndex(currentStepIndex + 1);
        else { completedOnboarding = true; saveProgress(); }
    }

    public void previousStep() {
        if (currentStepIndex > 0) setCurrentStepIndex(currentStepIndex - 1);
    }

    public void restartTutorial() {
        currentStepIndex = 0;
        completedOnboarding = false;
        saveProgress();
    }

    public void skipTutorial() {
        completedOnboarding = true;
        saveProgress();
    }

    public boolean isCompletedOnboarding() { return completedOnboarding; }

    public void loadProgress() {
        if (tutorialFile == null) return;
        JsonObject root = ConfigValidator.readSafeJsonObject(tutorialFile);
        if (root.has("currentStepIndex")) this.currentStepIndex = root.get("currentStepIndex").getAsInt();
        if (root.has("completedOnboarding")) this.completedOnboarding = root.get("completedOnboarding").getAsBoolean();
    }

    public void saveProgress() {
        if (tutorialFile == null) return;
        JsonObject root = new JsonObject();
        root.addProperty("currentStepIndex", currentStepIndex);
        root.addProperty("completedOnboarding", completedOnboarding);
        ConfigManager.writeAtomically(tutorialFile, root);
    }
}
