package com.ahmed.banglaclient.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Represents a cycleable/dropdown enum setting for a module.
 *
 * @param <E> Enum type.
 */
public final class EnumSetting<E extends Enum<E>> extends Setting<E> {
    private final E[] constants;

    public EnumSetting(String id, String name, String description, E defaultValue) {
        super(id, name, description, defaultValue);
        this.constants = defaultValue.getDeclaringClass().getEnumConstants();
    }

    public E[] getConstants() {
        return constants.clone();
    }

    public void cycleNext() {
        int nextIndex = (getValue().ordinal() + 1) % constants.length;
        setValue(constants[nextIndex]);
    }

    public void cyclePrevious() {
        int prevIndex = (getValue().ordinal() - 1 + constants.length) % constants.length;
        setValue(constants[prevIndex]);
    }

    @Override
    protected E validate(E candidate) {
        return candidate != null ? candidate : getDefaultValue();
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getValue().name());
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String rawName = element.getAsString();
            for (E constant : constants) {
                if (constant.name().equalsIgnoreCase(rawName)) {
                    setValue(constant);
                    return;
                }
            }
        }
    }
}
