package wtf.tatp.meowtils.gui.values;
import wtf.tatp.meowtils.gui.ColorLink;
public final class SaturationValue extends Value<Double> {
    private final ColorLink link;
    public SaturationValue(ColorLink link) { super("Saturation", null, 1.0); this.link=link; }
    public ColorLink getLink() { return link; }
    public double get() { return getValue()*100; }
    public void set(double value) { setValue(value/100); }
    @Override public Double getValue() { return (double) link.getSaturation(); }
    @Override public void setValue(Double value) { link.apply(link.getHue(), value.floatValue(), link.getBrightness()); }
}
