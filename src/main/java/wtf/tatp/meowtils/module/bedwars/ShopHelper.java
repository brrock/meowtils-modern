package wtf.tatp.meowtils.module.bedwars;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.SlotClickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.OpacityValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.Settings;

/** Highlights affordable shop items, middle-clicks purchases, and blocks duplicate swords/sticks. */
public final class ShopHelper extends Module {
    @wtf.tatp.meowtils.config.Config public float opacity = 50f;
    public static final Map<String, Integer> INVENTORY_RESOURCES = new HashMap<>();
    private static final Map<String, Integer> CURRENT_RESOURCES = new HashMap<>();
    private static final Map<String, ItemCategory> CATEGORY = new HashMap<>();
    private static final Map<String, Integer> PRIORITY = new HashMap<>();
    private static final EnumMap<ItemCategory, Integer> BEST_ITEMS = new EnumMap<>(ItemCategory.class);

    private enum ItemCategory { SWORD, ARMOR, PICKAXE, AXE, STICK, SHEARS }

    static {
        register(ItemCategory.SWORD, "diamond_sword", "iron_sword", "stone_sword");
        register(ItemCategory.ARMOR, "diamond_leggings", "iron_leggings", "chainmail_leggings", "chain_leggings");
        register(ItemCategory.PICKAXE, "diamond_pickaxe", "golden_pickaxe", "gold_pickaxe", "iron_pickaxe", "wooden_pickaxe", "wood_pickaxe");
        register(ItemCategory.AXE, "diamond_axe", "golden_axe", "gold_axe", "iron_axe", "stone_axe", "wooden_axe", "wood_axe");
        register(ItemCategory.STICK, "stick");
        register(ItemCategory.SHEARS, "shears");
    }

    public ShopHelper() {
        super("ShopHelper", Category.Bedwars);
        tag(ModuleTag.LEGIT);
        tooltip("Helps you shop faster by highlighting affordable items, replacing clicks with middle clicks and\npreventing purchasing certain duplicate items.");
        addOpacity(new OpacityValue("Opacity", "opacity", this));
        addToggle(new ToggleValue("Highlight affordable", "highlightAffordable", this));
        addToggle(new ToggleValue("Replace clicks", "replaceClicks", this));
        addToggle(new ToggleValue("Prevent duplicate", "preventDuplicate", this));
    }

    public static int getColor(String resource) {
        ShopHelper module = Module.get(ShopHelper.class);
        int alpha = BedwarsSupport.opacityAlpha(module == null ? 50 : Settings.number(module, "opacity", 50));
        return switch (resource) {
            case "iron" -> BedwarsSupport.rgba(255, 255, 255, alpha);
            case "gold" -> BedwarsSupport.rgba(255, 170, 0, alpha);
            case "diamond" -> BedwarsSupport.rgba(85, 255, 255, alpha);
            case "emerald" -> BedwarsSupport.rgba(0, 170, 0, alpha);
            default -> BedwarsSupport.rgba(170, 170, 170, alpha);
        };
    }

    public static int slotColor(ItemStack stack) {
        ShopHelper module = Module.get(ShopHelper.class);
        if (module == null || !module.getState() || !Settings.bool(module, "highlightAffordable", true) || !BedwarsSupport.inMatch()) return 0;
        if (!isShopOpen()) return 0;
        ItemCost cost = getCostFromLore(stack);
        if (cost == null || !shouldHighlight(stack, cost)) return 0;
        return getColor(cost.resourceType);
    }

    @EventTarget
    public void onSlotClick(SlotClickEvent event) {
        if (!BedwarsSupport.inMatch() || !(event.getScreen() instanceof AbstractContainerScreen<?>)) return;
        String title = event.getScreen().getTitle().getString();
        if (!isShopTitle(title)) return;
        if (!isUpgradeShop(title) && event.getSlot() != null && event.getSlot().hasItem()) {
            ItemStack stack = event.getSlot().getItem();
            ItemCost cost = getCostFromLore(stack);
            if (Settings.bool(this, "preventDuplicate", true) && !shouldHighlight(stack, cost) && isDuplicateRestricted(stack)) {
                event.setCancelled(true);
                BedwarsSupport.notifyMode("§cPrevented you from buying duplicate item!", "§cPrevented you from buying duplicate item!",
                        "ShopHelper", "Prevented duplicate!", NotificationManager.Type.INFO, 1500L);
                BedwarsSupport.play(BedwarsSupport.Sound.ERROR);
            }
        }
        if (Settings.bool(this, "replaceClicks", true) && event.getInput() == ContainerInput.PICKUP && event.getButton() == 0) {
            event.setReplaceClick();
        }
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        if (!Settings.bool(this, "highlightAffordable", true) || !BedwarsSupport.inMatch()) return;
        CURRENT_RESOURCES.clear();
        BEST_ITEMS.clear();
        if (!isShopOpen()) return;
        for (ItemStack stack : mc.player.getInventory().getNonEquipmentItems()) {
            if (stack.isEmpty()) continue;
            String resource = BedwarsSupport.resourceKey(stack);
            if (!resource.isEmpty()) CURRENT_RESOURCES.put(resource, CURRENT_RESOURCES.getOrDefault(resource, 0) + stack.getCount());
            String id = ItemIds.id(stack);
            ItemCategory category = categoryOf(id);
            if (category != null) {
                int priority = PRIORITY.getOrDefault(id, Integer.MAX_VALUE);
                BEST_ITEMS.merge(category, priority, Math::min);
            }
        }
        INVENTORY_RESOURCES.clear();
        INVENTORY_RESOURCES.putAll(CURRENT_RESOURCES);
    }

    public static ItemCost getCostFromLore(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        for (String line : BedwarsSupport.loreLines(stack)) {
            String clean = ColorUtil.unformattedText(line).trim();
            String lower = ColorUtil.plainLower(clean);
            if (!lower.contains("cost:") && !lower.contains("tier")) continue;
            int amount;
            String type;
            if (lower.contains("cost:")) {
                String after = clean.substring(lower.indexOf("cost:") + 5).trim();
                String[] split = after.split("\\s+");
                if (split.length < 2 || !isNumeric(split[0])) continue;
                amount = Integer.parseInt(split[0]);
                type = split[1].toLowerCase(Locale.ROOT);
            } else {
                int commaIndex = clean.lastIndexOf(',');
                if (commaIndex == -1 || commaIndex + 2 >= clean.length()) continue;
                String[] costSplit = clean.substring(commaIndex + 2).trim().split("\\s+");
                if (costSplit.length < 2 || !isNumeric(costSplit[0])) continue;
                amount = Integer.parseInt(costSplit[0]);
                type = costSplit[1].toLowerCase(Locale.ROOT);
            }
            if (type.contains("unlocked")) return null;
            String resource = type.startsWith("iron") ? "iron" : type.startsWith("gold") ? "gold"
                    : type.startsWith("diamond") ? "diamond" : type.startsWith("emerald") ? "emerald" : null;
            if (resource != null) return new ItemCost(resource, amount);
        }
        return null;
    }

    public static boolean shouldHighlight(ItemStack stack, ItemCost cost) {
        if (stack == null || stack.isEmpty() || cost == null) return false;
        boolean affordable = INVENTORY_RESOURCES.getOrDefault(cost.resourceType, 0) >= cost.amount;
        if (isUpgradeShop(BedwarsSupport.screenTitle())) return affordable;
        if ("diamond".equals(cost.resourceType)) return affordable;
        ItemCategory category = categoryOf(ItemIds.id(stack));
        if (category != null) {
            int priority = PRIORITY.getOrDefault(ItemIds.id(stack), Integer.MAX_VALUE);
            int best = BEST_ITEMS.getOrDefault(category, Integer.MAX_VALUE);
            return priority < best && affordable;
        }
        return affordable;
    }

    private static boolean isDuplicateRestricted(ItemStack stack) {
        return ItemIds.isSword(stack) && !ItemIds.is(stack, "wooden_sword", "wood_sword")
                || ItemIds.is(stack, "stick");
    }

    /** Named shop tabs, plus ViaVersion blank / {@code container.chest} titles when lore still looks like a shop. */
    public static boolean isShopTitle(String title) {
        if (BedwarsSupport.shopScreen(title) || BedwarsSupport.upgradeShop(title)) return true;
        return viaGenericChestTitle(title) && shopLooksLikeShop();
    }

    static boolean isShopOpen() {
        return isShopTitle(BedwarsSupport.screenTitle());
    }

    static boolean isUpgradeShop(String title) {
        return BedwarsSupport.upgradeShop(title);
    }

    private static boolean viaGenericChestTitle(String title) {
        String plain = ColorUtil.unformattedText(title).trim();
        String lower = ColorUtil.plainLower(plain);
        if (plain.isEmpty()) return true;
        if (lower.equals("container.chest") || lower.equals("chest")) return true;
        return lower.equals(ColorUtil.plainLower(I18n.get("container.chest")));
    }

    private static boolean shopLooksLikeShop() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return false;
        AbstractContainerMenu menu = client.player.containerMenu;
        int limit = menu instanceof ChestMenu chest
                ? chest.getContainer().getContainerSize()
                : Math.min(menu.slots.size(), 54);
        for (int i = 0; i < limit; i++) {
            Slot slot = menu.slots.get(i);
            if (slot == null || !slot.hasItem()) continue;
            if (getCostFromLore(slot.getItem()) != null) return true;
        }
        return false;
    }

    private static boolean isNumeric(String value) {
        if (value == null || value.isEmpty()) return false;
        for (int i = 0; i < value.length(); i++) if (!Character.isDigit(value.charAt(i))) return false;
        return true;
    }

    private static void register(ItemCategory category, String... items) {
        for (int i = 0; i < items.length; i++) {
            CATEGORY.put(items[i], category);
            PRIORITY.put(items[i], i);
        }
    }

    private static ItemCategory categoryOf(String id) {
        ItemCategory direct = CATEGORY.get(id);
        if (direct != null) return direct;
        for (var entry : CATEGORY.entrySet()) {
            if (id.contains(entry.getKey())) return entry.getValue();
        }
        return null;
    }

    public static final class ItemCost {
        public final String resourceType;
        public final int amount;
        public ItemCost(String resourceType, int amount) {
            this.resourceType = resourceType;
            this.amount = amount;
        }
    }
}
