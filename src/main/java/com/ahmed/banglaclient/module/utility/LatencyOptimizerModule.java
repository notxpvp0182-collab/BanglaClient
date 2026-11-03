package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

/**
 * "Latency Optimizer" module.
 * Optimizes client-side TCP socket packet dispatching and buffer responsiveness.
 * Client-side optimization only; does not alter remote server ping or simulate fake latency.
 */
public final class LatencyOptimizerModule extends Module {
    private final BooleanSetting enableTcpNoDelaySetting;
    private final BooleanSetting optimizeClientPacketQueueSetting;

    public LatencyOptimizerModule() {
        super(
                "latency_optimizer",
                "Latency Optimizer",
                "Optimizes client-side TCP socket packet dispatching and buffer responsiveness.",
                ModuleCategory.UTILITY
        );
        this.enableTcpNoDelaySetting = addSetting(new BooleanSetting(
                "tcp_nodelay", "Enable TCP_NODELAY", "Disables Nagle's algorithm on client network connection to flush packets immediately", true
        ));
        this.optimizeClientPacketQueueSetting = addSetting(new BooleanSetting(
                "packet_queue", "Responsive Packet Queue", "Ensures outgoing client interaction packets are flushed on every client tick", true
        ));
        addAliases("latency", "pingoptimizer", "packetflush", "networkopt");
    }

    public boolean isTcpNoDelayEnabled() { return isEnabled() && enableTcpNoDelaySetting.isEnabled(); }
    public boolean isOptimizePacketQueue() { return isEnabled() && optimizeClientPacketQueueSetting.isEnabled(); }
}
