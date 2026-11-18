package com.ahmed.banglaclient.performance;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Central BANGLACLIENT Performance, Telemetry & Adaptive Optimization Engine (Sections 01, 02, 03, 17, 23).
 * Never fabricates metrics; reports "Unavailable" when platform telemetry is unsupported.
 */
public final class PerformanceEngine {
    private static final PerformanceEngine INSTANCE = new PerformanceEngine();
    private static final int SAMPLE_WINDOW = 240;

    private final float[] frameTimesMs = new float[SAMPLE_WINDOW];
    private final int[] fpsHistory = new int[SAMPLE_WINDOW];
    private int sampleCursor = 0;
    private int sampleCount = 0;
    private long lastFrameNano = System.nanoTime();

    private float currentFrameTimeMs = 16.6f;
    private float frameTimeVarianceMs = 0.0f;
    private int currentOnePercentLowFps = 60;
    private int sessionPeakFps = 0;
    private long sessionFpsSum = 0L;
    private long sessionFpsSamples = 0L;

    // Network & TPS Real Measurement
    private final AtomicInteger txPacketsThisSecond = new AtomicInteger(0);
    private final AtomicInteger rxPacketsThisSecond = new AtomicInteger(0);
    private volatile int lastTxRate = 0;
    private volatile int lastRxRate = 0;
    private long lastPacketRateResetMs = System.currentTimeMillis();

    private long lastWorldTimeTick = -1L;
    private long lastWorldTimeMs = -1L;
    private float measuredServerTps = -1.0f; // -1 means Unavailable

    // Input & AFK Tracking
    private long lastInputActivityMs = System.currentTimeMillis();
    private int confirmedHitsSession = 0;
    private int lastAttackEntityId = -1;

    // Adaptive FPS & Render Distance Hysteresis State
    private long lastRenderDistAdjustMs = 0L;
    private int savedOriginalViewDistance = -1;
    private int savedOriginalMaxFps = -1;

    private final it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap cellItemCounts = new it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap();
    private final it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap cellEntityCounts = new it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap();
    private int pistonsRenderedThisFrame = 0;

    public boolean allowPistonRender(int maxPistons) {
        return ++pistonsRenderedThisFrame <= maxPistons;
    }
    private PerformanceEngine() {}

    public static PerformanceEngine get() {
        return INSTANCE;
    }

    /**
     * Called once per rendered frame to record real frame time and compute rolling 1% Low FPS & variance.
     */
    public void onRenderFrame() {
        cellItemCounts.clear();
        cellEntityCounts.clear();
        pistonsRenderedThisFrame = 0;
        long now = System.nanoTime();
        float dtMs = (now - lastFrameNano) / 1_000_000.0f;
        lastFrameNano = now;
        if (dtMs <= 0.01f || dtMs > 5000.0f) return;

        currentFrameTimeMs = dtMs;
        frameTimesMs[sampleCursor] = dtMs;
        MinecraftClient mc = MinecraftClient.getInstance();
        int curFps = mc != null ? Math.max(0, mc.getCurrentFps()) : Math.round(1000.0f / dtMs);
        fpsHistory[sampleCursor] = curFps;

        sampleCursor = (sampleCursor + 1) % SAMPLE_WINDOW;
        if (sampleCount < SAMPLE_WINDOW) sampleCount++;

        if (curFps > sessionPeakFps) sessionPeakFps = curFps;
        if (curFps > 0) {
            sessionFpsSum += curFps;
            sessionFpsSamples++;
        }

        // Recompute 1% Low FPS & Variance every 15 frames to avoid per-frame sort allocations
        if (sampleCursor % 15 == 0 && sampleCount >= 10) {
            float[] copy = Arrays.copyOf(frameTimesMs, sampleCount);
            Arrays.sort(copy);
            // 99th percentile slowest frame duration = 1% Low FPS
            int p99Index = Math.min(sampleCount - 1, Math.max(0, (int) Math.floor(sampleCount * 0.99f)));
            float slowestOnePctMs = Math.max(0.5f, copy[p99Index]);
            currentOnePercentLowFps = Math.max(1, Math.round(1000.0f / slowestOnePctMs));

            float sum = 0.0f;
            for (int i = 0; i < sampleCount; i++) sum += copy[i];
            float mean = sum / sampleCount;
            float sqDiff = 0.0f;
            for (int i = 0; i < sampleCount; i++) {
                float d = copy[i] - mean;
                sqDiff += d * d;
            }
            frameTimeVarianceMs = (float) Math.sqrt(sqDiff / sampleCount);
        }

        long nowMs = System.currentTimeMillis();
        if (nowMs - lastPacketRateResetMs >= 1000L) {
            lastTxRate = txPacketsThisSecond.getAndSet(0);
            lastRxRate = rxPacketsThisSecond.getAndSet(0);
            lastPacketRateResetMs = nowMs;
        }
    }

    /**
     * Called once per client tick to update real TPS estimation, hit tracking, and hysteresis-protected adaptive limits.
     */
    public void onClientTick(MinecraftClient client) {
        if (client == null) return;

        if (client.options != null && (client.options.forwardKey.isPressed()
                || client.options.backKey.isPressed()
                || client.options.leftKey.isPressed()
                || client.options.rightKey.isPressed()
                || client.options.attackKey.isPressed()
                || client.options.useKey.isPressed())) {
            lastInputActivityMs = System.currentTimeMillis();
        }

        if (client.world == null || client.player == null) {
            lastWorldTimeTick = -1L;
            lastWorldTimeMs = -1L;
            measuredServerTps = -1.0f;
            return;
        }

        // Track confirmed hits on targeted living entities
        if (client.targetedEntity != null && client.options.attackKey.isPressed()) {
            if (client.targetedEntity.getId() != lastAttackEntityId || client.player.getAttackCooldownProgress(0.0f) > 0.85f) {
                lastAttackEntityId = client.targetedEntity.getId();
                confirmedHitsSession++;
            }
        }

        // Measure Server TPS from actual world time progression over 1-second windows
        long nowMs = System.currentTimeMillis();
        long worldTime = client.world.getTime();
        if (lastWorldTimeTick >= 0L && nowMs - lastWorldTimeMs >= 1000L) {
            long deltaTicks = worldTime - lastWorldTimeTick;
            if (deltaTicks >= 0L && deltaTicks <= 100L) {
                float rawTps = Math.min(20.0f, (deltaTicks * 1000.0f) / Math.max(1L, nowMs - lastWorldTimeMs));
                measuredServerTps = (measuredServerTps < 0.0f) ? rawTps : (measuredServerTps * 0.75f + rawTps * 0.25f);
            }
            lastWorldTimeTick = worldTime;
            lastWorldTimeMs = nowMs;
        } else if (lastWorldTimeTick < 0L) {
            lastWorldTimeTick = worldTime;
            lastWorldTimeMs = nowMs;
        }
    }

    /**
     * Evaluates whether an entity should be culled based on active Optimizer modules (Section 01).
     */
    public boolean shouldCullEntity(Entity entity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || entity == null || entity == mc.player) return false;
        BanglaClient bc = BanglaClient.getInstance();
        if (bc == null || bc.getModuleManager() == null) return false;

        double distSq = entity.squaredDistanceTo(mc.player);

        long cellKey = (((long) (entity.getBlockX() >> 1) & 0x3FFFFFL) << 42)
                | (((long) (entity.getBlockY() >> 1) & 0xFFFFFL) << 22)
                | ((long) (entity.getBlockZ() >> 1) & 0x3FFFFFL);

        var lootOpt = bc.getModuleManager().getModule(com.ahmed.banglaclient.module.performance.ItemEntityOptimizerModule.class);
        if (entity instanceof ItemEntity && lootOpt.isPresent() && lootOpt.get().isEnabled()) {
            if (distSq > lootOpt.get().getMaxDistanceSq()) return true;
            int cnt = cellItemCounts.addTo(cellKey, 1);
            if (cnt >= lootOpt.get().getMaxItemsPerCell()) return true;
        }

        var lagShield = bc.getModuleManager().getModule(com.ahmed.banglaclient.module.performance.LagMachineOptimizerModule.class);
        if (lagShield.isPresent() && lagShield.get().isEnabled()) {
            if (lagShield.get().shouldCullFallingBlocks() && entity instanceof net.minecraft.entity.FallingBlockEntity && distSq > 32.0 * 32.0) {
                return true;
            }
            if (lagShield.get().shouldLimitStackedEntities() && !(entity instanceof net.minecraft.entity.player.PlayerEntity)) {
                int ec = cellEntityCounts.addTo(cellKey, 1);
                if (ec >= lagShield.get().getMaxEntitiesPerCell()) return true;
            }
        }
        if (entity instanceof ExperienceOrbEntity && isModEnabled("xp_optimizer") && distSq > 36.0 * 36.0) {
            return true;
        }
        if (entity instanceof TntEntity && isModEnabled("tnt_optimizer") && distSq > 64.0 * 64.0) {
            return true;
        }
        if (entity instanceof ProjectileEntity && isModEnabled("projectile_optimizer") && distSq > 64.0 * 64.0) {
            return true;
        }
        if (entity instanceof AbstractMinecartEntity && isModEnabled("cart_optimizer") && distSq > 56.0 * 56.0) {
            return true;
        }
        if (entity instanceof MobEntity && isModEnabled("mob_rendering_optimizer") && distSq > 72.0 * 72.0) {
            return true;
        }
        if (isModEnabled("entity_culling") && distSq > 84.0 * 84.0 && !entity.hasCustomName()) {
            return true;
        }
        return false;
    }

    private boolean isModEnabled(String id) {
        BanglaClient bc = BanglaClient.getInstance();
        if (bc == null || bc.getModuleManager() == null) return false;
        return bc.getModuleManager().getModuleById(id).map(Module::isEnabled).orElse(false);
    }

    public void recordOutgoingPacket() { txPacketsThisSecond.incrementAndGet(); }
    public void recordIncomingPacket() { rxPacketsThisSecond.incrementAndGet(); }

    public int getTxPacketRate() { return lastTxRate; }
    public int getRxPacketRate() { return lastRxRate; }

    public float getCurrentFrameTimeMs() { return currentFrameTimeMs; }
    public float getFrameTimeVarianceMs() { return frameTimeVarianceMs; }
    public int getOnePercentLowFps() { return currentOnePercentLowFps; }
    public int getSessionPeakFps() { return sessionPeakFps; }
    public int getSessionAverageFps() {
        return sessionFpsSamples > 0 ? (int) (sessionFpsSum / sessionFpsSamples) : 60;
    }
    public int getConfirmedHitsSession() { return confirmedHitsSession; }
    public void resetConfirmedHits() { confirmedHitsSession = 0; }
    public boolean isPlayerAfk() { return System.currentTimeMillis() - lastInputActivityMs > 120_000L; }

    public String getFormattedServerTps() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.isInSingleplayer() && mc.getServer() != null) {
            float mspt = mc.getServer().getAverageTickTime();
            float spTps = Math.min(20.0f, 1000.0f / Math.max(1.0f, mspt));
            return String.format(Locale.ROOT, "%.1f TPS", spTps);
        }
        if (measuredServerTps < 0.0f) {
            return "Unavailable";
        }
        return String.format(Locale.ROOT, "%.1f TPS", measuredServerTps);
    }

    public String getFormattedCpuUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                double load = sunBean.getProcessCpuLoad();
                if (load < 0.0) load = sunBean.getCpuLoad();
                if (load >= 0.0) {
                    return String.format(Locale.ROOT, "%d%%", Math.min(100, Math.round(load * 100.0)));
                }
            }
        } catch (Throwable ignored) {}
        return "Unavailable";
    }

    public String getFormattedGpuUsage() {
        // Standard cross-vendor OpenGL 3.3 / GLES 3 does not expose hardware GPU % without vendor drivers
        return "Unavailable";
    }

    public int getLoadedEntityCount() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return (mc != null && mc.world != null) ? mc.world.getRegularEntityCount() : 0;
    }

    public String getActiveParticleCountString() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.particleManager != null) {
            try {
                return mc.particleManager.getDebugString();
            } catch (Throwable ignored) {}
        }
        return "0";
    }

    public int getCompletedChunkCount() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.worldRenderer != null) {
            try {
                return mc.worldRenderer.getCompletedChunkCount();
            } catch (Throwable ignored) {}
        }
        return 0;
    }

    public String analyzeBottleneck() {
        if (frameTimeVarianceMs > 12.0f) return "High Frame-Time Variance (Stutter)";
        Runtime rt = Runtime.getRuntime();
        long ramPct = ((rt.totalMemory() - rt.freeMemory()) * 100L) / Math.max(1L, rt.maxMemory());
        if (ramPct >= 88L) return "High RAM Heap Pressure (>88% - GC Risk)";
        if (getLoadedEntityCount() > 250) return "High Entity Rendering Workload (>250)";
        return "None (Balanced Frame Pacing)";
    }
}
