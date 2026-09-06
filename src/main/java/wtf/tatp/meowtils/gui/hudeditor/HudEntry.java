package wtf.tatp.meowtils.gui.hudeditor;

import java.lang.reflect.Field;
import java.util.function.Supplier;
import wtf.tatp.meowtils.gui.Module;

/** Source-compatible HUD position adapter for native modules and extensions. */
public final class HudEntry {
    public float scale=1;
    private final Module owner;
    private final String name;
    private final Field x, y;
    private final Supplier<int[]> bounds;
    public HudEntry(String name, Module owner, String xField, String yField, Supplier<int[]> bounds) {
        this.name=name; this.owner=owner; this.bounds=bounds;
        x=field(owner,xField); y=field(owner,yField);
    }
    private static Field field(Object owner, String name) {
        for (Class<?> type=owner.getClass(); type!=null; type=type.getSuperclass()) try {
            Field field=type.getDeclaredField(name); field.setAccessible(true); return field;
        } catch (NoSuchFieldException ignored) { }
        throw new IllegalArgumentException("Missing HUD coordinate: "+name);
    }
    private int get(Field field) { try { return field.getInt(owner); } catch (IllegalAccessException e) { throw new IllegalStateException(e); } }
    private void set(Field field, int value) { try { field.setInt(owner,value); } catch (IllegalAccessException e) { throw new IllegalStateException(e); } }
    public int getX() { return get(x); }
    public int getY() { return get(y); }
    public void setX(int value) { set(x,value); }
    public void setY(int value) { set(y,value); }
    public String getName() { return name==null ? owner.getName() : name; }
    public Module getOwner() { return owner; }
    public int[] getBounds() { int[] b=bounds.get(); return b==null || b.length<2 ? new int[]{1,1} : new int[]{Math.max(1,b[0]), Math.max(1,b[1])}; }
}
