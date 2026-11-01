package com.ahmed.banglaclient;

import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.ColorSetting;
import com.ahmed.banglaclient.module.setting.EnumSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.module.setting.StringSetting;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Automated JUnit 5 verification tests for BANGLA CLIENT setting validation, clamping, and JSON serialization.
 */
public final class SettingSerializationTest {

    private enum SampleMode {
        ALPHA, BETA, GAMMA
    }

    @Test
    public void testBooleanSettingToggleAndJsonRoundTrip() {
        BooleanSetting setting = new BooleanSetting("test_bool", "Test Bool", "Desc", false);
        Assertions.assertFalse(setting.isEnabled());

        setting.toggle();
        Assertions.assertTrue(setting.isEnabled());

        JsonElement json = setting.toJson();
        setting.resetToDefault();
        Assertions.assertFalse(setting.isEnabled());

        setting.fromJson(json);
        Assertions.assertTrue(setting.isEnabled());
    }

    @Test
    public void testNumberSettingClampingAndStepSnapping() {
        NumberSetting slider = new NumberSetting("scale", "Scale", "Desc", 1.0, 0.5, 2.5, 0.25, "x");
        slider.setValue(99.0);
        Assertions.assertEquals(2.5, slider.getValue(), 0.001);

        slider.setValue(-5.0);
        Assertions.assertEquals(0.5, slider.getValue(), 0.001);

        slider.setValue(1.13);
        Assertions.assertEquals(1.25, slider.getValue(), 0.001);
    }

    @Test
    public void testEnumSettingCycleAndJsonRoundTrip() {
        EnumSetting<SampleMode> mode = new EnumSetting<>("mode", "Mode", "Desc", SampleMode.ALPHA);
        mode.cycleNext();
        Assertions.assertEquals(SampleMode.BETA, mode.getValue());

        JsonElement serialized = mode.toJson();
        mode.resetToDefault();
        Assertions.assertEquals(SampleMode.ALPHA, mode.getValue());

        mode.fromJson(serialized);
        Assertions.assertEquals(SampleMode.BETA, mode.getValue());
    }

    @Test
    public void testColorSettingAndStringSettingBounds() {
        ColorSetting color = new ColorSetting("tint", "Tint", "Desc", 0xCC38BDF8, true);
        Assertions.assertEquals(0xCC, color.getAlpha());
        Assertions.assertEquals(0x38, color.getRed());

        StringSetting str = new StringSetting("prefix", "Prefix", "Desc", "FPS", 5);
        str.setValue("123456789");
        Assertions.assertEquals("12345", str.getValue());
    }
}
