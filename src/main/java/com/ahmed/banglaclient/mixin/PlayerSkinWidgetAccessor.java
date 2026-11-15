package com.ahmed.banglaclient.mixin;

import net.minecraft.client.gui.widget.PlayerSkinWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerSkinWidget.class)
public interface PlayerSkinWidgetAccessor {
    @Accessor("yRotation")
    float banglaclient$getYRotation();

    @Accessor("yRotation")
    void banglaclient$setYRotation(float yRotation);
}
