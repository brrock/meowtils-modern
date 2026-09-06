package wtf.tatp.meowtils.module.hypixel;

import java.util.LinkedList;
import java.util.Queue;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ButtonValue;
import wtf.tatp.meowtils.handler.PartyHandler;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.Settings;

public final class PartyNotifier extends Module {
    private static final String ANTICHEAT_DEFAULT = "#player failed #check";
    private static final String DENICKER_DEFAULT = "#player is nicked.";
    private static final String DENICKER_FULL_DEFAULT = "#denicked is nicked as #name.";
    private static final String ARMORALERTS_DEFAULT = "#player purchased #armor";
    private static final String BEDTRACKER_DEFAULT = "#player is #distance blocks from your bed! #warning";
    private static final String CONSUMEALERTS_DEFAULT = "#player consumed #item";
    private static final String ITEMALERTS_DEFAULT = "#player has #item";
    private static final String UPGRADEALERTS_DEFAULT = "#team purchased #upgrade";
    private static final String PARTYDETECTOR_DEFAULT = "Warning: #amount players joined! (Party)";
    private static final String URCHIN_DEFAULT = "#player is tagged on Urchin for #tag";
    private static final Queue<String> MESSAGE_QUEUE = new LinkedList<>();
    private static int tickCounter;

    public PartyNotifier() {
        super("PartyNotifier", Category.Hypixel);
        tag(ModuleTag.LEGIT);
        tooltip("Sends a notification to party chat from selected modules.");
        addButton(new ButtonValue("Reset messages", 5.0f, () -> {
            resetDefault();
            if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) Meowtils.addMessage("Reset all PartyNotifier messages.");
            if (Notifications.getMode() != Notifications.Mode.CHAT) {
                NotificationManager.show("PartyNotifier", "Reset messages.", NotificationManager.Type.INFO, 2000L);
            }
        }));
    }

    private static PartyNotifier p() { return get(PartyNotifier.class); }

    private static void resetDefault() {
        PartyNotifier module = p();
        if (module == null) return;
        module.settingsStorage().put("antiCheatMessage", ANTICHEAT_DEFAULT);
        module.settingsStorage().put("denickerMessage", DENICKER_DEFAULT);
        module.settingsStorage().put("denickerMessageFull", DENICKER_FULL_DEFAULT);
        module.settingsStorage().put("armorAlertsMessage", ARMORALERTS_DEFAULT);
        module.settingsStorage().put("bedTrackerMessage", BEDTRACKER_DEFAULT);
        module.settingsStorage().put("consumeAlertsMessage", CONSUMEALERTS_DEFAULT);
        module.settingsStorage().put("itemAlertsMessage", ITEMALERTS_DEFAULT);
        module.settingsStorage().put("upgradeAlertsMessage", UPGRADEALERTS_DEFAULT);
        module.settingsStorage().put("partyDetectorMessage", PARTYDETECTOR_DEFAULT);
        module.settingsStorage().put("urchinMessage", URCHIN_DEFAULT);
        ConfigManager.save();
    }

    private static void notifyParty(String message) { MESSAGE_QUEUE.add(message); }

    private void alert(String message) {
        if (Server.HYPIXEL.isNotActive()) return;
        Meowtils.debugMessage("§e[Notify]: §fSent: " + message);
        if (!PartyHandler.inParty()) return;
        if (Settings.bool(this, "showPrefix", true)) Meowtils.sendMessage(message);
        else Meowtils.sendCleanMessage("/pc " + message);
    }

    private static void handleError(Exception e) {
        resetDefault();
        Meowtils.addMessage("§cThere was an error in PartyNotifier. It has been reset.");
        Meowtils.error("Error in PartyNotifier: " + e);
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null || event.getPhase() != ClientTickEvent.Phase.POST) return;
        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;
        if (MESSAGE_QUEUE.size() > 20) {
            MESSAGE_QUEUE.clear();
            Meowtils.addMessage("§cError notifying party. Cleared queue.");
        }
        if (!MESSAGE_QUEUE.isEmpty()) alert(MESSAGE_QUEUE.poll());
    }

    public static void antiCheat(String player, String check) {
        PartyNotifier module = p();
        if (module == null || !module.getState() || !Settings.bool(module, "antiCheat", true)) return;
        try { notifyParty(Settings.text(module, "antiCheatMessage", ANTICHEAT_DEFAULT).replace("#player", ColorUtil.unformattedText(player)).replace("#check", check)); }
        catch (Exception e) { handleError(e); }
    }

    public static void denicker(String player, String denicked, boolean full) {
        PartyNotifier module = p();
        if (module == null || !module.getState() || !Settings.bool(module, "denicker", true)) return;
        try {
            if (full) notifyParty(Settings.text(module, "denickerMessageFull", DENICKER_FULL_DEFAULT).replace("#denicked", denicked).replace("#name", player));
            else notifyParty(Settings.text(module, "denickerMessage", DENICKER_DEFAULT).replace("#player", player));
        } catch (Exception e) { handleError(e); }
    }

    public static void armorAlerts(String player, String armor) {
        PartyNotifier module = p();
        if (module == null || !module.getState() || !Settings.bool(module, "armorAlerts", false)) return;
        try { notifyParty(Settings.text(module, "armorAlertsMessage", ARMORALERTS_DEFAULT).replace("#player", player).replace("#armor", armor)); }
        catch (Exception e) { handleError(e); }
    }

    public static void bedTracker(String player, int distance) {
        PartyNotifier module = p();
        if (module == null || !module.getState() || !Settings.bool(module, "bedTracker", false)) return;
        try { notifyParty(Settings.text(module, "bedTrackerMessage", BEDTRACKER_DEFAULT).replace("#player", player).replace("#distance", String.valueOf(distance)).replace("#warning", "⚠")); }
        catch (Exception e) { handleError(e); }
    }

    public static void consumeAlerts(String player, String item) {
        PartyNotifier module = p();
        if (module == null || !module.getState() || !Settings.bool(module, "consumeAlerts", false)) return;
        try { notifyParty(Settings.text(module, "consumeAlertsMessage", CONSUMEALERTS_DEFAULT).replace("#player", player).replace("#item", item)); }
        catch (Exception e) { handleError(e); }
    }

    public static void itemAlerts(String player, String item) {
        PartyNotifier module = p();
        if (module == null || !module.getState() || !Settings.bool(module, "itemAlerts", false)) return;
        try { notifyParty(Settings.text(module, "itemAlertsMessage", ITEMALERTS_DEFAULT).replace("#player", player).replace("#item", item)); }
        catch (Exception e) { handleError(e); }
    }

    public static void upgradeAlerts(String team, String upgrade) {
        PartyNotifier module = p();
        if (module == null || !module.getState() || !Settings.bool(module, "upgradeAlerts", false)) return;
        try { notifyParty(Settings.text(module, "upgradeAlertsMessage", UPGRADEALERTS_DEFAULT).replace("#team", team).replace("#upgrade", upgrade)); }
        catch (Exception e) { handleError(e); }
    }

    public static void partyDetector(int amount) {
        PartyNotifier module = p();
        if (module == null || !module.getState() || !Settings.bool(module, "partyDetector", false)) return;
        try { notifyParty(Settings.text(module, "partyDetectorMessage", PARTYDETECTOR_DEFAULT).replace("#amount", String.valueOf(amount))); }
        catch (Exception e) { handleError(e); }
    }

    public static void urchin(String name, String tag) {
        PartyNotifier module = p();
        if (module == null || !module.getState() || !Settings.bool(module, "urchin", true)) return;
        try { notifyParty(Settings.text(module, "urchinMessage", URCHIN_DEFAULT).replace("#player", name).replace("#tag", tag)); }
        catch (Exception e) { handleError(e); }
    }
}
