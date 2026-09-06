package wtf.tatp.meowtils.module.bedwars;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;

/** Alerts when an enemy finishes a golden apple, milk, or potion. */
public final class ConsumeAlerts extends Module {
    private static final Map<UUID, Long> LAST_ALERT_TIME = new HashMap<>();
    private static final Map<UUID, TrackedUse> USING_ITEM = new HashMap<>();
    private static final long ALERT_COOLDOWN = 1600;

    public ConsumeAlerts() {
        super("ConsumeAlerts", Category.Bedwars);
        tag(ModuleTag.SAFE);
        tooltip("Alerts you when players consume a specific item.");
        addMode(new ModeValue("Alert", List.of("Chat", "Notification", "All"), "alertType", this));
        addSlider(new SliderValue("Max distance", 0, 250, 5, "m", "distance", this, Integer.class));
        addToggle(new ToggleValue("Ping sound", "sound", this));
        addToggle(new ToggleValue("Show distance", "showDistance", this));
        addCheck(new CheckValue("§6Golden Apple ", "goldenApple", this));
        addCheck(new CheckValue("Milk", "milk", this));
        addCheck(new CheckValue("§eSpeed Potion", "speed", this));
        addCheck(new CheckValue("§aJump Potion", "jump", this));
        addCheck(new CheckValue("§bInvis Potion", "invis", this));
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.level == null || mc.player == null) return;
        if (!BedwarsSupport.inMatch()) return;
        int maxDistance = Settings.integer(this, "distance", 0);
        for (Player player : mc.level.players()) {
            if (BedwarsSupport.skipPlayer(player, false) || skipFriend(player)) continue;
            float distance = player.distanceTo(mc.player);
            if (maxDistance > 0 && distance > maxDistance) continue;
            UUID uuid = player.getUUID();
            ItemStack held = player.getMainHandItem();
            boolean using = player.isUsingItem();
            if (using && !held.isEmpty() && player.getUseItem() != null && !player.getUseItem().isEmpty()) {
                held = player.getUseItem();
            }
            TrackedUse previous = USING_ITEM.get(uuid);
            if (using && !held.isEmpty() && isTrackedItem(held)) {
                if (previous == null || !ItemStack.isSameItemSameComponents(held, previous.item)) {
                    USING_ITEM.put(uuid, new TrackedUse(held.copy(), System.currentTimeMillis()));
                }
            } else if (previous != null) {
                USING_ITEM.remove(uuid);
                if (held.isEmpty() || !ItemStack.isSameItemSameComponents(held, previous.item)) {
                    alert(player, previous.item);
                }
            }
        }
    }

    private static boolean skipFriend(Player player) {
        return TeamUtil.ignoreFriends(player.getUUID().toString()) || TeamUtil.ignoreFriends(player.getGameProfile().name());
    }

    private static boolean isTrackedItem(ItemStack stack) {
        return ItemIds.is(stack, "golden_apple", "enchanted_golden_apple")
                || ItemIds.is(stack, "potion", "splash_potion", "lingering_potion")
                || ItemIds.is(stack, "milk_bucket", "milk");
    }

    private void alert(Player player, ItemStack item) {
        long now = System.currentTimeMillis();
        if (now - LAST_ALERT_TIME.getOrDefault(player.getUUID(), 0L) < ALERT_COOLDOWN) return;
        LAST_ALERT_TIME.put(player.getUUID(), now);
        String distance = Settings.bool(this, "showDistance", true)
                ? "§7 (§b" + (int) player.distanceTo(mc.player) + "m§7)" : "";
        if (ItemIds.is(item, "golden_apple", "enchanted_golden_apple") && Settings.bool(this, "goldenApple", true)) {
            send(player, "Golden Apple", "§6", distance);
            return;
        }
        if (ItemIds.is(item, "milk_bucket", "milk") && Settings.bool(this, "milk", true)) {
            send(player, "Milk", "§f", distance);
            return;
        }
        if (ItemIds.is(item, "potion", "splash_potion", "lingering_potion")) {
            String potion = BedwarsSupport.itemLabel(item);
            if (potion.contains("speed") && Settings.bool(this, "speed", true)) send(player, "Speed Potion", "§e", distance);
            else if (potion.contains("jump") && Settings.bool(this, "jump", true)) send(player, "Jump Potion", "§a", distance);
            else if (potion.contains("invis") && Settings.bool(this, "invis", true)) send(player, "Invis Potion", "§b", distance);
        }
    }

    private void send(Player player, String item, String color, String distance) {
        String name = BedwarsSupport.displayName(player);
        String text = name + "§7 consumed " + color + item + distance;
        BedwarsSupport.alert(this, text, "ConsumeAlerts", name + "§7 consumed " + color + item, Settings.text(this, "alertType", "Chat"));
        if (Settings.bool(this, "sound", true)) BedwarsSupport.play(BedwarsSupport.Sound.PING_MEDIUM);
    }

    private record TrackedUse(ItemStack item, long startUseDuration) {}

    @Override
    public void onReset() {
        USING_ITEM.clear();
        LAST_ALERT_TIME.clear();
    }
}
