package wtf.tatp.meowtils.handler;

import wtf.tatp.meowtils.event.WorldEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.ModuleManager;
import wtf.tatp.meowtils.manager.session.SessionManager;
import wtf.tatp.meowtils.util.TeamUtil;

/** World-load reset matching 2.0.1 ResetHandler. */
public final class ResetHandler {
    @EventTarget
    public void onWorld(WorldEvent event) {
        TeamUtil.reset();
        SessionManager.resetStates();
        PartyHandler.reset();
        if (event.getType() == WorldEvent.Type.LOAD) {
            for (Module module : ModuleManager.getModules()) module.reset();
        }
    }
}
