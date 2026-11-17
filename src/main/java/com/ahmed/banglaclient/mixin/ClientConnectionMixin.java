package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.utility.LatencyOptimizerModule;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), require = 0)
    private void banglaclient$countTxPacket(net.minecraft.network.packet.Packet<?> packet, CallbackInfo ci) {
        com.ahmed.banglaclient.performance.PerformanceEngine.get().recordOutgoingPacket();
    }
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), require = 0)
    private void banglaclient$countRxPacket(ChannelHandlerContext ctx, net.minecraft.network.packet.Packet<?> packet, CallbackInfo ci) {
        com.ahmed.banglaclient.performance.PerformanceEngine.get().recordIncomingPacket();
    }

    @Shadow private Channel channel;

    @Inject(method = "channelActive", at = @At("TAIL"), require = 0)
    private void banglaclient$configureTcpNoDelay(ChannelHandlerContext context, CallbackInfo ci) {
        try {
            if (this.channel != null) {
                this.channel.config().setOption(ChannelOption.TCP_NODELAY, true);
            }
        } catch (Throwable ignored) {}
    }

    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void banglaclient$flushPacketQueue(CallbackInfo ci) {
        if (this.channel == null || !this.channel.isOpen()) return;
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var latOpt = bangla.getModuleManager().getModule(LatencyOptimizerModule.class);
            if (latOpt.isPresent() && latOpt.get().isOptimizePacketQueue()) {
                this.channel.flush();
            }
        }
    }
}
