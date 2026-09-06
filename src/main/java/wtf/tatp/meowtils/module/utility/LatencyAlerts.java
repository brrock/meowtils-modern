package wtf.tatp.meowtils.module.utility;

import net.minecraft.ChatFormatting;
import net.minecraft.world.scores.DisplaySlot;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.handler.LatencyHandler;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.util.Settings;

import java.util.List;

/** Warns when inbound packets stall, using the always-on LatencyHandler clock. */
public final class LatencyAlerts extends Module {
    private long lastAlert;

    public LatencyAlerts() {
        super("LatencyAlerts", Category.Utility);
        addMode(new ModeValue("Alert", List.of("Chat", "Notification", "All"), "alertType", this));
        addSlider(new SliderValue("Latency threshold", 0, 3000, 50, "ms", "threshold", this, Integer.class));
        addToggle(new ToggleValue("Ignore limbo", "ignoreLimbo", this));
        tooltip("Warns you in chat when you lose connection to the server. \n May not work on all servers.");
        tag(ModuleTag.LEGIT);
    }

    public static void markPacketReceived() {
        LatencyHandler.markPacketReceived();
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null || mc.isLocalServer()) {
            return;
        }
        var scoreboard = mc.level.getScoreboard();
        var sidebar = scoreboard == null ? null : scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (sidebar == null && Settings.bool(this, "ignoreLimbo", true)) return;
        long now = System.currentTimeMillis();
        long last = now - LatencyHandler.getLastPacket();
        if (last < Settings.integer(this, "threshold", 500) || now - lastAlert < 3000) return;
        String alertType = Settings.text(this, "alertType", "Chat");
        if (!alertType.equals("Notification")) {
            Meowtils.addMessage(ChatFormatting.DARK_GRAY + "Packet loss detected: " + ChatFormatting.RED + last + "ms");
        }
        if (!alertType.equals("Chat")) {
            NotificationManager.show("LatencyAlerts", ChatFormatting.RED + String.valueOf(last) + "ms",
                    NotificationManager.Type.ALERT, 2000L);
        }
        lastAlert = now;
    }
}
