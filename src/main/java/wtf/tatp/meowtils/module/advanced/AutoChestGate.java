package wtf.tatp.meowtils.module.advanced;

import java.util.Locale;
import wtf.tatp.meowtils.util.ColorUtil;

/**
 * Title and chest-kind gates for AutoChest.
 * ViaVersion/Hypixel 1.8 windows often arrive as a generic chest with a blank title,
 * {@code container.chest}/{@code container.enderchest}, or the I18n name — not
 * {@code PlayerEnderChestContainer}.
 */
public final class AutoChestGate {
    public enum Kind { ENDER, NORMAL, BLANK, OTHER }

    private AutoChestGate() {}

    public static String normalize(String title) {
        return ColorUtil.plainLower(title).trim();
    }

    public static boolean isEnderTitle(String title, String localizedEnder) {
        String text = normalize(title);
        if (text.isEmpty()) return false;
        if (text.equals("container.enderchest") || text.equals("container.ender_chest")) return true;
        if (text.contains("ender") && text.contains("chest")) return true;
        return !localizedEnder.isEmpty() && text.equals(normalize(localizedEnder));
    }

    public static boolean isChestTitle(String title, String localizedChest) {
        String text = normalize(title);
        if (text.isEmpty() || isEnderTitle(title, "")) return false;
        if (text.equals("container.chest") || text.equals("chest")) return true;
        return !localizedChest.isEmpty() && text.equals(normalize(localizedChest));
    }

    public static Kind classify(boolean typedEnder, String title, boolean hypixel, String localizedChest, String localizedEnder) {
        if (typedEnder || isEnderTitle(title, localizedEnder)) return Kind.ENDER;
        if (isChestTitle(title, localizedChest)) return Kind.NORMAL;
        if (hypixel && normalize(title).isEmpty()) return Kind.BLANK;
        return Kind.OTHER;
    }

    public static boolean allow(boolean enderChests, boolean normalChests, Kind kind, boolean shop) {
        if (shop) return false;
        return switch (kind) {
            case ENDER -> enderChests;
            case NORMAL -> normalChests;
            case BLANK -> enderChests || normalChests;
            case OTHER -> false;
        };
    }

    public static boolean allowedResource(String kind, boolean iron, boolean gold, boolean diamonds, boolean emeralds) {
        return switch (kind) {
            case "iron" -> iron;
            case "gold" -> gold;
            case "diamond" -> diamonds;
            case "emerald" -> emeralds;
            default -> false;
        };
    }

    /** String form of {@link wtf.tatp.meowtils.module.bedwars.BedwarsSupport#resourceKey} for unit tests. */
    public static String resourceKindFromId(String itemId) {
        String id = itemId.toLowerCase(Locale.ROOT);
        if (id.contains("iron_ingot")) return "iron";
        if (id.contains("gold_ingot") || id.contains("golden_ingot")) return "gold";
        if (id.contains("diamond")
                && !id.contains("diamond_block")
                && !id.contains("diamond_sword")
                && !id.contains("diamond_pickaxe")
                && !id.contains("diamond_axe")
                && !id.contains("diamond_hoe")
                && !id.contains("diamond_shovel")
                && !id.contains("diamond_helmet")
                && !id.contains("diamond_chestplate")
                && !id.contains("diamond_leggings")
                && !id.contains("diamond_boots")) return "diamond";
        if (id.contains("emerald") && !id.contains("emerald_block") && !id.contains("emerald_ore")) return "emerald";
        return "";
    }

    public static boolean allowedResourceId(String itemId, boolean iron, boolean gold, boolean diamonds, boolean emeralds) {
        return allowedResource(resourceKindFromId(itemId), iron, gold, diamonds, emeralds);
    }
}
