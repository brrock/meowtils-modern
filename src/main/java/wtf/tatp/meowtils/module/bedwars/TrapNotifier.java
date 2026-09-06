package wtf.tatp.meowtils.module.bedwars;

import net.minecraft.world.effect.MobEffects;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.Settings;

/** Warns when a trap fires and reminds you after yours is gone. */
public final class TrapNotifier extends Module {
    private static boolean triggerNotified;
    private static int tickCounter;
    private static String trap = "";
    private static boolean revealTrap;
    private static boolean reminderNotified;
    private static int reminderSeconds = -1;

    public TrapNotifier() {
        super("TrapNotifier", Category.Bedwars);
        tag(ModuleTag.LEGIT);
        tooltip("Reminds when you're missing a trap and notify when you trigger one.");
        addToggle(new ToggleValue("Triggered trap alert", "triggerAlert", this));
        addToggle(new ToggleValue("Missing trap reminder", "missingReminder", this));
        addToggle(new ToggleValue("Ping sound", "sound", this));
    }

    @EventTarget
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.isOverlay() || !BedwarsSupport.inMatch()) return;
        String msg = event.getText();
        String plain = ColorUtil.plainLower(msg);
        if (Settings.bool(this, "triggerAlert", true) && plain.equals("your invisibility was removed by an reveal trap!")) {
            revealTrap = true;
        }
        if (!Settings.bool(this, "missingReminder", true)) return;
        if (plain.contains("trap was set off!")) {
            reminderSeconds = 30;
            reminderNotified = false;
        }
        if (plain.contains("your bed was destroyed")) {
            reminderSeconds = 0;
            reminderNotified = true;
        }
        if (plain.contains("purchased") && plain.contains("trap") && !plain.contains(":")) {
            reminderSeconds = 0;
            reminderNotified = true;
        }
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        if (!BedwarsSupport.inMatch()) return;
        tickCounter++;
        if (tickCounter != 10) return;
        tickCounter = 0;
        boolean currentTrap = mc.player.hasEffect(MobEffects.MINING_FATIGUE) || mc.player.hasEffect(MobEffects.BLINDNESS) || revealTrap;
        if (Settings.bool(this, "triggerAlert", true) && !triggerNotified) {
            if (mc.player.hasEffect(MobEffects.MINING_FATIGUE)) {
                trap = "Miner Fatigue";
                alert();
            } else if (mc.player.hasEffect(MobEffects.BLINDNESS)) {
                trap = "Blindness";
                alert();
            } else if (revealTrap) {
                trap = "Reveal";
                revealTrap = false;
                alert();
            }
        }
        if (!currentTrap) triggerNotified = false;
        if (Settings.bool(this, "missingReminder", true) && reminderSeconds > 0) reminderSeconds--;
        if (Settings.bool(this, "missingReminder", true) && !reminderNotified && reminderSeconds == 0) {
            BedwarsSupport.notifyMode("§c§lYou currently don't have a trap active!", "§c§lYou currently don't have a trap active!",
                    "TrapNotifier", "No trap active!", NotificationManager.Type.WARNING, 2000L);
            if (Settings.bool(this, "sound", true)) BedwarsSupport.play(BedwarsSupport.Sound.ANVIL);
            reminderNotified = true;
        }
    }

    private void alert() {
        String text = "§c§lTRAP TRIGGERED! §8(§6" + trap + "§8)";
        BedwarsSupport.notifyMode(text, text, "TrapNotifier", text, NotificationManager.Type.ALERT, 2000L);
        triggerNotified = true;
        if (Settings.bool(this, "sound", true)) BedwarsSupport.play(BedwarsSupport.Sound.ANVIL);
    }

    @Override
    public void onReset() {
        triggerNotified = false;
        tickCounter = 0;
        trap = "";
        revealTrap = false;
        reminderNotified = false;
        reminderSeconds = -1;
    }
}
