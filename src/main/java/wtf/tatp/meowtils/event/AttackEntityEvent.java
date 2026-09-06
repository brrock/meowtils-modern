package wtf.tatp.meowtils.event;

import net.minecraft.world.entity.Entity;
import wtf.tatp.meowtils.event.api.Event;

public final class AttackEntityEvent extends Event {
    private final Entity target;
    public AttackEntityEvent(Entity target) { this.target = target; }
    public Entity getTarget() { return target; }
}
