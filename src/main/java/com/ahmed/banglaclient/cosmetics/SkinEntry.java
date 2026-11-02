package com.ahmed.banglaclient.cosmetics;

import java.nio.file.Path;

/**
 * Represents a validated local skin or cape texture in the BANGLA CLIENT cosmetics library.
 */
public final class SkinEntry {
    private final String id;
    private final String displayName;
    private final Path filePath;
    private final int width;
    private final int height;
    private final boolean cape;
    private boolean slimModel;
    private final int[] argbPixels;

    public SkinEntry(
            String id,
            String displayName,
            Path filePath,
            int width,
            int height,
            boolean cape,
            boolean slimModel,
            int[] argbPixels
    ) {
        this.id = id;
        this.displayName = displayName;
        this.filePath = filePath;
        this.width = width;
        this.height = height;
        this.cape = cape;
        this.slimModel = slimModel;
        this.argbPixels = argbPixels;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Path getFilePath() { return filePath; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isCape() { return cape; }
    public boolean isSlimModel() { return slimModel; }
    public void setSlimModel(boolean slimModel) { this.slimModel = slimModel; }

    /**
     * Samples a texel at normalized 64x64 (for skins) or 64x32 (for capes) UV coordinates.
     */
    public int sampleUv(int u, int v, int virtualW, int virtualH) {
        if (argbPixels == null || width <= 0 || height <= 0) return 0;
        int sx = Math.max(0, Math.min(width - 1, (u * width) / Math.max(1, virtualW)));
        int sy = Math.max(0, Math.min(height - 1, (v * height) / Math.max(1, virtualH)));
        return argbPixels[sy * width + sx];
    }
}
