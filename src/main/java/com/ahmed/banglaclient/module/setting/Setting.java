package com.ahmed.banglaclient.module.setting;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Abstract base class for all typed, serializable module settings in BANGLA CLIENT.
 *
 * @param <T> Value type held by the setting.
 */
public abstract class Setting<T> {
    private final String id;
    private final String name;
    private final String description;
    private final T defaultValue;
    private T value;
    private BooleanSupplier visibilityCondition = () -> true;
    private final List<Consumer<T>> changeListeners = new ArrayList<>();

    protected Setting(String id, String name, String description, T defaultValue) {
        this.id = Objects.requireNonNull(id, "Setting id must not be null");
        this.name = Objects.requireNonNull(name, "Setting name must not be null");
        this.description = description != null ? description : "";
        this.defaultValue = Objects.requireNonNull(defaultValue, "Default value must not be null");
        this.value = defaultValue;
    }

    public final String getId() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final T getDefaultValue() {
        return defaultValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T newValue) {
        if (newValue == null) {
            return;
        }
        T validated = validate(newValue);
        if (!Objects.equals(this.value, validated)) {
            this.value = validated;
            notifyListeners(this.value);
        }
    }

    public void resetToDefault() {
        setValue(defaultValue);
    }

    public boolean isDefault() {
        return Objects.equals(value, defaultValue);
    }

    public Setting<T> visibleWhen(BooleanSupplier condition) {
        if (condition != null) {
            this.visibilityCondition = condition;
        }
        return this;
    }

    public boolean isVisible() {
        return visibilityCondition.getAsBoolean();
    }

    public Setting<T> onChange(Consumer<T> listener) {
        if (listener != null) {
            this.changeListeners.add(listener);
        }
        return this;
    }

    protected void notifyListeners(T updatedValue) {
        for (Consumer<T> listener : changeListeners) {
            listener.accept(updatedValue);
        }
    }

    /**
     * Validates or clamps incoming values before assignment.
     */
    protected abstract T validate(T candidate);

    /**
     * Converts the current setting value into a Gson JsonElement for persistence.
     */
    public abstract JsonElement toJson();

    /**
     * Loads and validates the setting value from a Gson JsonElement.
     */
    public abstract void fromJson(JsonElement element);
}
