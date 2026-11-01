package com.ahmed.banglaclient.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Represents a bounded text setting for custom labels, formats, or messages.
 */
public final class StringSetting extends Setting<String> {
    private final int maxLength;

    public StringSetting(String id, String name, String description, String defaultValue, int maxLength) {
        super(id, name, description, sanitize(defaultValue, maxLength));
        this.maxLength = Math.max(1, maxLength);
        setValue(defaultValue);
    }

    public StringSetting(String id, String name, String description, String defaultValue) {
        this(id, name, description, defaultValue, 128);
    }

    public int getMaxLength() {
        return maxLength;
    }

    @Override
    protected String validate(String candidate) {
        return sanitize(candidate, maxLength);
    }

    private static String sanitize(String input, int limit) {
        if (input == null) {
            return "";
        }
        int effectiveLimit = Math.max(1, limit);
        if (input.length() > effectiveLimit) {
            return input.substring(0, effectiveLimit);
        }
        return input;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            setValue(element.getAsString());
        }
    }
}
