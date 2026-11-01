package com.ahmed.banglaclient.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Represents a bounded numeric setting (double/float/int) with configurable step precision.
 */
public final class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;
    private final String unitSuffix;

    public NumberSetting(
            String id,
            String name,
            String description,
            double defaultValue,
            double min,
            double max,
            double step,
            String unitSuffix
    ) {
        super(id, name, description, clampAndSnap(defaultValue, min, max, step));
        if (min > max) {
            throw new IllegalArgumentException("min cannot be greater than max for NumberSetting: " + id);
        }
        this.min = min;
        this.max = max;
        this.step = step > 0.0 ? step : 0.01;
        this.unitSuffix = unitSuffix != null ? unitSuffix : "";
        setValue(defaultValue);
    }

    public NumberSetting(String id, String name, String description, double defaultValue, double min, double max, double step) {
        this(id, name, description, defaultValue, min, max, step, "");
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public String getUnitSuffix() {
        return unitSuffix;
    }

    public float getFloatValue() {
        return getValue().floatValue();
    }

    public int getIntValue() {
        return (int) Math.round(getValue());
    }

    public double getNormalizedProgress() {
        if (max <= min) {
            return 0.0;
        }
        return (getValue() - min) / (max - min);
    }

    public void setFromNormalizedProgress(double normalized) {
        double clampedNorm = Math.max(0.0, Math.min(1.0, normalized));
        double raw = min + clampedNorm * (max - min);
        setValue(raw);
    }

    public String getFormattedValue() {
        if (step >= 1.0 && Math.floor(step) == step) {
            return getIntValue() + unitSuffix;
        } else if (step >= 0.1) {
            return String.format("%.1f%s", getValue(), unitSuffix);
        }
        return String.format("%.2f%s", getValue(), unitSuffix);
    }

    @Override
    protected Double validate(Double candidate) {
        if (candidate == null || Double.isNaN(candidate) || Double.isInfinite(candidate)) {
            return getDefaultValue();
        }
        return clampAndSnap(candidate, min, max, step);
    }

    private static double clampAndSnap(double val, double minVal, double maxVal, double stepVal) {
        double clamped = Math.max(minVal, Math.min(maxVal, val));
        if (stepVal > 0.0) {
            clamped = Math.round(clamped / stepVal) * stepVal;
            clamped = Math.max(minVal, Math.min(maxVal, clamped));
        }
        return Math.round(clamped * 10000.0) / 10000.0;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            setValue(element.getAsDouble());
        }
    }
}
