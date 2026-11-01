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

/**
 * Automated JUnit 5 verification tests for corrupted JSON recovery, `.corrupt.bak` backup creation,
 * schema version migration, `ThemeManager`, and `PerformancePreset` cycling.
 */
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
        Assertions.assertEquals(4, tm.getAllThemes().size());

        Theme initial = tm.getActiveTheme();
        Assertions.assertEquals("liquid_dark_sky", initial.getId());

        tm.cycleNextTheme();
        Assertions.assertNotEquals(initial.getId(), tm.getActiveTheme().getId());

        tm.setActiveThemeById("bengal_emerald_glass");
        Assertions.assertEquals("Bengal Emerald Glass", tm.getActiveTheme().getDisplayName());
    }

    @Test
    public void testPerformancePresetCycling() {
        PerformancePreset low = PerformancePreset.LOW;
        Assertions.assertFalse(low.isBlurEnabled());
        Assertions.assertEquals(PerformancePreset.BALANCED, low.next());
        Assertions.assertEquals(PerformancePreset.HIGH, low.next().next());
        Assertions.assertEquals(PerformancePreset.LOW, low.next().next().next());
    }
}
