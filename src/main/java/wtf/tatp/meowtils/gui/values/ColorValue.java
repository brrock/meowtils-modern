package wtf.tatp.meowtils.gui.values;

import wtf.tatp.meowtils.gui.ColorLink;

public final class ColorValue extends Value<Integer> {
    private final ColorLink link;
    public ColorValue(String name, ColorLink link) { super(name, name, 0xFFFFFFFF); this.link = link; }
    public ColorLink getLink() { return link; }
    @Override public Integer getValue() { return 0xFF000000 | link.getRGB(); }
    @Override public void setValue(Integer color) { link.setRGB(color); }
    public int getColor() { return getValue(); }
    public void setColor(int color) { setValue(color); }
    public double get() { return getLink().getHue()*360.0; }
    public void set(double hue) { getLink().apply((float)(hue/360),getLink().getSaturation(),getLink().getBrightness()); }
}
