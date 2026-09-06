package wtf.tatp.meowtils.util;

import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Match both 26.2 and ViaVersion/1.8.9 item/block identifiers.
 * ViaVersion {@code Protocol1_8To1_9.isSword} uses numeric IDs 267/268/272/276/283;
 * after rewrite the 26.2 client sees modern names ({@code golden_sword}, not {@code gold_sword}).
 */
public final class ItemIds {
    /** ViaVersion 1.8 sword IDs: iron 267, wood 268, stone 272, diamond 276, gold 283. */
    public static final int[] VIA_SWORDS_1_8 = {267, 268, 272, 276, 283};

    private ItemIds() {}

    public static String id(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase(Locale.ROOT);
    }

    public static String id(BlockState state) {
        if (state == null) return "";
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().toLowerCase(Locale.ROOT);
    }

    public static boolean is(ItemStack stack, String... names) {
        String path = id(stack);
        for (String name : names) {
            String needle = name.toLowerCase(Locale.ROOT);
            if (path.contains(needle)) return true;
            if (needle.startsWith("gold_") && path.contains("golden_" + needle.substring(5))) return true;
            if (needle.startsWith("golden_") && path.contains("gold_" + needle.substring(7))) return true;
            if (needle.equals("chain_leggings") && path.contains("chainmail_leggings")) return true;
            if (needle.equals("fireball") && path.contains("fire_charge")) return true;
        }
        return false;
    }

    public static boolean isBlock(BlockState state, String... names) {
        String path = id(state);
        for (String name : names) if (path.contains(name.toLowerCase(Locale.ROOT))) return true;
        return false;
    }

    /** ViaVersion 1.8 swords plus 26.2 {@link ItemTags#SWORDS}. */
    public static boolean isSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(holder -> holder.is(ItemTags.SWORDS))) return true;
        String path = id(stack);
        return path.endsWith("_sword") || path.equals("sword");
    }

    public static boolean isBow(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String path = id(stack);
        return path.equals("bow") || path.endsWith("_bow") && !path.contains("bowl") && !path.contains("crossbow");
    }
}
