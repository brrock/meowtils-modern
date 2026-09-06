package wtf.tatp.meowtils.module.skywars;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.icons.IconManager;
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.NameUtil;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;
import wtf.tatp.meowtils.util.Util;

public final class SkywarsAlerts extends Module {
    @Config public boolean enabled;
    @Config public int key;
    @Config public int cooldown = 10;
    @Config public String soundMode = "None";
    @Config public boolean showDistance = true;
    @Config public boolean fireSword = true;
    @Config public boolean diamondSword = true;
    @Config public boolean knockbackSword = true;
    @Config public boolean knockbackRod = true;
    @Config public boolean strengthPotion = true;
    @Config public boolean enderPearl = true;
    @Config public boolean corruptPearl = true;
    @Config public boolean warpPearl = true;
    @Config public boolean nametagIcon = true;
    @Config public boolean swordIcon = true;
    @Config public boolean knockbackIcon = true;
    @Config public boolean pearlIcon = true;
    @Config public boolean strengthIcon = true;
    @Config public String alertType = "Chat";
    private static final Map<String, Map<String, Long>> COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Set<String>> HELD_ITEM_CACHE = new HashMap<>();

    public SkywarsAlerts() {
        super("SkywarsAlerts", Category.Skywars);
        tag(ModuleTag.SAFE);
        tooltip("Alerts you of items players have in Skywars.");
        addMode(new ModeValue("Alert", List.of("Chat", "Notification", "All"), "alertType", this));
        addMode(new ModeValue("Ping sound", List.of("All", "Important", "None"), "soundMode", this));
        addSlider(new SliderValue("Cooldown", 1, 30, 1, "s", "cooldown", this, Integer.TYPE));
        addToggle(new ToggleValue("Show distance", "showDistance", this));
        addExpand(new ExpandValue("Items", e -> {
            e.addCheck(new CheckValue("§cFire §fSword", "fireSword", this));
            e.addCheck(new CheckValue("§bDiamond §fSword", "diamondSword", this));
            e.addCheck(new CheckValue("§eKnockback §fSword", "knockbackSword", this));
            e.addCheck(new CheckValue("§6Knockback §fRod", "knockbackRod", this));
            e.addCheck(new CheckValue("§4Strength §fPotion", "strengthPotion", this));
            e.addCheck(new CheckValue("§5Ender §fPearl", "enderPearl", this));
            e.addCheck(new CheckValue("§3Corrupt §fPearl", "corruptPearl", this));
            e.addCheck(new CheckValue("§dTime Warp §fPearl", "warpPearl", this));
        }, this));
        addExpand(new ExpandValue("Nametag Icon", e -> {
            e.addToggle(new ToggleValue("Show icon", "nametagIcon", this));
            e.addCheck(new CheckValue("§cSwords", "swordIcon", this));
            e.addCheck(new CheckValue("§6Knockback §fItems", "knockbackIcon", this));
            e.addCheck(new CheckValue("§5Ender §fPearl", "pearlIcon", this));
            e.addCheck(new CheckValue("§4Strength §fPotion", "strengthIcon", this));
        }, this));
        IconManager.register(new wtf.tatp.meowtils.manager.icons.impl.SkywarsIcon());
    }

    public static boolean heldItem(UUID uuid, String item) {
        Set<String> set = HELD_ITEM_CACHE.get(uuid);
        return set != null && set.contains(item);
    }

    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null || event.getPhase() != ClientTickEvent.Phase.POST || Skywars.GAME.isNotActive()) return;
        for (Player player : mc.level.players()) {
            if (player == mc.player || TeamUtil.ignoreFriends(player.getUUID().toString()) || TeamUtil.ignoreFriends(player.getScoreboardName())) continue;
            ItemStack held = player.getMainHandItem();
            if (held.isEmpty()) continue;
            UUID uuid = player.getUUID();
            String itemName = null;
            if (ItemIds.is(held, "ender_pearl") && enchanted(held) && loreContains(held, "Teleport back") && Settings.bool(this, "warpPearl", warpPearl)) {
                itemName = "§dTime Warp Pearl";
            } else if (ItemIds.is(held, "ender_pearl") && enchanted(held) && Settings.bool(this, "corruptPearl", corruptPearl)) {
                itemName = "§3Corrupt Pearl";
            } else if (ItemIds.is(held, "ender_pearl") && Settings.bool(this, "enderPearl", enderPearl)) {
                itemName = "§5Ender Pearl";
                markHeld(uuid, "ender_pearl");
            } else if (ItemIds.isSword(held) && ItemIds.is(held, "diamond_sword") && Settings.bool(this, "diamondSword", diamondSword)) {
                itemName = "§bDiamond Sword";
                markHeld(uuid, "diamond_sword");
            } else if (ItemIds.isSword(held) && ItemIds.is(held, "iron_sword") && enchanted(held) && Settings.bool(this, "fireSword", fireSword)) {
                itemName = "§cFire Sword";
                markHeld(uuid, "iron_sword");
            } else if (ItemIds.is(held, "fishing_rod") && enchanted(held) && held.getEnchantments().size() == 1 && Settings.bool(this, "knockbackRod", knockbackRod)) {
                itemName = "§6Knockback Rod";
                markHeld(uuid, "fishing_rod");
            } else if (ItemIds.isSword(held) && ItemIds.is(held, "golden_sword") && enchanted(held) && Settings.bool(this, "knockbackSword", knockbackSword)) {
                itemName = "§eKnockback Sword";
                markHeld(uuid, "golden_sword");
            } else if (ItemIds.is(held, "potion") && (loreContains(held, "Strength") || potionIsStrength(held)) && Settings.bool(this, "strengthPotion", strengthPotion)) {
                itemName = "§4Strength Potion";
                markHeld(uuid, "potion");
            }
            if (itemName != null && !hasCooldown(player.getScoreboardName(), itemName)) {
                alert(player, itemName);
                setCooldown(player.getScoreboardName(), itemName);
            }
        }
    }

    private void alert(Player player, String itemName) {
        int distance = (int) player.distanceTo(mc.player);
        String raw = ColorUtil.unformattedText(itemName).toLowerCase(java.util.Locale.ROOT);
        String distanceText = Settings.bool(this, "showDistance", showDistance) ? "§7 (§b" + distance + "m§7)" : "";
        String text = NameUtil.getTabDisplayName(player.getScoreboardName()) + "§7 has " + itemName;
        String type = Settings.text(this, "alertType", alertType);
        if (!type.equals("Notification")) Meowtils.addMessage(text + distanceText);
        if (!type.equals("Chat")) NotificationManager.show("SkywarsAlerts", text, NotificationManager.Type.ALERT, 1500L);
        String mode = Settings.text(this, "soundMode", soundMode);
        if (mode.equals("All") || (mode.equals("Important") && (raw.equals("ender pearl") || raw.equals("diamond sword") || raw.equals("knockback rod") || raw.contains("strength")))) {
            Util.playSound(Util.Sound.PING, 100);
        }
    }

    private boolean hasCooldown(String playerName, String itemName) {
        long window = Settings.integer(this, "cooldown", cooldown) * 1000L;
        Map<String, Long> playerCooldowns = COOLDOWNS.get(playerName);
        Long last = playerCooldowns == null ? null : playerCooldowns.get(itemName);
        return last != null && System.currentTimeMillis() - last < window;
    }

    private void setCooldown(String playerName, String itemName) {
        COOLDOWNS.computeIfAbsent(playerName, key -> new HashMap<>()).put(itemName, System.currentTimeMillis());
    }

    private static void markHeld(UUID uuid, String item) {
        HELD_ITEM_CACHE.computeIfAbsent(uuid, key -> new HashSet<>()).add(item);
    }

    private static boolean enchanted(ItemStack stack) { return stack.hasFoil() || !stack.getEnchantments().isEmpty(); }

    private static boolean loreContains(ItemStack stack, String needle) {
        if (stack.getHoverName().getString().contains(needle) || ColorUtil.unformattedText(stack.getHoverName().getString()).contains(needle)) return true;
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore != null) {
            for (var line : lore.lines()) {
                if (line.getString().contains(needle) || ColorUtil.unformattedText(line.getString()).contains(needle)) return true;
            }
        }
        for (String line : wtf.tatp.meowtils.module.bedwars.BedwarsSupport.loreLines(stack)) {
            if (line.contains(needle) || ColorUtil.unformattedText(line).contains(needle)) return true;
        }
        return false;
    }

    private static boolean potionIsStrength(ItemStack stack) {
        var contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return false;
        for (var effect : contents.getAllEffects()) {
            if (effect.getDescriptionId().toLowerCase(java.util.Locale.ROOT).contains("strength")) return true;
        }
        return false;
    }

    @Override
    public void onReset() {
        COOLDOWNS.clear();
        HELD_ITEM_CACHE.clear();
    }
}
