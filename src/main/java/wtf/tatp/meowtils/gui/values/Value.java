package wtf.tatp.meowtils.gui.values;

public class Value<T> {
    private final String name;
    private final String config;
    private T value;
    private Object owner;
    private java.lang.reflect.Field field;
    protected Value(String name, String config, T initial) { this.name = name; this.config = config; this.value = initial; }
    protected Value(String name, String config, T initial, Object owner) {
        this(name, config, initial);
        this.owner = owner;
        if (owner instanceof wtf.tatp.meowtils.gui.Module module && config != null) module.settingsStorage().putIfAbsent(config, initial);
        if (owner == null || config == null) return;
        for (Class<?> type = owner.getClass(); type != null && type != wtf.tatp.meowtils.gui.Module.class; type = type.getSuperclass()) {
            try {
                var candidate = type.getDeclaredField(config);
                Class<?> kind = candidate.getType();
                if (kind.isPrimitive() || kind == String.class || Number.class.isAssignableFrom(kind) || kind == Boolean.class) {
                    candidate.setAccessible(true); field = candidate;
                }
                break;
            } catch (NoSuchFieldException ignored) { }
        }
    }
    public String getName() { return name; }
    public String getConfig() { return config; }
    @SuppressWarnings("unchecked")
    public T getValue() {
        if (field == null && owner instanceof wtf.tatp.meowtils.gui.Module module && module.settingsStorage().containsKey(config)) {
            Object current=module.settingsStorage().get(config);
            if (current instanceof Number n) {
                if (value instanceof Double) return (T) Double.valueOf(n.doubleValue());
                if (value instanceof Integer) return (T) Integer.valueOf(n.intValue());
            }
            return (T) current;
        }
        if (field != null) try {
            Object current = field.get(owner);
            if (current instanceof Number n) {
                if (value instanceof Double) return (T) Double.valueOf(n.doubleValue());
                if (value instanceof Integer) return (T) Integer.valueOf(n.intValue());
            }
            if (current != null) return (T) current;
        } catch (IllegalAccessException exception) { throw new IllegalStateException(exception); }
        return value;
    }
    public void setValue(T value) {
        this.value = value;
        if (owner instanceof wtf.tatp.meowtils.gui.Module module && config != null) module.settingsStorage().put(config,value);
        if (field != null) try {
            Object converted = value;
            if (value instanceof Number n) {
                Class<?> kind = field.getType();
                if (kind == int.class || kind == Integer.class) converted = n.intValue();
                else if (kind == float.class || kind == Float.class) converted = n.floatValue();
                else if (kind == long.class || kind == Long.class) converted = n.longValue();
            }
            field.set(owner, converted);
        } catch (IllegalAccessException exception) { throw new IllegalStateException(exception); }
    }
}
