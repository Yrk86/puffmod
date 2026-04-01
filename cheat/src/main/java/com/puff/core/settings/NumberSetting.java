package com.puff.core.settings;

/**
 * Setting numérique (double) avec bornes.
 */
public class NumberSetting extends Setting<Double> {

    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        super(name, clamp(defaultValue, min, max));
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double get() {
        Double v = getValue();
        return v == null ? 0.0 : v;
    }

    public void set(double value) {
        // On limite la valeur aux bornes.
        double clamped = clamp(value, min, max);

        // On applique un "pas" (step) pour éviter des valeurs trop fines.
        if (step > 0) {
            clamped = Math.round(clamped / step) * step;
        }

        setValue(clamped);
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
}

