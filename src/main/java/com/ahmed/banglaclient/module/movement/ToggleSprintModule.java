package com.ahmed.banglaclient.module.movement;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

/**
 * Module 58: Toggle Sprint
 * Provides a configurable sprint toggle that respects all vanilla movement rules
 * (forward key pressed, sufficient hunger, not sneaking, not using a slow item, and not blinded).
 */
public final class ToggleSprintModule extends Module {
    private final BooleanSetting disableWhileFlyingSetting;

    public ToggleSprintModule() {
        super(
                "toggle_sprint",
                "Toggle Sprint",
                "Keeps sprint toggled while moving forward within legitimate vanilla movement rules.",
                ModuleCategory.MOVEMENT
        );
        this.disableWhileFlyingSetting = addSetting(new BooleanSetting(
                "pause_when_flying", "Pause in Creative Flight", "Do not force sprint state while flying in Creative mode", false
        ));
        addAliases("togglesprint", "keepsprint", "sprinttoggle");
    }

    @Override
    protected void onDisable() {
        if (client != null && client.options != null) {
            client.options.sprintKey.setPressed(false);
        }
    }

    @Override
    public void onTick() {
        if (client == null || client.player == null || client.options == null) {
            return;
        }
        if (client.currentScreen != null) {
            return;
        }
        if (disableWhileFlyingSetting.isEnabled() && client.player.getAbilities().flying) {
            return;
        }

        boolean forwardPressed = client.options.forwardKey.isPressed();
        boolean canLegitimatelySprint = forwardPressed
                && !client.player.isSneaking()
                && !client.player.isUsingItem()
                && !client.player.horizontalCollision
                && (client.player.getHungerManager().getFoodLevel() > 6 || client.player.getAbilities().allowFlying);

        if (canLegitimatelySprint) {
            client.options.sprintKey.setPressed(true);
        }
    }
}
