package wtf.tatp.meowtils.module.skywars;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;
import wtf.tatp.meowtils.CommandManager;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.MeowtilsData;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.OpacityValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.Util;

public final class ItemHighlight extends Module {
    @Config public boolean enabled;
    @Config public int key;
    @Config public float opacity = 50;
    @Config public String blacklistColor = "§4Dark Red";
    @Config public String safelistColor = "§aGreen";
    @Config public String bestColor = "§dLight Purple";
    @Config public boolean showBlacklisted = true;
    @Config public boolean showSafelisted = true;
    @Config public boolean showBest = true;
    @Config public boolean swords = true;
    @Config public boolean armor = true;
    @Config public boolean healing = true;
    @Config public boolean bows = true;
    @Config public boolean tools = true;
    @Config public boolean preventDropSafelisted;
    @Config public boolean preventDropBest = true;

    private static ItemStack bestSword, bestBow, bestPickaxe, bestAxe, bestShovel, bestHoe;
    private static int bestSwordScore, bestSwordAny, bestBowScore, bestBowAny;
    private static int bestPickaxeSharp, bestPickaxeEff, bestPickaxeAny;
    private static int bestAxeSharp, bestAxeEff, bestAxeAny;
    private static int bestShovelSharp, bestShovelEff, bestShovelAny;
    private static int bestHoeSharp, bestHoeEff, bestHoeAny;
    private static final ItemStack[] bestArmor = new ItemStack[4];
    private static final int[] bestArmorScore = new int[4];
    private static final int[] bestArmorAny = new int[4];
    private static Path blacklistFile() { return MeowtilsData.itemHighlightBlacklist(); }
    private static Path safelistFile() { return MeowtilsData.itemHighlightSafelist(); }
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Set<String> safelist = new HashSet<>();
    private static Set<String> blacklist = new HashSet<>();
    private static final List<String> DEFAULT_BLACKLIST = Arrays.asList("chest", "double_plant", "feather", "fireworks", "glass_bottle", "gunpowder", "jukebox", "leather", "lever", "magma_cream", "noteblock", "prismarine_crystals", "prismarine_shard", "rabbit_foot", "rabbit_hide", "record_11", "record_13", "record_blocks", "record_cat", "record_chirp", "record_far", "record_mall", "record_mellohi", "record_stal", "record_strad", "record_wait", "record_ward", "red_flower", "redstone", "redstone_torch", "repeater", "rotten_flesh", "saddle", "sand", "sapling", "spider_eye", "stone_button", "stone_pressure_plate", "string", "torch", "tripwire_hook", "waterlily", "wheat", "wooden_button", "wooden_pressure_plate", "yellow_flower", "carrot_on_a_stick", "cactus", "wheat_seeds", "skull", "gravel", "oak_stairs", "stone_stairs", "brick_stairs", "stone_brick_stairs", "nether_brick_stairs", "sandstone_stairs", "spruce_stairs", "birch_stairs", "jungle_stairs", "quartz_stairs", "acacia_stairs", "dark_oak_stairs", "red_sandstone_stairs", "stone_slab", "wooden_slab", "stone_slab2", "wooden_pickaxe", "wooden_shovel", "wooden_hoe", "dandelion", "poppy", "lily_pad", "player_head", "music_disc_11", "music_disc_13", "music_disc_blocks", "music_disc_cat", "note_block");
    private static final List<String> DEFAULT_SAFELIST = Arrays.asList("arrow", "bow", "chainmail_boots", "chainmail_chestplate", "chainmail_helmet", "chainmail_leggings", "clock", "diamond", "diamond_axe", "diamond_pickaxe", "diamond_shovel", "diamond_hoe", "diamond_block", "diamond_boots", "diamond_chestplate", "diamond_helmet", "diamond_leggings", "diamond_sword", "ender_pearl", "fishing_rod", "golden_apple", "golden_axe", "golden_boots", "golden_chestplate", "golden_helmet", "golden_leggings", "golden_sword", "iron_axe", "iron_pickaxe", "iron_shovel", "iron_hoe", "iron_boots", "iron_chestplate", "iron_helmet", "iron_leggings", "iron_sword", "leather_boots", "leather_chestplate", "leather_helmet", "leather_leggings", "snowball", "stone_axe", "stone_sword", "wooden_axe", "wooden_sword", "egg", "lava_bucket", "water_bucket", "potion");

    public ItemHighlight() {
        super("ItemHighlight", Category.Skywars);
        tag(ModuleTag.LEGIT);
        tooltip("Highlights important items in your inventory.\n§d/itemsl §d<item> §f- safelist\n§d/itemusl §d<item> §f- unsafelist\n§d/itembl §d<item> §f- blacklist\n§d/itemubl §d<item> §f- unblacklist");
        addOpacity(new OpacityValue("Opacity", "opacity", this));
        addMode(new ModeValue("Blacklist", List.of("§4Dark Red", "§cRed", "§eYellow", "§1Dark Blue"), "blacklistColor", this));
        addMode(new ModeValue("Safelist", List.of("§aGreen", "§2Dark Green", "§9Blue", "§3Dark Aqua"), "safelistColor", this));
        addMode(new ModeValue("Best", List.of("§dLight Purple", "§5Dark Purple", "§6Gold", "§bAqua"), "bestColor", this));
        addToggle(new ToggleValue("Prevent drop for best", "preventDropBest", this));
        addToggle(new ToggleValue("Prevent drop for safelisted", "preventDropSafelisted", this));
        addToggle(new ToggleValue("Render §4Blacklisted §fItems", "showBlacklisted", this));
        addToggle(new ToggleValue("Render §aSafelisted §fItems", "showSafelisted", this));
        addToggle(new ToggleValue("Render §dBest §fItems", "showBest", this));
        addExpand(new ExpandValue("Items", e -> {
            e.addCheck(new CheckValue("§bSwords", "swords", this));
            e.addCheck(new CheckValue("§3Armor", "armor", this));
            e.addCheck(new CheckValue("§dHealing", "healing", this));
            e.addCheck(new CheckValue("§6Bows", "bows", this));
            e.addCheck(new CheckValue("§2Tools", "tools", this));
        }, this));
        init();
        command("itemsafelist", "itemsl", true, true);
        command("itemunsafelist", "itemusl", true, false);
        command("itemblacklist", "itembl", false, true);
        command("itemunblacklist", "itemubl", false, false);
    }

    private static void command(String name, String alias, boolean safe, boolean add) {
        var root = com.mojang.brigadier.builder.LiteralArgumentBuilder.<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>literal(name)
                .then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource, String>argument("item", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .executes(c -> { mutate(com.mojang.brigadier.arguments.StringArgumentType.getString(c, "item"), safe, add); return 1; }));
        CommandManager.register(root);
        CommandManager.register(com.mojang.brigadier.builder.LiteralArgumentBuilder.<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>literal(alias)
                .then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource, String>argument("item", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .executes(c -> { mutate(com.mojang.brigadier.arguments.StringArgumentType.getString(c, "item"), safe, add); return 1; })));
    }

    private static void mutate(String item, boolean safe, boolean add) {
        String id = item.toLowerCase(java.util.Locale.ROOT);
        if (safe) {
            if (add) {
                if (isSafelisted(id)) Meowtils.addMessage("§7This item is already §2safelisted§7!");
                else { addSafelistItem(id); Meowtils.addMessage("§aAdded §f§o" + id + "§a to the item §2safelist§a!"); }
            } else {
                if (!isSafelisted(id)) Meowtils.addMessage("§7This item is not safelisted.");
                else { removeSafelistItem(id); Meowtils.addMessage("§eRemoved §f§o" + id + "§e from the item safelist."); }
            }
        } else if (add) {
            if (isBlacklisted(id)) Meowtils.addMessage("§7This item is already §4blacklisted§7!");
            else { addBlacklistItem(id); Meowtils.addMessage("§cAdded §f§o" + id + "§c to the item §4blacklist§c!"); }
        } else {
            if (!isBlacklisted(id)) Meowtils.addMessage("§7This item is not blacklisted.");
            else { removeBlacklistItem(id); Meowtils.addMessage("§eRemoved §f§o" + id + "§e from the item blacklist."); }
        }
    }

    public static void init() { load(); }

    public static String getListName(ItemStack stack) { return ItemIds.id(stack); }

    public static int slotColor(Slot slot) {
        ItemHighlight module = get(ItemHighlight.class);
        if (module == null || !module.getState() || slot == null || !slot.hasItem()) return 0;
        if (Skywars.GAME.isNotActive() && Skywars.MINI.isNotActive()) return 0;
        ItemStack stack = slot.getItem();
        if (Settings.bool(module, "showBlacklisted", true) && isBlacklisted(getListName(stack))) return getColor(Settings.text(module, "blacklistColor", module.blacklistColor));
        if (Settings.bool(module, "showBest", true) && shouldHighlight(stack)) return getColor(Settings.text(module, "bestColor", module.bestColor));
        if (Settings.bool(module, "showSafelisted", true) && isSafelisted(getListName(stack))) return getColor(Settings.text(module, "safelistColor", module.safelistColor));
        return 0;
    }

    public static boolean shouldCancelThrow(Slot slot, ContainerInput input) {
        ItemHighlight module = get(ItemHighlight.class);
        if (module == null || !module.getState() || input != ContainerInput.THROW || slot == null || !slot.hasItem()) return false;
        if (Skywars.GAME.isNotActive() && Skywars.MINI.isNotActive()) return false;
        if (Settings.bool(module, "preventDropBest", true) && shouldHighlight(slot.getItem())) {
            Meowtils.addMessage("§cStopped you from dropping best item!");
            Util.playSound(Util.Sound.ERROR_DEEP, 100);
            return true;
        }
        if (Settings.bool(module, "preventDropSafelisted", false) && isSafelisted(getListName(slot.getItem()))) {
            Meowtils.addMessage("§cStopped you from dropping safelisted item!");
            Util.playSound(Util.Sound.ERROR_DEEP, 100);
            return true;
        }
        return false;
    }

    public static boolean shouldCancelSelectedDrop() {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null || mc.gui.screen() != null) return false;
        int selected = mc.player.getInventory().getSelectedSlot();
        for (Slot slot : mc.player.inventoryMenu.slots) {
            if (slot.container == mc.player.getInventory() && slot.getContainerSlot() == selected) {
                return shouldCancelThrow(slot, ContainerInput.THROW);
            }
        }
        return false;
    }

    public static boolean shouldHighlight(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !isSafelisted(getListName(stack))) return false;
        ItemHighlight module = get(ItemHighlight.class);
        if (module == null) return false;
        update();
        if (Settings.bool(module, "swords", true) && stack == bestSword) return true;
        if (Settings.bool(module, "bows", true) && stack == bestBow) return true;
        if (Settings.bool(module, "healing", true) && isHealingItem(stack)) return true;
        if (Settings.bool(module, "tools", true) && (stack == bestPickaxe || stack == bestAxe || stack == bestShovel || stack == bestHoe)) return true;
        if (Settings.bool(module, "armor", true)) {
            for (ItemStack armor : bestArmor) if (stack == armor) return true;
        }
        return false;
    }

    public static int getColor(String config) {
        ItemHighlight module = get(ItemHighlight.class);
        int alpha = ColorUtil.convertOpacity((float) Settings.number(module, "opacity", module == null ? 50 : module.opacity));
        int rgb = ColorUtil.rgbFromFormatting(ColorUtil.formattingFromName(config));
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    private static void update() {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null) return;
        clear();
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) compare(mc.player.getItemBySlot(slot));
        for (ItemStack stack : mc.player.getInventory().getNonEquipmentItems()) compare(stack);
        if (mc.player.containerMenu != mc.player.inventoryMenu) {
            for (Slot slot : mc.player.containerMenu.slots) if (slot != null) compare(slot.getItem());
        }
    }

    private static void compare(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        int any = stack.hasFoil() || !stack.getEnchantments().isEmpty() ? 1 : 0;
        if (ItemIds.isSword(stack)) {
            int dmg = materialScore(stack) + enchant(stack, "sharpness");
            if (bestSword == null || dmg > bestSwordScore || (dmg == bestSwordScore && any > bestSwordAny)) {
                bestSwordScore = dmg; bestSwordAny = any; bestSword = stack;
            }
            return;
        }
        if (ItemIds.isBow(stack)) {
            int power = enchant(stack, "power");
            if (bestBow == null || power > bestBowScore || (power == bestBowScore && any > bestBowAny)) {
                bestBowScore = power; bestBowAny = any; bestBow = stack;
            }
            return;
        }
        if (stack.is(ItemTags.PICKAXES)) { pick(stack, any); return; }
        if (stack.is(ItemTags.AXES)) {
            int sharp = enchant(stack, "sharpness"), eff = enchant(stack, "efficiency");
            if (bestAxe == null || sharp > bestAxeSharp || (sharp == bestAxeSharp && eff > bestAxeEff) || (sharp == bestAxeSharp && eff == bestAxeEff && any > bestAxeAny)) {
                bestAxeSharp = sharp; bestAxeEff = eff; bestAxeAny = any; bestAxe = stack;
            }
            return;
        }
        if (stack.is(ItemTags.SHOVELS)) {
            int sharp = enchant(stack, "sharpness"), eff = enchant(stack, "efficiency");
            if (bestShovel == null || sharp > bestShovelSharp || (sharp == bestShovelSharp && eff > bestShovelEff) || (sharp == bestShovelSharp && eff == bestShovelEff && any > bestShovelAny)) {
                bestShovelSharp = sharp; bestShovelEff = eff; bestShovelAny = any; bestShovel = stack;
            }
            return;
        }
        if (stack.is(ItemTags.HOES)) {
            int sharp = enchant(stack, "sharpness"), eff = enchant(stack, "efficiency");
            if (bestHoe == null || sharp > bestHoeSharp || (sharp == bestHoeSharp && eff > bestHoeEff) || (sharp == bestHoeSharp && eff == bestHoeEff && any > bestHoeAny)) {
                bestHoeSharp = sharp; bestHoeEff = eff; bestHoeAny = any; bestHoe = stack;
            }
            return;
        }
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot().isArmor()) {
            int slot = switch (equippable.slot()) { case HEAD -> 0; case CHEST -> 1; case LEGS -> 2; case FEET -> 3; default -> -1; };
            if (slot < 0) return;
            int score = materialScore(stack) + enchant(stack, "protection");
            if (score > bestArmorScore[slot] || (score == bestArmorScore[slot] && any > bestArmorAny[slot])) {
                bestArmorScore[slot] = score; bestArmorAny[slot] = any; bestArmor[slot] = stack;
            }
        }
    }

    private static void pick(ItemStack stack, int any) {
        int sharp = enchant(stack, "sharpness"), eff = enchant(stack, "efficiency");
        if (bestPickaxe == null || sharp > bestPickaxeSharp || (sharp == bestPickaxeSharp && eff > bestPickaxeEff) || (sharp == bestPickaxeSharp && eff == bestPickaxeEff && any > bestPickaxeAny)) {
            bestPickaxeSharp = sharp; bestPickaxeEff = eff; bestPickaxeAny = any; bestPickaxe = stack;
        }
    }

    private static int materialScore(ItemStack stack) {
        String id = ItemIds.id(stack);
        if (id.contains("netherite")) return 8;
        if (id.contains("diamond")) return 7;
        if (id.contains("iron")) return 6;
        if (id.contains("chainmail")) return 5;
        if (id.contains("stone") || id.contains("copper")) return 5;
        if (id.contains("gold") || id.contains("golden")) return 4;
        return 3;
    }

    private static int enchant(ItemStack stack, String path) {
        ItemEnchantments enchants = stack.getEnchantments();
        if (enchants == null || enchants.isEmpty()) return 0;
        int level = 0;
        for (var entry : enchants.entrySet()) {
            String name = entry.getKey().unwrapKey().map(key -> key.identifier().getPath()).orElseGet(entry.getKey()::getRegisteredName);
            if (name.toLowerCase(java.util.Locale.ROOT).contains(path)) level = Math.max(level, entry.getIntValue());
        }
        return level;
    }

    private static boolean isHealingItem(ItemStack stack) { return ItemIds.is(stack, "golden_apple"); }

    private static void clear() {
        bestHoe = bestShovel = bestAxe = bestPickaxe = bestBow = bestSword = null;
        bestSwordAny = bestSwordScore = bestBowAny = bestBowScore = 0;
        bestPickaxeAny = bestPickaxeEff = bestPickaxeSharp = 0;
        bestAxeAny = bestAxeEff = bestAxeSharp = 0;
        bestShovelAny = bestShovelEff = bestShovelSharp = 0;
        bestHoeAny = bestHoeEff = bestHoeSharp = 0;
        Arrays.fill(bestArmor, null);
        Arrays.fill(bestArmorAny, 0);
        Arrays.fill(bestArmorScore, 0);
    }

    public static void addSafelistItem(String id) { safelist.add(id.toLowerCase(java.util.Locale.ROOT)); save(); }
    public static void removeSafelistItem(String id) { safelist.remove(id.toLowerCase(java.util.Locale.ROOT)); save(); }
    public static void addBlacklistItem(String id) { blacklist.add(id.toLowerCase(java.util.Locale.ROOT)); save(); }
    public static void removeBlacklistItem(String id) { blacklist.remove(id.toLowerCase(java.util.Locale.ROOT)); save(); }
    public static boolean isSafelisted(String id) { return id != null && safelist.contains(id.toLowerCase(java.util.Locale.ROOT)); }
    public static boolean isBlacklisted(String id) { return id != null && blacklist.contains(id.toLowerCase(java.util.Locale.ROOT)); }

    private static void load() {
        Path safeSource = resolve(safelistFile(), MeowtilsData.legacyItemSafelist());
        Path blackSource = resolve(blacklistFile(), MeowtilsData.legacyItemBlacklist());
        boolean initSafe = fileEmpty(safeSource);
        boolean initBlack = fileEmpty(blackSource);
        if (initSafe) { safelist.clear(); safelist.addAll(DEFAULT_SAFELIST); } else read(safeSource, true);
        if (initBlack) { blacklist.clear(); blacklist.addAll(DEFAULT_BLACKLIST); } else read(blackSource, false);
        if (initSafe || initBlack || !safeSource.equals(safelistFile()) || !blackSource.equals(blacklistFile())) save();
    }

    private static Path resolve(Path preferred, Path fallback) {
        if (!fileEmpty(preferred)) return preferred;
        if (!fileEmpty(fallback)) return fallback;
        return preferred;
    }

    private static boolean fileEmpty(Path path) {
        try {
            if (!Files.isRegularFile(path)) return true;
            String text = Files.readString(path).trim();
            return text.isEmpty() || "{}".equals(text);
        } catch (Exception ignored) { return true; }
    }

    private static void read(Path path, boolean safe) {
        try {
            Set<String> data = GSON.fromJson(Files.readString(path), new TypeToken<Set<String>>(){}.getType());
            if (safe) safelist = data != null ? new HashSet<>(data) : new HashSet<>();
            else blacklist = data != null ? new HashSet<>(data) : new HashSet<>();
        } catch (Exception exception) {
            if (safe) safelist = new HashSet<>();
            else blacklist = new HashSet<>();
        }
    }

    private static void save() {
        try {
            Files.createDirectories(safelistFile().getParent());
            Files.writeString(safelistFile(), GSON.toJson(safelist));
            Files.writeString(blacklistFile(), GSON.toJson(blacklist));
        } catch (Exception ignored) {}
    }
}
