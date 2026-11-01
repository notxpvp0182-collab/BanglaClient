package com.ahmed.banglaclient.core;

/**
 * Tracks client launch duration and world/server session duration.
 */
public final class SessionTracker {
    private final long clientStartMillis;
    private long worldJoinMillis;
    private int sessionJumps;
    private double sessionDistanceBlocks;

    public SessionTracker() {
        this.clientStartMillis = System.currentTimeMillis();
        this.worldJoinMillis = this.clientStartMillis;
    }

    public void onWorldJoin() {
        this.worldJoinMillis = System.currentTimeMillis();
    }

    public long getClientUptimeSeconds() {
        return Math.max(0L, (System.currentTimeMillis() - clientStartMillis) / 1000L);
    }

    public long getWorldSessionSeconds() {
        return Math.max(0L, (System.currentTimeMillis() - worldJoinMillis) / 1000L);
    }

    public void recordJump() {
        this.sessionJumps++;
    }

    public int getSessionJumps() {
        return sessionJumps;
    }

    public void addDistance(double deltaBlocks) {
        if (deltaBlocks > 0.0 && deltaBlocks < 100.0) {
            this.sessionDistanceBlocks += deltaBlocks;
        }
    }

    public double getSessionDistanceBlocks() {
        return sessionDistanceBlocks;
    }

    public void resetCounters() {
        this.sessionJumps = 0;
        this.sessionDistanceBlocks = 0.0;
    }

    public String formatClientUptime() {
        return formatDuration(getClientUptimeSeconds());
    }

    public String formatWorldSession() {
        return formatDuration(getWorldSessionSeconds());
    }

    private static String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
}
