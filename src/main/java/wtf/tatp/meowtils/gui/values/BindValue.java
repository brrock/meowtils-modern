package wtf.tatp.meowtils.gui.values;
import wtf.tatp.meowtils.gui.Module;
public class BindValue extends Value<Integer> {
    private final Module owner;
    public BindValue(String name, String config, Module owner) { super(name, config, 0, owner); this.owner = owner; }
    @Override public Integer getValue() { return owner != null && "key".equals(getConfig()) ? owner.getKey() : super.getValue(); }
    @Override public void setValue(Integer key) { super.setValue(key); if (owner != null && "key".equals(getConfig())) owner.setKey(key); }
    public int getBind() { return getValue(); }
    public void setBind(int key) { setValue(key); }
}
