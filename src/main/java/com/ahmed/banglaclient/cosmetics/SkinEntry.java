package com.ahmed.banglaclient.cosmetics;

import net.minecraft.util.Identifier;
import java.nio.file.Path;

public final class SkinEntry {
    private final String id;
    private final String displayName;
    private final Path filePath;
    private final int width;
    private final int height;
    private final boolean cape;
    private boolean slimModel;
    private final Identifier textureId;

    public SkinEntry(
            String id,
            String displayName,
            Path filePath,
            int width,
            int height,
            boolean cape,
            boolean slimModel,
            Identifier textureId
    ) {
        this.id = id;
        this.displayName = displayName;
        this.filePath = filePath;
        this.width = width;
        this.height = height;
        this.cape = cape;
        this.slimModel = slimModel;
        this.textureId = textureId;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Path getFilePath() { return filePath; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isCape() { return cape; }
    public boolean isSlimModel() { return slimModel; }
    public void setSlimModel(boolean slimModel) { this.slimModel = slimModel; }
    public Identifier getTextureId() { return textureId; }
}
