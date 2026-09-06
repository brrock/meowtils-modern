package wtf.tatp.meowtils.module.bedwars;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;

/** Alerts when an enemy buys chain/iron/diamond armor. */
public final class ArmorAlerts extends Module {
    private static int tickCounter;
    private static final Map<UUID, Integer> ALERTED_ARMOR = new HashMap<>();

    public ArmorAlerts() {
        super("ArmorAlerts", Category.Bedwars);
        tag(ModuleTag.SAFE);
        tooltip("Alerts you when players buy an armor upgrade.");
        addMode(new ModeValue("Alert", List.of("Chat", "Notification", "All"), "alertType", this));
        addToggle(new ToggleValue("Ping sound", "pingSound", this));
        addCheck(new CheckValue("§7Chainmail Armor", "chain", this));
        addCheck(new CheckValue("Iron Armor", "iron", this));
        addCheck(new CheckValue("§bDiamond Armor", "diamond", this));
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        if (!BedwarsSupport.inMatch()) return;
        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;
        for (Player player : mc.level.players()) {
            if (player.tickCount < 60 || BedwarsSupport.skipPlayer(player, true) || skipFriend(player)) continue;
            checkArmor(player);
        }
    }

    private void checkArmor(Player player) {
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        if (leggings.isEmpty()) return;
        int current = priority(leggings);
        if (current <= 0) return;
        if (current == 1 && !Settings.bool(this, "chain", true)) return;
        if (current == 2 && !Settings.bool(this, "iron", true)) return;
        if (current == 3 && !Settings.bool(this, "diamond", true)) return;
        int last = ALERTED_ARMOR.getOrDefault(player.getUUID(), 0);
        if (current <= last) return;
        ALERTED_ARMOR.put(player.getUUID(), current);
        String armorName = current == 1 ? "Chainmail Armor" : current == 2 ? "Iron Armor" : "Diamond Armor";
        String color = current == 1 ? "§8" : current == 2 ? "§f" : "§b";
        String text = BedwarsSupport.displayName(player) + "§7 purchased " + color + armorName;
        BedwarsSupport.alert(this, text, "ArmorAlerts", text, Settings.text(this, "alertType", "Chat"));
        if (Settings.bool(this, "pingSound", true)) BedwarsSupport.play(BedwarsSupport.Sound.PING);
    }

    private static boolean skipFriend(Player player) {
        return TeamUtil.ignoreFriends(player.getUUID().toString()) || TeamUtil.ignoreFriends(player.getGameProfile().name());
    }

    private static int priority(ItemStack stack) {
        if (ItemIds.is(stack, "chainmail_leggings", "chain_leggings")) return 1;
        if (ItemIds.is(stack, "iron_leggings")) return 2;
        if (ItemIds.is(stack, "diamond_leggings")) return 3;
        return 0;
    }

    @Override
    public void onReset() {
        ALERTED_ARMOR.clear();
    }
}
