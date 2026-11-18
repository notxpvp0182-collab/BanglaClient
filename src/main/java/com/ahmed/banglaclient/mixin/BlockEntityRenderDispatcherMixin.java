package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.ModuleManager;
import com.ahmed.banglaclient.module.performance.BeaconBeamOptimizerModule;
import com.ahmed.banglaclient.module.performance.BlockRenderingOptimizerModule;
import com.ahmed.banglaclient.module.performance.SignRenderingOptimizerModule;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private <E extends BlockEntity> void banglaclient$cullBlockEntities(
            E blockEntity,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            CallbackInfo ci
    ) {
        if (blockEntity == null) return;
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) return;
        ModuleManager mm = bangla.getModuleManager();

        var signOpt = mm.getModule(SignRenderingOptimizerModule.class);
        if (signOpt.isPresent() && signOpt.get().shouldSkipSign(blockEntity)) {
            ci.cancel();
            return;
        }

        var beaconOpt = mm.getModule(BeaconBeamOptimizerModule.class);
        if (beaconOpt.isPresent() && beaconOpt.get().shouldSkipBeacon(blockEntity)) {
            ci.cancel();
            return;
        }

        if (blockEntity instanceof net.minecraft.block.entity.PistonBlockEntity) { var lagOpt = mm.getModule(com.ahmed.banglaclient.module.performance.LagMachineOptimizerModule.class); if (lagOpt.isPresent() && lagOpt.get().shouldLimitPistons() && !com.ahmed.banglaclient.performance.PerformanceEngine.get().allowPistonRender(lagOpt.get().getMaxPistonsPerFrame())) { ci.cancel(); return; } }
        var blockOpt = mm.getModule(BlockRenderingOptimizerModule.class);
        if (blockOpt.isPresent() && blockOpt.get().shouldSkipBlockEntity(blockEntity)) {
            ci.cancel();
        }
    }
}
