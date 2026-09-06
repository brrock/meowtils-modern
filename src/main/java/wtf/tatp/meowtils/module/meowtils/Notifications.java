package wtf.tatp.meowtils.module.meowtils;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.SoundLoader;
import wtf.tatp.meowtils.manager.lists.BlacklistManager;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.DelayedTask;
import wtf.tatp.meowtils.util.NameUtil;
import wtf.tatp.meowtils.util.Settings;

/** Central notification preferences and 2.0.1 start/layout/mode routing. */
public final class Notifications extends Module {
    @Config public boolean toggle = true;
    @Config public boolean challengeWarning = true;
    @Config public boolean shoutCooldown = true;
    @Config public boolean modWarnings = true;
    @Config public boolean startNotifications = true;
    @Config public String notificationMode = "Chat";
    @Config public boolean blacklistedWarning = true;
    @Config public boolean showBannedPlayer = true;

    private final ModeValue mode = new ModeValue("Prefer", List.of("Notification", "Chat", "Both"), "notificationMode", this);
    private static final HashSet<String> BLACKLISTED_ALERTED = new HashSet<>();
    private static final HashSet<String> PLAYERS_BEFORE = new HashSet<>();
    private static final HashSet<String> PLAYERS_AFTER = new HashSet<>();
    private static final HashSet<UUID> SEEN_PLAYERS = new HashSet<>();
    private static int tickCounter;
    private static boolean shoutAlerted = true;
    public static long lastShout;
    public static int shoutTimeLeft;

    public Notifications() {
        super("Notifications", Category.Meowtils, true);
        addMode(mode);
        addToggle(new ToggleValue("Module toggled", "toggle", this));
        addToggle(new ToggleValue("Start notifications", "startNotifications", this));
        addToggle(new ToggleValue("Mod conflict warnings", "modWarnings", this));
        addToggle(new ToggleValue("Challenge warning", "challengeWarning", this));
        addToggle(new ToggleValue("Shout cooldown", "shoutCooldown", this));
        addToggle(new ToggleValue("Blacklisted warning", "blacklistedWarning", this));
        addToggle(new ToggleValue("Show banned player", "showBannedPlayer", this));
        tooltip("Select which notifications to send in chat.\n§bPrefer §f- Select whether to prefer notifications, chat messages, or both.");
        tag(ModuleTag.SAFE);
    }

    public static Mode getMode() {
        Notifications value = Module.get(Notifications.class);
        if (value == null) return Mode.BOTH;
        return Mode.from(value.mode.getMode());
    }

    public static boolean chat() { return getMode() != Mode.NOTIFICATION; }
    public static boolean overlay() { return getMode() != Mode.CHAT; }

    public static void route(String chatMessage, String title, String overlayMessage, NotificationManager.Type type, long duration) {
        if (chat()) Meowtils.addMessage(chatMessage);
        if (overlay()) NotificationManager.show(title, overlayMessage, type, duration);
    }

    public enum Mode {
        NOTIFICATION, CHAT, BOTH;
        static Mode from(String value) {
            return switch (value) {
                case "Notification" -> NOTIFICATION;
                case "Chat" -> CHAT;
                default -> BOTH;
            };
        }
    }

    @EventTarget
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.isOverlay()) return;
        String msg = ColorUtil.unformattedText(event.getText());
        if (shoutCooldown && msg.contains("[SHOUT]") && mc.player != null
                && msg.contains(mc.player.getGameProfile().name() + ":") && Server.HYPIXEL.isActive()) {
            lastShout = System.currentTimeMillis();
            shoutTimeLeft = 60;
            shoutAlerted = false;
        }
        if (challengeWarning && msg.contains("You can disable Challenges through any NPC in the Bed Wars lobby using the redstone on the Challenges page.") && !msg.contains(":")) {
            new DelayedTask(() -> {
                route(ChatFormatting.DARK_RED.toString() + ChatFormatting.BOLD + "You currently have a "
                                + ChatFormatting.DARK_AQUA + ChatFormatting.BOLD + "bedwars challenge"
                                + ChatFormatting.DARK_RED + ChatFormatting.BOLD + " active!",
                        "Warning", "Challenge active!", NotificationManager.Type.WARNING, 2000L);
                SoundLoader.play(SoundLoader.Sound.MEOW, 100);
            }, 40);
        }
        if (showBannedPlayer && msg.contains("A player has been removed from your game.") && mc.level != null) {
            PLAYERS_BEFORE.clear();
            for (Player player : mc.level.players()) {
                if (player != null) PLAYERS_BEFORE.add(player.getGameProfile().name());
            }
            new DelayedTask(() -> {
                PLAYERS_AFTER.clear();
                if (mc.level != null) {
                    for (Player player : mc.level.players()) {
                        if (player != null) PLAYERS_AFTER.add(player.getGameProfile().name());
                    }
                }
                PLAYERS_BEFORE.removeAll(PLAYERS_AFTER);
                if (PLAYERS_BEFORE.isEmpty()) {
                    Meowtils.addMessage(ChatFormatting.RED + "No players removed!");
                    return;
                }
                for (String name : PLAYERS_BEFORE) {
                    if (name != null && !name.isEmpty()) {
                        Meowtils.addMessage(ChatFormatting.RED + "Player removed: " + ChatFormatting.GRAY + name);
                    }
                }
            }, 20);
        }
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null || event.getPhase() != ClientTickEvent.Phase.POST) return;
        alertBlacklistedPlayers();
        if (Server.HYPIXEL.isNotActive()) return;
        if (!shoutCooldown) {
            shoutAlerted = true;
            shoutTimeLeft = 0;
            lastShout = 0L;
            return;
        }
        if (shoutTimeLeft > 0) {
            tickCounter++;
            if (tickCounter < 20) return;
            tickCounter = 0;
            shoutTimeLeft--;
        }
        if (!shoutAlerted && shoutTimeLeft <= 0) {
            shoutAlerted = true;
            route(ChatFormatting.BOLD + "You may now " + ChatFormatting.RED + ChatFormatting.BOLD + "shout"
                            + ChatFormatting.WHITE + ChatFormatting.BOLD + " again!",
                    "Shout", "Cooldown ended.", NotificationManager.Type.INFO, 1500L);
        }
    }

    private void alertBlacklistedPlayers() {
        if (!blacklistedWarning && !Settings.bool(this, "blacklistedWarning", blacklistedWarning)) return;
        for (Player player : mc.level.players()) {
            if (player == null || player == mc.player) continue;
            if (!SEEN_PLAYERS.add(player.getUUID())) continue;
            String uuid = player.getUUID().toString();
            String name = player.getGameProfile().name();
            String key = BlacklistManager.isBlacklisted(uuid) ? uuid : BlacklistManager.isBlacklisted(name) ? name : null;
            if (key == null || BLACKLISTED_ALERTED.contains(key)) continue;
            String formatted = BlacklistManager.getFormattedEntry(key);
            Meowtils.addMessage(ChatFormatting.RED + "Warning: " + ChatFormatting.RESET
                    + NameUtil.getTabDisplayName(name) + ChatFormatting.GRAY + " is blacklisted since: " + formatted);
            if (overlay()) NotificationManager.show("Blacklisted Player", name, NotificationManager.Type.WARNING, 2000L);
            BLACKLISTED_ALERTED.add(key);
        }
    }

    @Override
    public void onReset() {
        BLACKLISTED_ALERTED.clear();
        PLAYERS_BEFORE.clear();
        PLAYERS_AFTER.clear();
        SEEN_PLAYERS.clear();
        tickCounter = 0;
        shoutAlerted = true;
        lastShout = 0L;
        shoutTimeLeft = 0;
    }
}
