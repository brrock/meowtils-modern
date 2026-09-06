package wtf.tatp.meowtils.module.skywars;

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
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.module.bedwars.BedwarsSupport;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.NameUtil;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;

public final class EquipAlerts extends Module {
    private static final Map<UUID, ItemStack[]> LAST = new HashMap<>();
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final String[] KEYS = {"helmet", "chestplate", "leggings", "boots"};

    public EquipAlerts() {
        super("EquipAlerts", Category.Skywars);
        tag(ModuleTag.SAFE);
        tooltip("Alerts when a player equips selected armor piece.");
        addMode(new ModeValue("Alert", List.of("Chat", "Notification", "All"), "alertType", this));
        addToggle(new ToggleValue("Ping sound", "sound", this));
        addCheck(new CheckValue("§bDiamond Helmet", "helmet", this));
        addCheck(new CheckValue("§bDiamond Chestplate", "chestplate", this));
        addCheck(new CheckValue("§bDiamond Leggings", "leggings", this));
        addCheck(new CheckValue("§bDiamond Boots", "boots", this));
    }

    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.level == null || mc.player == null || Skywars.GAME.isNotActive()) return;
        for (Player player : mc.level.players()) {
            if (player == mc.player || skipFriend(player)) continue;
            ItemStack[] now = armor(player);
            ItemStack[] previous = LAST.put(player.getUUID(), now);
            if (previous == null) continue;
            for (int i = 0; i < now.length; i++) {
                if (ItemStack.matches(previous[i], now[i])) continue;
                String name = diamondName(now[i], KEYS[i]);
                if (name == null || !Settings.bool(this, KEYS[i], true)) continue;
                String text = NameUtil.getTabDisplayName(player.getGameProfile().name()) + "§7 equipped §3" + name;
                BedwarsSupport.alert(this, text, "EquipAlerts", text, Settings.text(this, "alertType", "Chat"));
                if (Settings.bool(this, "sound", false)) BedwarsSupport.play(BedwarsSupport.Sound.PING_MEDIUM);
            }
        }
    }

    private static boolean skipFriend(Player player) {
        return TeamUtil.ignoreFriends(player.getUUID().toString()) || TeamUtil.ignoreFriends(player.getGameProfile().name());
    }

    private static ItemStack[] armor(Player player) {
        ItemStack[] stacks = new ItemStack[4];
        for (int i = 0; i < SLOTS.length; i++) stacks[i] = player.getItemBySlot(SLOTS[i]).copy();
        return stacks;
    }

    private static String diamondName(ItemStack stack, String key) {
        if (stack == null || stack.isEmpty() || !ItemIds.is(stack, "diamond_" + key)) return null;
        return switch (key) {
            case "helmet" -> "Diamond Helmet";
            case "chestplate" -> "Diamond Chestplate";
            case "leggings" -> "Diamond Leggings";
            case "boots" -> "Diamond Boots";
            default -> null;
        };
    }

    @Override
    public void onReset() {
        LAST.clear();
    }
}
