package wtf.tatp.meowtils.gui.values;
import wtf.tatp.meowtils.gui.Module;
public class TextValue extends Value<String> {
    private String description = "";
    public TextValue(String name, String config, Module owner) { super(name, config, "", owner); }
    public TextValue(String name, String description, String config, Module owner) { this(name, config, owner); this.description = description; }
    public String getDescription() { return description; }
    public String get() { return getValue(); }
    public void set(String value) { setValue(value); }
}
