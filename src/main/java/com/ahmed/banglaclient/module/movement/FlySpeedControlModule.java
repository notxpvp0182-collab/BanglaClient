package com.ahmed.banglaclient.module.movement;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class FlySpeedControlModule extends Module {
    private final NumberSetting flySpeedMultiplier;

    public FlySpeedControlModule() {
        super("fly_speed_control", "Fly Speed Control", "Customizes Creative and Spectator flight speed multiplier.", ModuleCategory.MOVEMENT);
        this.flySpeedMultiplier = addSetting(new NumberSetting("fly_speed", "Flight Speed Multiplier", "Multiplier for creative/spectator fly speed", 2.0, 0.5, 10.0, 0.25, "x"));
        addAliases("flyspeed", "flightspeed", "spectatorspeed");
    }

    @Override
    protected void onDisable() {
        if (client != null && client.player != null) {
            client.player.getAbilities().setFlySpeed(0.05f);
        }
    }

    @Override
    public void onTick() {
        if (client != null && client.player != null && (client.player.getAbilities().flying || client.player.isSpectator())) {
            client.player.getAbilities().setFlySpeed(0.05f * flySpeedMultiplier.getFloatValue());
        }
    }
}
