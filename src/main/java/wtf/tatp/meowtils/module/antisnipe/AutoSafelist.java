package wtf.tatp.meowtils.module.antisnipe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.lists.SafelistManager;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.util.MojangNameToUUID;
import wtf.tatp.meowtils.util.NameUtil;
import wtf.tatp.meowtils.util.Settings;

public final class AutoSafelist extends Module {
    private static final Pattern FINAL_KILL = Pattern.compile("^([A-Za-z0-9_]+)(?=[\\s'])");
    @Config public boolean enabled;
    @Config public int key;
    @Config public boolean autoSafelistMessage = true;

    public AutoSafelist() {
        super("AutoSafelist", Category.Antisnipe);
        tag(ModuleTag.LEGIT);
        tooltip("Automatically safelists players that take a final death.");
        addToggle(new ToggleValue("Safelist feedback", "autoSafelistMessage", this));
    }

    @EventTarget
    public void onChat(ChatReceivedEvent event) {
        if (event.isOverlay() || Bedwars.GAME.isNotActive()) return;
        String msg = event.getText();
        if (!msg.contains("FINAL KILL!")) return;
        Matcher matcher = FINAL_KILL.matcher(msg);
        if (!matcher.find()) return;
        String player = matcher.group(1);
        if ((mc.player != null && player.equals(mc.player.getScoreboardName())) || Server.HYPIXEL_REPLAY.isActive()) return;
        MojangNameToUUID.lookup(player, uuid -> {
            if ((uuid != null && SafelistManager.isSafelisted(uuid)) || SafelistManager.isSafelisted(player)) return;
            if (uuid != null) SafelistManager.add(uuid);
            else SafelistManager.add(player);
            if (!Settings.bool(this, "autoSafelistMessage", autoSafelistMessage)) return;
            if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) {
                Meowtils.addMessage("§aAuto-safelisted §r" + NameUtil.getTabDisplayName(player) + "§a.");
            }
            if (Notifications.getMode() != Notifications.Mode.CHAT) {
                NotificationManager.show("AutoSafelist", NameUtil.getTabDisplayName(player), NotificationManager.Type.INFO, 1000L);
            }
        });
    }
}
