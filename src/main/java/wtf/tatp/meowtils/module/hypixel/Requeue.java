package wtf.tatp.meowtils.module.hypixel;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import wtf.tatp.meowtils.CommandManager;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.WorldEvent;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.DelayedTask;
import wtf.tatp.meowtils.util.HypixelUtil;
import wtf.tatp.meowtils.util.Settings;

public final class Requeue extends Module {
    private static String detectedMode = "";
    private static int ticks;
    private static boolean scoreboardAvailable;
    private static boolean awaitingLocraw;
    private static boolean pressed;
    private static LocalPlayer joinedPlayer;
    private static ClientLevel joinedLevel;

    public Requeue() {
        super("Requeue", Category.Hypixel);
        tag(ModuleTag.LEGIT);
        tooltip("Allows you to manually or automatically requeue last played Hypixel game.\n§d/rq §f- Requeue last played game");
        CommandManager.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("rq").executes(c -> {
            requeue();
            return 1;
        }));
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        if (event.getType() == WorldEvent.Type.LOAD) beginLocraw();
        if (event.getType() == WorldEvent.Type.UNLOAD) {
            joinedPlayer = null;
            joinedLevel = null;
        }
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        if (joinedPlayer != mc.player || joinedLevel != mc.level) {
            joinedPlayer = mc.player;
            joinedLevel = mc.level;
            beginLocraw();
        }
        if (Server.HYPIXEL.isActive()) {
            if (awaitingLocraw && !scoreboardAvailable && mc.level.getScoreboard() != null) {
                scoreboardAvailable = true;
            }
            if (scoreboardAvailable) {
                ticks++;
                if (ticks == 20) Meowtils.sendCleanMessage("/locraw");
            }
        }
        int requeueKey = Settings.integer(this, "requeueKey", 0);
        if (requeueKey != 0) {
            boolean held = InputConstants.isKeyDown(mc.getWindow(), requeueKey);
            if (held && !pressed) requeue();
            pressed = held;
        }
    }

    @EventTarget
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.isOverlay()) return;
        String msg = ColorUtil.unformattedText(event.getText()).trim();
        if (awaitingLocraw) {
            if (!msg.startsWith("{")) return;
            awaitingLocraw = false;
            try {
                if (!msg.contains("dynamic") && !msg.contains("REPLAY") && !msg.contains("hub")
                        && !msg.equals("{\"server\":\"limbo\"}") && !msg.equals("HOUSING")) {
                    int index = msg.indexOf("mode\":\"");
                    if (index != -1) {
                        detectedMode = msg.substring(index + 7).split("\"")[0];
                        settingsStorage().put("lastPlayCommand", detectedMode);
                        ConfigManager.save();
                        if (Settings.bool(this, "feedback", true)) {
                            if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) {
                                Meowtils.addMessage(ChatFormatting.GREEN + "Saved play command: "
                                        + ChatFormatting.WHITE + ChatFormatting.ITALIC + detectedMode);
                            }
                            if (Notifications.getMode() != Notifications.Mode.CHAT) {
                                NotificationManager.show("Requeue", ChatFormatting.BLUE + "Saved command",
                                        NotificationManager.Type.INFO, 1500L);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!Settings.bool(this, "autoRequeue", false) || msg.contains(":")) return;
        if (HypixelUtil.isGameEnd(msg)) {
            int delay = Settings.integer(this, "autoRequeueDelay", 4) * 20;
            new DelayedTask(Requeue::requeue, delay);
        }
    }

    @EventTarget(priority = EventPriority.LOWEST)
    public void cancelLocraw(ChatReceivedEvent event) {
        if (event.isOverlay()) return;
        if (ColorUtil.unformattedText(event.getText()).trim().startsWith("{")) event.setCancelled(true);
    }

    private static void beginLocraw() {
        scoreboardAvailable = false;
        awaitingLocraw = true;
        ticks = 0;
    }

    public static void requeue() {
        Requeue r = get(Requeue.class);
        if (r == null || r.mc.player == null || r.mc.level == null) return;
        if (!r.getState()) {
            Meowtils.addMessage("Enable " + ChatFormatting.BLUE + "Requeue" + ChatFormatting.WHITE + " module to use this!");
            return;
        }
        if (Server.HYPIXEL.isNotActive()) {
            Meowtils.addMessage(ChatFormatting.RED + "This is only supported on " + ChatFormatting.GOLD + "Hypixel"
                    + ChatFormatting.RED + ".");
            return;
        }
        String last = Settings.text(r, "lastPlayCommand", "");
        if (!last.isEmpty()) {
            if (Settings.bool(r, "feedback", true)) {
                if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) {
                    Meowtils.addMessage("Requeuing..");
                }
                if (Notifications.getMode() != Notifications.Mode.CHAT) {
                    NotificationManager.show("Requeue", ChatFormatting.BLUE + "Requeuing..",
                            NotificationManager.Type.INFO, 1500L);
                }
            }
            Meowtils.sendCleanMessage("/play " + last);
            return;
        }
        Meowtils.addMessage(ChatFormatting.RED + "No previously saved /play command found.");
    }
}
