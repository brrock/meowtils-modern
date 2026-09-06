package wtf.tatp.meowtils.gui.values;
import java.util.List;
import wtf.tatp.meowtils.gui.Module;
public class ModeValue extends Value<String> {
    private final List<String> modes;
    public ModeValue(String name, List<String> modes, String config, Module owner) { super(name, config, modes.getFirst(), owner); this.modes = List.copyOf(modes); }
    public List<String> getModes() { return modes; }
    public String getMode() { return super.getValue(); }
    public String getValueString() { return getMode(); }
    @Override public String getValue() { return super.getValue(); }
    public boolean is(String mode) { return getMode().equalsIgnoreCase(mode); }
    public void setMode(String mode) { if (!modes.contains(mode)) throw new IllegalArgumentException("Unknown mode: " + mode); super.setValue(mode); }
    public void setValue(String mode) { setMode(mode); }
    public void setValue(int index) { setMode(modes.get(index)); }
}
