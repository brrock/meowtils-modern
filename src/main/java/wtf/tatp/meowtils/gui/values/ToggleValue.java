package wtf.tatp.meowtils.gui.values;
import wtf.tatp.meowtils.gui.Module;
public class ToggleValue extends Value<Boolean> {
    public ToggleValue(String name, String config, Module owner) { this(name, config, false, owner); }
    public ToggleValue(String name, String config, boolean initial, Module owner) { super(name, config, initial, owner); }
    public boolean get() { return getValue(); }
    public boolean getState() { return get(); }
    public void set(boolean value) { setValue(value); }
    public void setState(boolean value) { set(value); }
    public void toggle() { set(!get()); }
}
