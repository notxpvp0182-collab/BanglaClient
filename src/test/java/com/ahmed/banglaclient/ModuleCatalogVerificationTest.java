package com.ahmed.banglaclient;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class ModuleCatalogVerificationTest {

    private static final String[] ESSENTIAL_MODULE_CLASSES = {
            "com.ahmed.banglaclient.module.hud.FpsCounterModule",
            "com.ahmed.banglaclient.module.hud.CpsCounterModule",
            "com.ahmed.banglaclient.module.hud.KeystrokesModule",
            "com.ahmed.banglaclient.module.hud.CoordinatesModule",
            "com.ahmed.banglaclient.module.hud.PingDisplayModule",
            "com.ahmed.banglaclient.module.combat.ArmorStatusModule",
            "com.ahmed.banglaclient.module.combat.CrosshairCustomizerModule",
            "com.ahmed.banglaclient.module.visual.FullbrightModule",
            "com.ahmed.banglaclient.module.visual.ZoomModule",
            "com.ahmed.banglaclient.module.visual.CustomFogModule",
            "com.ahmed.banglaclient.module.visual.CustomSkyModule",
            "com.ahmed.banglaclient.module.visual.CustomWeatherModule",
            "com.ahmed.banglaclient.module.visual.ClearWaterModule",
            "com.ahmed.banglaclient.module.movement.ToggleSprintModule",
            "com.ahmed.banglaclient.module.utility.FastBlockPlacementModule",
            "com.ahmed.banglaclient.module.utility.LatencyOptimizerModule",
            "com.ahmed.banglaclient.module.world.SlimeChunkInfoModule"
    };

    @Test
    public void testEssentialModulesExistAndExtendModule() throws ClassNotFoundException {
        Assertions.assertEquals(10, ModuleCategory.values().length);
        for (String className : ESSENTIAL_MODULE_CLASSES) {
            Class<?> clazz = Class.forName(className, false, getClass().getClassLoader());
            Assertions.assertTrue(Module.class.isAssignableFrom(clazz),
                    className + " must extend com.ahmed.banglaclient.module.Module");
        }
    }
}
