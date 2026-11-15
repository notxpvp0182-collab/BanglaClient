package com.ahmed.banglaclient.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {
    protected HandledScreenMixin(Text title) {
        super(title);
    }
}
