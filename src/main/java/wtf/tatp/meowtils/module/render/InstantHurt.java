package wtf.tatp.meowtils.module.render;

import net.minecraft.world.entity.player.Player;
import wtf.tatp.meowtils.event.AttackEntityEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.util.TeamUtil;

/** Starts the client hurt overlay as soon as the local player attacks. */
public final class InstantHurt extends Module {
    public InstantHurt() {
        super("InstantHurt", Category.Render);
        tag(ModuleTag.LEGIT);
        tooltip("Instantly render hurt animation on players clientside.");
    }

    @EventTarget
    public void onAttack(AttackEntityEvent event) {
        if (!(event.getTarget() instanceof Player player) || TeamUtil.isBot(player) || player.hurtTime > 0) return;
        player.animateHurt(0.0f);
        if (player.hurtTime <= 0) {
            player.hurtDuration = 10;
            player.hurtTime = 10;
        }
    }
}
