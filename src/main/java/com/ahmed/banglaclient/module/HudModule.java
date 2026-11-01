package com.ahmed.banglaclient.module;

import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.ColorSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;

/**
 * Base class for all modules that render a movable, scalable HUD widget on screen.
 */
public abstract class HudModule extends Module {
    private float posX;
    private float posY;
    private final float defaultPosX;
    private final float defaultPosY;
    private float width;
    private float height;

    protected final NumberSetting scaleSetting;
    protected final BooleanSetting showBackgroundSetting;
    protected final BooleanSetting showBorderSetting;
    protected final NumberSetting cornerRadiusSetting;
    protected final NumberSetting paddingSetting;
    protected final ColorSetting textColorSetting;
    protected final ColorSetting backgroundColorSetting;

    protected HudModule(
            String id,
            String name,
            String description,
            ModuleCategory category,
            float defaultPosX,
            float defaultPosY,
            float initialWidth,
            float initialHeight,
            boolean defaultEnabled
    ) {
        super(id, name, description, category, -1, defaultEnabled);
        this.defaultPosX = defaultPosX;
        this.defaultPosY = defaultPosY;
        this.posX = defaultPosX;
        this.posY = defaultPosY;
        this.width = Math.max(24.0f, initialWidth);
        this.height = Math.max(14.0f, initialHeight);

        this.scaleSetting = addSetting(new NumberSetting(
                "hud_scale", "Widget Scale", "Scale factor for this HUD widget",
                1.0, 0.5, 2.5, 0.05, "x"
        ));
        this.showBackgroundSetting = addSetting(new BooleanSetting(
                "hud_background", "Glass Background", "Render a translucent Liquid Glass panel behind this widget",
                true
        ));
        this.showBorderSetting = addSetting(new BooleanSetting(
                "hud_border", "Glass Border", "Render a subtle specular border around this widget",
                true
        )).visibleWhen(showBackgroundSetting::isEnabled);
        this.cornerRadiusSetting = addSetting(new NumberSetting(
                "hud_radius", "Corner Radius", "Border rounding radius in pixels",
                6.0, 0.0, 14.0, 1.0, "px"
        )).visibleWhen(showBackgroundSetting::isEnabled) instanceof NumberSetting ns ? ns : null;
        this.paddingSetting = addSetting(new NumberSetting(
                "hud_padding", "Internal Padding", "Inner padding around widget content",
                6.0, 2.0, 16.0, 1.0, "px"
        ));
        this.textColorSetting = addSetting(new ColorSetting(
                "hud_text_color", "Text Color", "Primary text color for this widget",
                0xFFF4F7FA, true
        ));
        this.backgroundColorSetting = addSetting(new ColorSetting(
                "hud_bg_color", "Surface Tint", "Background glass tint color and opacity",
                0xCC0D1117, true
        )).visibleWhen(showBackgroundSetting::isEnabled) instanceof ColorSetting cs ? cs : null;
    }

    /**
     * Renders the HUD widget content at its current position.
     *
     * @param context   Minecraft DrawContext.
     * @param tickDelta Partial tick time.
     * @param preview   True if rendered inside the HUD Editor screen (shows sample state if needed).
     */
    public abstract void renderHud(DrawContext context, float tickDelta, boolean preview);

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public void setPosition(float x, float y) {
        this.posX = x;
        this.posY = y;
    }

    public void resetPosition() {
        this.posX = defaultPosX;
        this.posY = defaultPosY;
        this.scaleSetting.resetToDefault();
    }

    public float getWidth() {
        return width;
    }

    protected void setWidth(float width) {
        this.width = Math.max(16.0f, width);
    }

    public float getHeight() {
        return height;
    }

    protected void setHeight(float height) {
        this.height = Math.max(12.0f, height);
    }

    public float getScale() {
        return scaleSetting.getFloatValue();
    }

    public float getScaledWidth() {
        return width * getScale();
    }

    public float getScaledHeight() {
        return height * getScale();
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= posX && mouseX <= (posX + getScaledWidth())
                && mouseY >= posY && mouseY <= (posY + getScaledHeight());
    }

    @Override
    public JsonObject toJson() {
        JsonObject root = super.toJson();
        JsonObject hudObj = new JsonObject();
        hudObj.addProperty("posX", posX);
        hudObj.addProperty("posY", posY);
        hudObj.addProperty("width", width);
        hudObj.addProperty("height", height);
        root.add("hudLayout", hudObj);
        return root;
    }

    @Override
    public void fromJson(JsonObject root) {
        super.fromJson(root);
        if (root != null && root.has("hudLayout") && root.get("hudLayout").isJsonObject()) {
            JsonObject hudObj = root.getAsJsonObject("hudLayout");
            if (hudObj.has("posX") && hudObj.get("posX").isJsonPrimitive()) {
                this.posX = hudObj.get("posX").getAsFloat();
            }
            if (hudObj.has("posY") && hudObj.get("posY").isJsonPrimitive()) {
                this.posY = hudObj.get("posY").getAsFloat();
            }
            if (hudObj.has("width") && hudObj.get("width").isJsonPrimitive()) {
                setWidth(hudObj.get("width").getAsFloat());
            }
            if (hudObj.has("height") && hudObj.get("height").isJsonPrimitive()) {
                setHeight(hudObj.get("height").getAsFloat());
            }
        }
    }
}
