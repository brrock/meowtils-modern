package wtf.tatp.meowtils.event;

import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.event.api.Event;

public final class ClientTickEvent extends Event {
    public enum Phase { PRE, POST }
    private final Minecraft client;
    private final Phase phase;
    public ClientTickEvent(Minecraft client) { this(client, Phase.POST); }
    public ClientTickEvent(Minecraft client, Phase phase) { this.client = client; this.phase = phase; }
    public Minecraft getClient() { return client; }
    public Phase getPhase() { return phase; }
}
