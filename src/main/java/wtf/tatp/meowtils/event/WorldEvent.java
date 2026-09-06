package wtf.tatp.meowtils.event;

import net.minecraft.client.multiplayer.ClientLevel;
import wtf.tatp.meowtils.event.api.Event;

public final class WorldEvent extends Event {
    public enum Type { LOAD, UNLOAD }
    private final ClientLevel world;
    private final Type type;
    public WorldEvent(ClientLevel world) { this(world, Type.LOAD); }
    public WorldEvent(ClientLevel world, Type type) { this.world = world; this.type = type; }
    public ClientLevel getWorld() { return world; }
    public Type getType() { return type; }
}
