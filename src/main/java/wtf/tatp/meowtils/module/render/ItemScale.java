package wtf.tatp.meowtils.module.render;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.Settings;

/** Scale for dropped item models. ItemEntityRendererMixin applies the pose scale. */
public final class ItemScale extends Module {
    @Config public float scale = 2.5f;
    @Config public boolean importantOnly;
    @Config public boolean resources = true;
    @Config public boolean gear = true;
    @Config public boolean heads = true;

    public ItemScale() {
        super("ItemScale", Category.Render);
        addSlider(new SliderValue("Scale", 0.5, 5.0, 0.1, "x", "scale", this, Float.TYPE));
        addToggle(new ToggleValue("Important only", "importantOnly", this));
        addCheck(new CheckValue("§7Include §bGear", "gear", this));
        addCheck(new CheckValue("§7Include §6Resources", "resources", this));
        addCheck(new CheckValue("§7Include §dHeads", "heads", this));
        tag(ModuleTag.LEGIT);
        tooltip("Scale dropped items.");
    }

    public static float scaleFor(ItemStack stack) {
        ItemScale module = get(ItemScale.class);
        if (module == null || !module.getState() || stack == null || stack.isEmpty()) return 1.0f;
        if (Settings.bool(module, "importantOnly", false) && !shouldScale(stack)) return 1.0f;
        return (float) Settings.number(module, "scale", 2.5);
    }

    public static boolean shouldScale(ItemStack stack) {
        ItemScale module = get(ItemScale.class);
        if (module == null || stack == null || stack.isEmpty()) return false;
        if (Settings.bool(module, "resources", true) && ItemIds.is(stack, "iron_ingot", "gold_ingot", "diamond", "emerald", "golden_apple", "potion")) return true;
        if (Settings.bool(module, "gear", true) && isGear(stack)) return true;
        return Settings.bool(module, "heads", true) && ItemIds.is(stack, "head", "skull");
    }

    private static boolean isGear(ItemStack stack) {
        return stack.is(holder -> holder.is(ItemTags.SWORDS) || holder.is(ItemTags.PICKAXES) || holder.is(ItemTags.AXES)
                || holder.is(ItemTags.SHOVELS) || holder.is(ItemTags.HOES) || holder.is(ItemTags.HEAD_ARMOR)
                || holder.is(ItemTags.CHEST_ARMOR) || holder.is(ItemTags.LEG_ARMOR) || holder.is(ItemTags.FOOT_ARMOR));
    }
}
