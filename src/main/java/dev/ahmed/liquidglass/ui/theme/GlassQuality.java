package dev.ahmed.liquidglass.ui.theme;

public enum GlassQuality {
    LOW(0, 1.0f, false, false, false),
    MEDIUM(2, 0.5f, true, false, false),
    HIGH(3, 0.5f, true, true, false),
    ULTRA(4, 0.5f, true, true, true);

    private final int blurPasses;
    private final float framebufferScale;
    private final boolean shadowEnabled;
    private final boolean innerHighlightEnabled;
    private final boolean liquidRefractionEnabled;

    GlassQuality(int blurPasses, float framebufferScale, boolean shadowEnabled,
                 boolean innerHighlightEnabled, boolean liquidRefractionEnabled) {
        this.blurPasses = blurPasses;
        this.framebufferScale = framebufferScale;
        this.shadowEnabled = shadowEnabled;
        this.innerHighlightEnabled = innerHighlightEnabled;
        this.liquidRefractionEnabled = liquidRefractionEnabled;
    }

    public int blurPasses() { return blurPasses; }
    public float framebufferScale() { return framebufferScale; }
    public boolean shadowEnabled() { return shadowEnabled; }
    public boolean innerHighlightEnabled() { return innerHighlightEnabled; }
    public boolean liquidRefractionEnabled() { return liquidRefractionEnabled; }
}
