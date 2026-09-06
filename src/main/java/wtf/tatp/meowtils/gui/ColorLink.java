package wtf.tatp.meowtils.gui;

import wtf.tatp.meowtils.gui.values.SliderValue;

public final class ColorLink {
    private final SliderValue red, green, blue;
    private int last = -1;
    private float hue, saturation, brightness;
    public ColorLink(SliderValue red, SliderValue green, SliderValue blue) { this.red=red; this.green=green; this.blue=blue; }
    public SliderValue red() { return red; }
    public SliderValue green() { return green; }
    public SliderValue blue() { return blue; }
    private void sync() {
        int rgb = (int) red.get() << 16 | (int) green.get() << 8 | (int) blue.get();
        if (rgb == last) return;
        float[] hsb = java.awt.Color.RGBtoHSB((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, null);
        hue=hsb[0]; saturation=hsb[1]; brightness=hsb[2]; last=rgb;
    }
    public void apply(float h, float s, float b) {
        hue=clamp(h); saturation=clamp(s); brightness=clamp(b);
        last=java.awt.Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
        red.set((last >> 16) & 255); green.set((last >> 8) & 255); blue.set(last & 255);
    }
    private static float clamp(float v) { return Math.max(0, Math.min(1, v)); }
    public float getHue() { sync(); return hue; }
    public float getSaturation() { sync(); return saturation; }
    public float getBrightness() { sync(); return brightness; }
    public int getRGB() { sync(); return last; }
    public int getPureHueRGB() { return java.awt.Color.HSBtoRGB(getHue(), 1, 1) & 0xFFFFFF; }
    public void setRGB(int rgb) { red.set((rgb >> 16) & 255); green.set((rgb >> 8) & 255); blue.set(rgb & 255); sync(); }
    /** Compatibility constructor used by older extensions; prefer Extension.linkColor(...). */
    public ColorLink(String redConfig, String greenConfig, String blueConfig, Module owner) {
        this(new SliderValue("Red", 0, 255, 1, null, redConfig, owner, Integer.class),
                new SliderValue("Green", 0, 255, 1, null, greenConfig, owner, Integer.class),
                new SliderValue("Blue", 0, 255, 1, null, blueConfig, owner, Integer.class));
    }
}
