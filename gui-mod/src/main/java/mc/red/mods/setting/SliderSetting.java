package mc.red.mods.setting;

public class SliderSetting extends Setting {
    private double value;
    private final double min;
    private final double max;
    private final double step;

    public SliderSetting(String name, double min, double max, double value, double step) {
        super(name);
        this.min = min;
        this.max = max;
        this.value = Math.max(min, Math.min(max, value));
        this.step = step;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double v) {
        double clamped = Math.max(min, Math.min(max, v));
        this.value = Math.round(clamped / step) * step;
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
}
