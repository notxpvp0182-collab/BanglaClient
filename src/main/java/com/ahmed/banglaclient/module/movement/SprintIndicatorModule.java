package com.ahmed.banglaclient.module.movement;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 61: Sprint Indicator
 * Displays the current sprint/sneak state badge (e.g., [Sprinting (Toggled)], [Sprinting (Key Held)], [Sneaking]).
 */
public final class SprintIndicatorModule extends HudModule {

    public SprintIndicatorModule() {
        super(
                "sprint_indicator",
                "Sprint Indicator",
                "Displays current sprint and sneak toggle state on the HUD.",
                ModuleCategory.MOVEMENT,
                10.0f,
                268.0f,
                114.0f,
                20.0f,
                false
        );
        addAliases("sprintstatus", "sprinting", "togglehud");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        String stateText = "Standing";
        if (client.player != null) {
            boolean toggleSprintOn = false;
            boolean toggleSneakOn = false;
            if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getModuleManager() != null) {
                var mm = BanglaClient.getInstance().getModuleManager();
                toggleSprintOn = mm.getModule(ToggleSprintModule.class).map(ToggleSprintModule::isEnabled).orElse(false)
                        || mm.getModule(AutoSprintModule.class).map(AutoSprintModule::isEnabled).orElse(false);
                toggleSneakOn = mm.getModule(ToggleSneakModule.class).map(ToggleSneakModule::isEnabled).orElse(false);
            }

            if (client.player.isSneaking()) {
                stateText = toggleSneakOn ? "[Sneaking (Toggled)]" : "[Sneaking (Key Held)]";
            } else if (client.player.isSprinting()) {
                stateText = toggleSprintOn ? "[Sprinting (Toggled)]" : "[Sprinting (Key Held)]";
            } else if (toggleSprintOn) {
                stateText = "[Sprint (Toggled)]";
            } else if (!preview) {
                return;
            }
        } else if (preview) {
            stateText = "[Sprinting (Toggled)]";
        }

        int pad = paddingSetting.getIntValue();
        int boxW = client.textRenderer.getWidth(stateText) + pad * 2;
        int boxH = client.textRenderer.fontHeight + pad * 2;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        context.drawTextWithShadow(client.textRenderer, stateText, x + pad, y + pad + 1, textColorSetting.getArgb());
    }
}
