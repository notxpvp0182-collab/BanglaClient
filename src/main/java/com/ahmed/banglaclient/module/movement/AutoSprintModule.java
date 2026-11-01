package com.ahmed.banglaclient.module.movement;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import net.minecraft.entity.effect.StatusEffects;

/**
 * Module 60: Auto Sprint
 * Automatically engages vanilla sprint when pressing the forward movement key and all vanilla
 * sprint conditions (hunger > 6, not sneaking, not using item, not blinded) are met.
 */
public final class AutoSprintModule extends Module {
    private final BooleanSetting allowInWaterSetting;

    public AutoSprintModule() {
        super(
                "auto_sprint",
                "Auto Sprint",
                "Automatically engages vanilla sprint while moving forward when conditions permit.",
                ModuleCategory.MOVEMENT
        );
        this.allowInWaterSetting = addSetting(new BooleanSetting(
                "swim_sprint", "Sprint While Swimming", "Allow automatic sprint swimming when submerged in water", true
        ));
        addAliases("autosprint", "sprint", "run");
    }

    @Override
    public void onTick() {
        if (client == null || client.player == null || client.options == null || client.currentScreen != null) {
            return;
        }
        if (!allowInWaterSetting.isEnabled() && client.player.isTouchingWater()) {
            return;
        }

        boolean forward = client.options.forwardKey.isPressed() && !client.options.backKey.isPressed();
        boolean validConditions = forward
                && !client.player.isSneaking()
                && !client.player.isUsingItem()
                && !client.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && !client.player.horizontalCollision
                && (client.player.getHungerManager().getFoodLevel() > 6 || client.player.getAbilities().allowFlying);

        if (validConditions && !client.player.isSprinting()) {
            client.player.setSprinting(true);
        }
    }
}
