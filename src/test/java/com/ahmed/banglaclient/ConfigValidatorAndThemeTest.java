package com.ahmed.banglaclient;

import com.ahmed.banglaclient.config.ConfigValidator;
import com.ahmed.banglaclient.core.PerformancePreset;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeManager;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigValidatorAndThemeTest {

    @TempDir
    Path tempDir;

    @Test
    public void testCorruptedJsonRecoveryCreatesBackupAndDoesNotThrow() throws IOException {
        Path brokenFile = tempDir.resolve("modules.json");
        Files.writeString(brokenFile, "{ invalid json syntax !!!", StandardCharsets.UTF_8);

        JsonObject recovered = ConfigValidator.readSafeJsonObject(brokenFile);
        Assertions.assertNotNull(recovered);
        Assertions.assertEquals(0, recovered.size());

        Path backupFile = tempDir.resolve("modules.json.corrupt.bak");
        Assertions.assertTrue(Files.exists(backupFile), "Backup file should be created for corrupted JSON");
    }

    @Test
    public void testThemeManagerBuiltInThemesAndCycling() {
        ThemeManager tm = new ThemeManager();
        Assertions.assertEquals(2, tm.getAllThemes().size());

        Theme initial = tm.getActiveTheme();
        Assertions.assertEquals("red_theme", initial.getId());

        tm.cycleNextTheme();
        Assertions.assertNotEquals(initial.getId(), tm.getActiveTheme().getId());

        tm.setActiveThemeById("custom_theme");
        Assertions.assertEquals("Custom Theme", tm.getActiveTheme().getDisplayName());
    }

    @Test
    public void testPerformancePresetCycling() {
        PerformancePreset preset = PerformancePreset.FPS_BOOST;
        Assertions.assertFalse(preset.isBlurEnabled());
        Assertions.assertEquals(PerformancePreset.BALANCED, preset.next());
        Assertions.assertEquals(PerformancePreset.VISUAL_QUALITY, preset.next().next());
        Assertions.assertEquals(PerformancePreset.CUSTOM, preset.next().next().next());
        Assertions.assertEquals(PerformancePreset.FPS_BOOST, preset.next().next().next().next());
    }
}
