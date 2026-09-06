package wtf.tatp.meowtils.gui.values;
import wtf.tatp.meowtils.gui.Module;
public class SliderValue extends Value<Double> {
    private final double min, max, increment;
    private final String valueType;
    public SliderValue(String name, double min, double max, double increment, String valueType, String config, Module owner, Class<?> targetType) { super(name, config, min, owner); this.min=min; this.max=max; this.increment=increment; this.valueType=valueType; }
    public double get() { return getValue(); }
    public String getFormattedValue() { return java.math.BigDecimal.valueOf(get()).stripTrailingZeros().toPlainString(); }
    public String getValueType() { return valueType; }
    public void set(double value) { setValue(value); }
    @Override public void setValue(Double value) {
        if (!Double.isFinite(value)) return;
        double rounded = increment > 0 ? Math.round(value / increment) * increment : value;
        super.setValue(Math.max(min, Math.min(max, rounded)));
    }
    public double getMin() { return min; } public double getMax() { return max; } public double getIncrement() { return increment; }
}
