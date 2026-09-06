package wtf.tatp.meowtils.module;

import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ToggleValue;

/** First vanilla-independent built-in port: keeps sprint behavior in a normal module. */
public final class Sprint extends Module {
    public final ToggleValue omni = new ToggleValue("Omni sprint", "omni", this);

    public Sprint() {
        super("Sprint", Category.Utility);
        addToggle(omni);
    }

    @EventTarget
    private void onClientTick(ClientTickEvent event) {
        if (event.getClient().player == null || event.getClient().player.isSpectator()) return;
        var input = event.getClient().player.input;
        if (input == null || event.getClient().player.isUsingItem()) return;
        boolean moving = input.keyPresses.forward() || (omni.get() && (input.keyPresses.backward() || input.keyPresses.left() || input.keyPresses.right()));
        event.getClient().player.setSprinting(moving && !event.getClient().player.isCrouching());
    }
}
