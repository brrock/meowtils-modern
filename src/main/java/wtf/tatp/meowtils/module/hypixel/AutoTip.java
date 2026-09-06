package wtf.tatp.meowtils.module.hypixel;

import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.util.Settings;

public final class AutoTip extends Module {
    private static int tickCounter;
    private static long lastTipTime;

    public AutoTip() {
        super("AutoTip", Category.Hypixel);
        tag(ModuleTag.LEGIT);
        tooltip("Automatically runs /tipall every x minutes.");
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (mc.level == null || mc.player == null || event.getPhase() != ClientTickEvent.Phase.POST || Server.HYPIXEL.isNotActive()) {
            return;
        }
        tickCounter++;
        if (tickCounter < 200) return;
        tickCounter = 0;
        long currentTime = System.currentTimeMillis();
        long tipDelay = ((long) Settings.integer(this, "delay", 5)) * 60_000L;
        if (currentTime - lastTipTime >= tipDelay) {
            Meowtils.sendCleanMessage("/tip all");
            lastTipTime = currentTime;
        }
    }

    @EventTarget(priority = EventPriority.LOWEST)
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.isOverlay() || Server.HYPIXEL.isNotActive() || !Settings.bool(this, "hide", true)) return;
        String msg = event.getText();
        if (msg.contains("You tipped") || msg.contains("You already tipped everyone")
                || msg.contains("No one has a network booster active right now! Try again later.")) {
            event.setCancelled(true);
        }
    }
}
