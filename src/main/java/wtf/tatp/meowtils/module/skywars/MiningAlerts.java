package wtf.tatp.meowtils.module.skywars;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.world.entity.player.Player;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ReceivePacketEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.NameUtil;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.Util;

public final class MiningAlerts extends Module {
    @Config public boolean enabled;
    @Config public int key;
    @Config public boolean sound = true;
    @Config public String alertType = "Chat";

    public MiningAlerts() {
        super("MiningAlerts", Category.Skywars);
        tag(ModuleTag.SAFE);
        tooltip("Alerts you when a player mines Diamond Ore.");
        addMode(new ModeValue("Alert", List.of("Chat", "Notification", "All"), "alertType", this));
        addToggle(new ToggleValue("Ping sound", "sound", this));
    }

    @EventTarget
    public void onPacket(ReceivePacketEvent event) {
        if (mc.level == null || mc.player == null || !(event.getPacket() instanceof ClientboundBlockDestructionPacket packet)) return;
        if (Skywars.GAME.isNotActive() && Skywars.MINI.isNotActive()) return;
        BlockPos pos = packet.getPos();
        if (!ItemIds.isBlock(mc.level.getBlockState(pos), "diamond_ore") || packet.getProgress() != 9) return;
        Player closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (Player player : mc.level.players()) {
            if (player == null || player == mc.player) continue;
            double distance = player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = player;
            }
        }
        if (closest == null) return;
        String text = NameUtil.getTabDisplayName(closest.getScoreboardName()) + "§7 mined §bDiamond Ore";
        String type = Settings.text(this, "alertType", alertType);
        if (!type.equals("Notification")) Meowtils.addMessage(text);
        if (!type.equals("Chat")) NotificationManager.show("MiningAlerts", text, NotificationManager.Type.ALERT, 1500L);
        if (Settings.bool(this, "sound", sound)) Util.playSound(Util.Sound.PING_DEEP, 100);
    }
}
