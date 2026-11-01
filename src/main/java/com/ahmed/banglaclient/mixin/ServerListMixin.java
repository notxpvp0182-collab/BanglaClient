package com.ahmed.banglaclient.mixin;

import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

@Mixin(ServerList.class)
public abstract class ServerListMixin {

    @Unique
    private static final String BANGLA_SMP_NAME = "Bangla SMP";
    @Unique
    private static final String BANGLA_SMP_IP = "play.banglasmp.net";

    @Shadow
    @Final
    private List<ServerInfo> servers;

    @Inject(method = "loadFile", at = @At("TAIL"), require = 0)
    private void banglaclient$ensureBanglaSmpOnLoad(CallbackInfo ci) {
        banglaclient$pinBanglaSmpAtTop();
    }

    @Inject(method = "saveFile", at = @At("HEAD"), require = 0)
    private void banglaclient$ensureBanglaSmpOnSave(CallbackInfo ci) {
        banglaclient$pinBanglaSmpAtTop();
    }

    @Unique
    private void banglaclient$pinBanglaSmpAtTop() {
        if (this.servers == null) return;
        ServerInfo existingSmp = null;
        Iterator<ServerInfo> it = this.servers.iterator();
        while (it.hasNext()) {
            ServerInfo info = it.next();
            if (info != null && info.address != null && info.address.trim().equalsIgnoreCase(BANGLA_SMP_IP)) {
                if (existingSmp == null) {
                    existingSmp = info;
                    existingSmp.name = BANGLA_SMP_NAME;
                    existingSmp.address = BANGLA_SMP_IP;
                }
                it.remove();
            }
        }
        if (existingSmp == null) {
            existingSmp = new ServerInfo(BANGLA_SMP_NAME, BANGLA_SMP_IP, ServerInfo.ServerType.OTHER);
        }
        this.servers.add(0, existingSmp);
    }
}
