package wtf.tatp.meowtils.module.bedwars;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;

/** Chat/notification alerts when an enemy holds a tracked Bedwars item. */
public final class ItemAlerts extends Module {
    private static final Map<String, Map<String, Long>> COOLDOWNS = new HashMap<>();

    public ItemAlerts() {
        super("ItemAlerts", Category.Bedwars);
        tag(ModuleTag.SAFE);
        tooltip("Alerts in chat when a player holds a specific item.");
        addMode(new ModeValue("Alert", List.of("Chat", "Notification", "All"), "alertType", this));
        addMode(new ModeValue("Ping sound", List.of("All", "Important", "None"), "sound", this));
        addMode(new ModeValue("Distance", List.of("All", "Important", "None"), "distanceMode", this));
        addSlider(new SliderValue("Cooldown", 1, 30, 1, "s", "cooldown", this, Integer.class));
        addExpand(new ExpandValue("Items", expand -> {
            expand.addCheck(new CheckValue("Iron Sword", "ironSword", this));
            expand.addCheck(new CheckValue("§bDiamond Sword", "diamondSword", this));
            expand.addCheck(new CheckValue("§6Bows", "bows", this));
            expand.addCheck(new CheckValue("§eKnockback Stick", "knockbackStick", this));
            expand.addCheck(new CheckValue("§6Golden Apple", "goldenApple", this));
            expand.addCheck(new CheckValue("§9Water bucket", "waterBucket", this));
            expand.addCheck(new CheckValue("§6Golden Pickaxe", "goldenPickaxe", this));
            expand.addCheck(new CheckValue("§bDiamond Pickaxe", "diamondPickaxe", this));
            expand.addCheck(new CheckValue("§cFireball", "fireball", this));
            expand.addCheck(new CheckValue("§4T§fN§4T", "tnt", this));
            expand.addCheck(new CheckValue("§3Pop-Up Tower", "tower", this));
            expand.addCheck(new CheckValue("Milk", "milk", this));
            expand.addCheck(new CheckValue("§aJump Potion", "jump", this));
            expand.addCheck(new CheckValue("§eSpeed Potion", "speed", this));
            expand.addCheck(new CheckValue("§bInvis Potion", "invis", this));
            expand.addCheck(new CheckValue("§5Ender Pearl", "pearl", this));
            expand.addCheck(new CheckValue("§8Obsidian", "obsidian", this));
            expand.addCheck(new CheckValue("§3Bridge Egg", "egg", this));
            expand.addCheck(new CheckValue("Iron Golem", "golem", this));
            expand.addCheck(new CheckValue("§7BedBug", "bedbug", this));
            expand.addCheck(new CheckValue("§dRotation Items", "rotation", this));
        }, this));
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        if (!BedwarsSupport.inMatch()) return;
        for (Player player : mc.level.players()) {
            if (BedwarsSupport.skipPlayer(player, false) || skipFriend(player)) continue;
            ItemStack held = player.getMainHandItem();
            if (held.isEmpty()) continue;
            String itemName = identify(held);
            if (itemName != null && !hasCooldown(player.getGameProfile().name(), itemName)) {
                alert(player, itemName);
                setCooldown(player.getGameProfile().name(), itemName);
            }
        }
    }

    private String identify(ItemStack held) {
        String name = null;
        if (ItemIds.isSword(held) && ItemIds.is(held, "iron_sword") && Settings.bool(this, "ironSword", true)) name = "§fIron Sword";
        if (ItemIds.isSword(held) && ItemIds.is(held, "diamond_sword") && Settings.bool(this, "diamondSword", true)) name = "§bDiamond Sword";
        if (ItemIds.is(held, "golden_apple", "enchanted_golden_apple") && Settings.bool(this, "goldenApple", true)) name = "§6Golden Apple";
        if (ItemIds.isBow(held) && Settings.bool(this, "bows", true)) {
            name = held.hasFoil() || held.isEnchanted() ? "§6Enchanted Bow" : "§6Bow";
        }
        if (ItemIds.is(held, "stick") && Settings.bool(this, "knockbackStick", true)) name = "§6Knockback Stick";
        if (ItemIds.is(held, "water_bucket") && Settings.bool(this, "waterBucket", true)) name = "§9Water Bucket";
        if (ItemIds.is(held, "diamond_pickaxe") && Settings.bool(this, "diamondPickaxe", true)) name = "§bDiamond Pickaxe";
        if (ItemIds.is(held, "golden_pickaxe", "gold_pickaxe") && Settings.bool(this, "goldenPickaxe", true)) name = "§6Golden Pickaxe";
        if (ItemIds.is(held, "fire_charge", "fireball") && Settings.bool(this, "fireball", true)) name = "§cFireball";
        if (ItemIds.is(held, "tnt") && !held.hasFoil() && !held.isEnchanted() && Settings.bool(this, "tnt", true)) name = "§cT§fN§cT";
        if (ItemIds.is(held, "milk_bucket", "milk") && Settings.bool(this, "milk", true)) name = "§fMilk";
        String label = BedwarsSupport.itemLabel(held);
        if (label.contains("jump") && Settings.bool(this, "jump", true)) name = "§aJump Potion";
        if (label.contains("speed") && Settings.bool(this, "speed", true)) name = "§eSpeed Potion";
        if (label.contains("invis") && Settings.bool(this, "invis", true)) name = "§bInvis Potion";
        if (ItemIds.is(held, "ender_pearl") && !held.hasFoil() && !held.isEnchanted() && Settings.bool(this, "pearl", true)) name = "§5Ender Pearl";
        if (ItemIds.is(held, "egg") && Settings.bool(this, "egg", true)) name = "§3Bridge Egg";
        if (ItemIds.is(held, "obsidian") && !ItemIds.is(held, "crying") && Settings.bool(this, "obsidian", true)) name = "§8Obsidian";
        if (ItemIds.is(held, "snowball") && !held.hasFoil() && !held.isEnchanted() && Settings.bool(this, "bedbug", true)) name = "§fBedbug";
        if ((ItemIds.is(held, "iron_golem_spawn_egg") || (ItemIds.is(held, "spawn_egg") && !label.contains("villager"))) && Settings.bool(this, "golem", true)) {
            name = "§fIron Golem";
        }
        if (ItemIds.is(held, "chest") && !ItemIds.is(held, "ender_chest", "trapped_chest") && Settings.bool(this, "tower", true)) {
            name = "§3Pop-Up Tower";
        }
        if (Settings.bool(this, "rotation", true)) {
            if (ItemIds.is(held, "nether_star")) name = "§e§nShuriken";
            else if (ItemIds.is(held, "ice") && !ItemIds.is(held, "ice_bridge") && (held.hasFoil() || held.isEnchanted())) name = "§3§nIce Bridge";
            else if (ItemIds.is(held, "flint_and_steel")) name = "§1§nBridge Zapper";
            else if (ItemIds.is(held, "name_tag")) name = "§6§nCharlie the Unicorn";
            else if (ItemIds.is(held, "ender_pearl") && (held.hasFoil() || held.isEnchanted())) name = "§d§nTime Warp Pearl";
            else if (ItemIds.is(held, "cookie")) name = "§b§nSugar Cookie";
            else if (ItemIds.is(held, "cobweb", "web")) name = "§f§nCobweb";
            else if (ItemIds.is(held, "pumpkin") && !ItemIds.is(held, "pumpkin_pie", "pumpkin_seeds")) name = "§2§nTeleportation Device";
            else if (ItemIds.is(held, "tnt") && (held.hasFoil() || held.isEnchanted())) name = "§4§nMega §cT§fN§cT";
            else if (ItemIds.is(held, "wooden_hoe", "wood_hoe", "mace")) name = "§6§nMace";
            else if (ItemIds.is(held, "snowball") && (held.hasFoil() || held.isEnchanted())) name = "§f§nWind Charge";
            else if (ItemIds.is(held, "shears") && (held.hasFoil() || held.isEnchanted())) name = "§a§nEnchanted Shears";
            else if (ItemIds.is(held, "beacon") && !held.hasFoil() && !held.isEnchanted()) name = "§c§nFinal Revive Beacon";
            else if (ItemIds.is(held, "prismarine_shard")) name = "§b§nBlock Zapper";
        }
        return name;
    }

    private static boolean skipFriend(Player player) {
        return TeamUtil.ignoreFriends(player.getUUID().toString()) || TeamUtil.ignoreFriends(player.getGameProfile().name());
    }

    private void alert(Player player, String itemName) {
        int distanceToEntity = (int) player.distanceTo(mc.player);
        String distance = "§7 (§b" + distanceToEntity + "m§7)";
        String rawItemName = ColorUtil.unformattedText(itemName).toLowerCase();
        String mode = Settings.text(this, "distanceMode", "Important");
        String showDistance = "All".equals(mode) || importantDistance(rawItemName) ? distance : "";
        String name = BedwarsSupport.displayName(player);
        BedwarsSupport.alert(this, name + "§7 has " + itemName + showDistance, name, itemName, Settings.text(this, "alertType", "Chat"));
        String sound = Settings.text(this, "sound", "None");
        if ("All".equals(sound) || ("Important".equals(sound) && importantSound(rawItemName))) {
            BedwarsSupport.play(BedwarsSupport.Sound.PING);
        }
    }

    private static boolean importantDistance(String raw) {
        return raw.equals("fireball") || raw.equals("ender pearl") || raw.equals("bridge egg")
                || raw.equals("speed potion") || raw.equals("jump potion") || raw.equals("invis potion")
                || raw.equals("charlie the unicorn");
    }

    private static boolean importantSound(String raw) {
        return raw.equals("jump potion") || raw.equals("speed potion") || raw.equals("invis potion")
                || raw.equals("bridge egg") || raw.equals("diamond sword") || raw.equals("iron golem")
                || raw.equals("bedbug") || raw.equals("charlie the unicorn") || raw.equals("diamond pickaxe")
                || raw.equals("enchanted bow") || raw.equals("milk") || raw.equals("ender pearl");
    }

    private boolean hasCooldown(String playerName, String itemName) {
        Map<String, Long> playerCooldowns = COOLDOWNS.get(playerName);
        if (playerCooldowns == null) return false;
        Long last = playerCooldowns.get(itemName);
        return last != null && System.currentTimeMillis() - last < (long) Settings.integer(this, "cooldown", 10) * 1000;
    }

    private void setCooldown(String playerName, String itemName) {
        COOLDOWNS.computeIfAbsent(playerName, ignored -> new HashMap<>()).put(itemName, System.currentTimeMillis());
    }

    @Override
    public void onReset() {
        COOLDOWNS.clear();
    }
}
