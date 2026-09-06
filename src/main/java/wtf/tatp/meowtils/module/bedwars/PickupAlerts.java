package wtf.tatp.meowtils.module.bedwars;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.ReceivePacketEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;

/** Alerts when another player picks up iron, gold, diamonds, or emeralds. */
public final class PickupAlerts extends Module {
    private static final Map<UUID, Long> COOLDOWN = new HashMap<>();

    public PickupAlerts() {
        super("PickupAlerts", Category.Bedwars);
        tag(ModuleTag.SAFE);
        tooltip("Alerts you when someone picks up a resource.");
        addSlider(new SliderValue("Cooldown", 0, 10, 1, "s", "cooldown", this, Integer.class));
        addToggle(new ToggleValue("Ping sound", "pingSound", this));
        addCheck(new CheckValue("For §7Iron Ingots", "iron", this));
        addCheck(new CheckValue("For §6Gold Ingots", "gold", this));
        addCheck(new CheckValue("For §bDiamonds", "diamonds", this));
        addCheck(new CheckValue("For §2Emeralds", "emeralds", this));
    }

    @EventTarget
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!(event.getPacket() instanceof ClientboundTakeItemEntityPacket packet) || !BedwarsSupport.inMatch()) return;
        if (mc.level == null || mc.player == null) return;
        Entity collector = mc.level.getEntity(packet.getPlayerId());
        Entity itemEntity = mc.level.getEntity(packet.getItemId());
        if (!(collector instanceof Player player) || player == mc.player || BedwarsSupport.skipPlayer(player, false) || skipFriend(player)) return;
        if (!(itemEntity instanceof ItemEntity dropped)) return;
        ItemStack stack = dropped.getItem();
        String kind = BedwarsSupport.resourceKey(stack);
        if (kind.isEmpty() || !allowed(kind)) return;
        long now = System.currentTimeMillis();
        if (now - COOLDOWN.getOrDefault(player.getUUID(), 0L) <= (long) Settings.integer(this, "cooldown", 2) * 1000) return;
        COOLDOWN.put(player.getUUID(), now);
        Meowtils.addMessage(BedwarsSupport.displayName(player) + "§7 picked up " + BedwarsSupport.resourceColor(kind) + stack.getHoverName().getString());
        if (Settings.bool(this, "pingSound", true)) BedwarsSupport.play(BedwarsSupport.Sound.PING_MEDIUM);
    }

    private static boolean skipFriend(Player player) {
        return TeamUtil.ignoreFriends(player.getUUID().toString()) || TeamUtil.ignoreFriends(player.getGameProfile().name());
    }

    private boolean allowed(String kind) {
        return switch (kind) {
            case "iron" -> Settings.bool(this, "iron", false);
            case "gold" -> Settings.bool(this, "gold", false);
            case "diamond" -> Settings.bool(this, "diamonds", true);
            case "emerald" -> Settings.bool(this, "emeralds", true);
            default -> false;
        };
    }

    @Override
    public void onReset() { COOLDOWN.clear(); }

    @Override
    public void onDisable() { COOLDOWN.clear(); }
}
