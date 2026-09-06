package wtf.tatp.meowtils.gui.values;
public class ButtonValue extends Value<Runnable> {
    private final float scale;
    public ButtonValue(String name, float textScale, Runnable action) { super(name, name, action); scale = textScale; }
    public float getScale() { return scale; }
    public void press() { getValue().run(); }
    public void click() { press(); }
}
