package wtf.tatp.meowtils.module.render;

import net.minecraft.world.item.ItemStack;
import wtf.tatp.meowtils.util.ItemIds;

/** Exact 2.0.1 ItemESP item lists (no hoe, no ore extras except emerald block). */
public final class ItemEspItems {
    private ItemEspItems() {}

    public static boolean iron(ItemStack stack) {
        return match(ItemIds.id(stack), "iron_ingot", "iron_sword", "iron_pickaxe", "iron_axe", "iron_shovel",
                "iron_helmet", "iron_chestplate", "iron_leggings", "iron_boots");
    }

    public static boolean gold(ItemStack stack) {
        return match(ItemIds.id(stack), "gold_ingot", "golden_sword", "golden_pickaxe", "golden_axe", "golden_shovel",
                "golden_helmet", "golden_chestplate", "golden_leggings", "golden_boots", "golden_apple");
    }

    public static boolean diamond(ItemStack stack) {
        return match(ItemIds.id(stack), "diamond", "diamond_sword", "diamond_pickaxe", "diamond_axe", "diamond_shovel",
                "diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots");
    }

    public static boolean emerald(ItemStack stack) {
        return match(ItemIds.id(stack), "emerald", "emerald_block");
    }

    static boolean match(String id, String... names) {
        if (id == null || id.isEmpty()) return false;
        for (String name : names) {
            if (id.equals(name)) return true;
            if (name.startsWith("gold_") && id.equals("golden_" + name.substring(5))) return true;
            if (name.startsWith("golden_") && id.equals("gold_" + name.substring(7))) return true;
        }
        return false;
    }
}
