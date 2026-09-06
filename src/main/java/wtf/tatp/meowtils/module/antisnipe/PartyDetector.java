package wtf.tatp.meowtils.module.antisnipe;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.ReceivePacketEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.util.DelayedTask;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.Util;

public final class PartyDetector extends Module {
    @Config public boolean enabled;
    @Config public int key;
    @Config public boolean sound = true;
    @Config public boolean twos = true;
    @Config public boolean threes = true;
    @Config public boolean foursNormal = true;
    @Config public boolean foursTwo = true;
    @Config public boolean showMissed = true;
    private static int playerCounter;
    private static long lastJoinTime;
    private static boolean countingPlayers;
    private static int missedCounter;
    private static boolean alertedMissed;
    private static int tickCounter;
    private static boolean gameStarted;

    public PartyDetector() {
        super("PartyDetector", Category.Antisnipe);
        tag(ModuleTag.LEGIT);
        tooltip("Detects when a party joins your lobby.");
        addToggle(new ToggleValue("Ping sound", "sound", this));
        addToggle(new ToggleValue("Show missed players", "showMissed", this));
        addCheck(new CheckValue("Bedwars 2s", "twos", this));
        addCheck(new CheckValue("Bedwars 3s", "threes", this));
        addCheck(new CheckValue("Bedwars 4s", "foursNormal", this));
        addCheck(new CheckValue("Bedwars 4v4", "foursTwo", this));
    }

    @EventTarget
    public void onPacket(ReceivePacketEvent event) {
        if (!(event.getPacket() instanceof ClientboundAddEntityPacket packet) || packet.getType() != EntityTypes.PLAYER) return;
        if (mc.player != null && packet.getUUID().equals(mc.player.getUUID())) return;
        if (Bedwars.PRE_GAME.isNotActive() || getBedwarsMode() == 0) return;
        long now = System.currentTimeMillis();
        if (!countingPlayers) lastJoinTime = now;
        if (now - lastJoinTime <= 1000) {
            playerCounter++;
            countingPlayers = true;
        } else {
            countingPlayers = false;
            lastJoinTime = 0;
            playerCounter = 0;
        }
        if (playerCounter != 0 && playerCounter >= getBedwarsMode()) {
            int size = getBedwarsMode();
            new DelayedTask(() -> {
                if (gameStarted) return;
                Meowtils.addMessage("§cWarning: §e" + size + "§f players joined! §8(§9Party§8)");
                if (Settings.bool(this, "sound", sound)) Util.playSound(Util.Sound.PING_DEEP, 100);
            }, 1);
            playerCounter = 0;
            lastJoinTime = 0;
            countingPlayers = false;
        }
    }

    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null || event.getPhase() != ClientTickEvent.Phase.POST
                || Bedwars.PRE_GAME.isNotActive() || !Settings.bool(this, "showMissed", showMissed) || alertedMissed) return;
        tickCounter++;
        if (tickCounter < 10) return;
        for (Player player : mc.level.players()) {
            if (player != null && player != mc.player && player.getUUID().version() == 2) missedCounter++;
        }
        if (missedCounter != 0) {
            if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) Meowtils.addMessage("Missed players: §e" + missedCounter);
            if (Notifications.getMode() != Notifications.Mode.CHAT) {
                NotificationManager.show("PartyDetector", "Missed: §e" + missedCounter, NotificationManager.Type.INFO, 1500L);
            }
        }
        alertedMissed = true;
    }

    @EventTarget
    public void onChat(ChatReceivedEvent event) {
        if (!event.isOverlay() && event.getText().contains("The game starts in 1 second!")) gameStarted = true;
    }

    private int getBedwarsMode() {
        if (Bedwars.FOUR_FOUR.isActive() && Settings.bool(this, "foursTwo", foursTwo)) return 4;
        if (Bedwars.FOURS.isActive() && Settings.bool(this, "foursNormal", foursNormal)) return 4;
        if (Bedwars.THREES.isActive() && Settings.bool(this, "threes", threes)) return 3;
        return Bedwars.DOUBLES.isActive() && Settings.bool(this, "twos", twos) ? 2 : 0;
    }

    @Override
    public void onReset() {
        playerCounter = 0;
        lastJoinTime = 0;
        countingPlayers = false;
        alertedMissed = false;
        missedCounter = 0;
        tickCounter = 0;
        gameStarted = false;
    }
}
