package wtf.tatp.meowtils.event;

import net.minecraft.client.KeyMapping;
import wtf.tatp.meowtils.event.api.Event;

public final class KeyPressEvent extends Event {
    private final KeyMapping mapping;
    public KeyPressEvent(KeyMapping mapping) { this.mapping = mapping; }
    public KeyMapping getMapping() { return mapping; }
    public String getId() { return mapping.getName(); }
}
