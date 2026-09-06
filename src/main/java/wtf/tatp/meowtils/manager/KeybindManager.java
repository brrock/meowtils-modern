package wtf.tatp.meowtils.manager;

import net.minecraft.client.KeyMapping;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;

/**
 * Source-compatibility facade for old extensions that imported the manager
 * from its 2.x package. New code should use the static root API.
 */
@Deprecated(forRemoval = false)
public final class KeybindManager {
    public KeybindManager() {}

    public KeyMapping register(String id, int defaultKey) {
        return wtf.tatp.meowtils.KeybindManager.register(id, defaultKey);
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        wtf.tatp.meowtils.KeybindManager.tick();
    }
}
