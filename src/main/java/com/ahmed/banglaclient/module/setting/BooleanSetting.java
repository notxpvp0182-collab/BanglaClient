package com.ahmed.banglaclient.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Represents a boolean ON/OFF setting for a module.
 */
public final class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String id, String name, String description, boolean defaultValue) {
        super(id, name, description, defaultValue);
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(getValue());
    }

    public void toggle() {
        setValue(!isEnabled());
    }

    @Override
    protected Boolean validate(Boolean candidate) {
        return candidate != null ? candidate : getDefaultValue();
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(isEnabled());
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
            setValue(element.getAsBoolean());
        }
    }
}
