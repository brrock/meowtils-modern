package wtf.tatp.meowtils.util;

import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventManager;
import wtf.tatp.meowtils.event.api.EventTarget;

/** Runs a client-side callback after a number of PRE ticks, matching the 2.0.1 helper. */
public final class DelayedTask {
    private final Runnable runnable;
    private int counter;

    public DelayedTask(Runnable task, int ticks) {
        this.runnable = task;
        this.counter = ticks;
        EventManager.register(this);
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.PRE) return;
        int remaining = this.counter;
        this.counter = remaining - 1;
        if (remaining > 0) return;
        EventManager.unregister(this);
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        try {
            runnable.run();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
