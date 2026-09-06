package wtf.tatp.meowtils.module.antisnipe;

import java.util.List;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.SendPacketEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.lists.BlacklistManager;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.util.MojangNameToUUID;
import wtf.tatp.meowtils.util.NameUtil;
import wtf.tatp.meowtils.util.Settings;

public final class AutoBlacklist extends Module {
    @Config public boolean enabled;
    @Config public int key;
    @Config public boolean showNotifications = true;
    @Config public boolean forFlags = true;
    @Config public boolean flagAutoblock = true;
    @Config public boolean flagNoslow = true;
    @Config public boolean flagKillaura = true;
    @Config public boolean flagLegitScaffold = true;
    @Config public boolean forReports = true;
    @Config public boolean whenReportCommand = true;
    @Config public boolean whenWdrCommand = true;

    public AutoBlacklist() {
        super("AutoBlacklist", Category.Antisnipe);
        tag(ModuleTag.LEGIT);
        tooltip("Automatically adds players to the blacklist for selected events.\n§cNOTE: Some features require AntiCheat module to be enabled.");
        addToggle(new ToggleValue("Show notifications", "showNotifications", this));
        addExpand(new ExpandValue("For flags", e -> {
            e.addToggle(new ToggleValue("Enabled", "forFlags", this));
            e.addCheck(new CheckValue("When flags §cAutoBlock", "flagAutoblock", this));
            e.addCheck(new CheckValue("When flags §cNoSlow", "flagNoslow", this));
            e.addCheck(new CheckValue("When flags §cKillaura", "flagKillaura", this));
            e.addCheck(new CheckValue("When flags §cLegit Scaffold", "flagLegitScaffold", this));
        }, this));
        addExpand(new ExpandValue("For reports", e -> {
            e.addToggle(new ToggleValue("Enabled", "forReports", this));
            e.addCheck(new CheckValue("When §b/report", "whenReportCommand", this));
            e.addCheck(new CheckValue("When §b/wdr", "whenWdrCommand", this));
        }, this));
    }

    @EventTarget
    public void onSend(SendPacketEvent event) {
        if (!(event.getPacket() instanceof ServerboundChatCommandPacket packet) || !Settings.bool(this, "forReports", forReports)) return;
        String command = packet.command().trim();
        String[] args = command.split("\\s+");
        if (args.length < 2) return;
        String root = args[0].toLowerCase(java.util.Locale.ROOT);
        boolean wdr = root.equals("wdr") || root.equals("watchdogreport");
        boolean report = root.equals("report");
        if (wdr && Settings.bool(this, "whenWdrCommand", whenWdrCommand)) {
            blacklistPlayer(args[1], args.length > 2 ? BlacklistManager.formatReasons(java.util.Arrays.copyOfRange(args, 2, args.length)) : "cheating");
        } else if (report && Settings.bool(this, "whenReportCommand", whenReportCommand)) {
            blacklistPlayer(args[1], args.length > 2 ? BlacklistManager.formatReasons(java.util.Arrays.copyOfRange(args, 2, args.length)) : "cheating");
        }
    }

    public static void blacklistPlayer(String player, String reasons) {
        MojangNameToUUID.lookup(player, uuid -> {
            String key = uuid != null ? uuid : player;
            if (!BlacklistManager.isBlacklisted(key) && !BlacklistManager.isBlacklisted(player)) sendNotification(player);
            BlacklistManager.appendReason(key, reasons);
        });
    }

    private static void sendNotification(String player) {
        AutoBlacklist module = get(AutoBlacklist.class);
        if (module == null || !Settings.bool(module, "showNotifications", true)) return;
        if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) {
            Meowtils.addMessage("§cAuto-blacklisted §r" + NameUtil.getTabDisplayName(player) + "§c.");
        }
        if (Notifications.getMode() != Notifications.Mode.CHAT) {
            NotificationManager.show("AutoBlacklist", NameUtil.getTabDisplayName(player), NotificationManager.Type.ALERT, 1000L);
        }
    }
}
