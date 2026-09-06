package wtf.tatp.meowtils.module.advanced;

import java.util.List;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import wtf.tatp.meowtils.event.AttackEntityEvent;
import wtf.tatp.meowtils.event.RenderTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.Settings;

/** Swaps to another hotbar stack when the selected item is used up. */
public final class AutoSwap extends Module {
    public static final List<String> ALLOWED_BLOCKS = List.of("stone", "grass", "dirt", "planks", "wool", "wood", "glass", "leaves", "clay", "cloth", "log");
    private static final List<String> PROJECTILES = List.of("egg", "snowball");
    private static final List<String> PEARLS = List.of("pearl");
    private static final List<String> SWORDS = List.of("sword");
    private static final List<String> TOOLS = List.of("rod", "pickaxe", "axe", "shovel", "hoe", "flint_and_steel");
    private static final List<String> RESOURCES = List.of("iron_ingot", "gold_ingot", "emerald", "diamond");
    private static Item lastItem;
    private static int lastSlot = -1;

    public AutoSwap() {
        super("AutoSwap", Category.Advanced);
        tag(ModuleTag.BLATANT);
        tooltip("Automatically swap slots when running out of certain items.");
        addToggle(new ToggleValue("§7Blocks", "blocks", this));
        addToggle(new ToggleValue("§6Projectiles", "projectiles", this));
        addToggle(new ToggleValue("§2Resources", "resources", this));
        addToggle(new ToggleValue("§5Pearls", "pearls", this));
        addToggle(new ToggleValue("§3Swords", "swords", this));
        addCheck(new CheckValue("Swap on attack", "swordOnAttack", this));
        addToggle(new ToggleValue("§eTools", "tools", this));
    }

    @EventTarget
    public void onRenderTick(RenderTickEvent event) {
        if (event.getPhase() != RenderTickEvent.Phase.POST || mc.player == null || mc.level == null || mc.gui.screen() != null) return;
        int slot = mc.player.getInventory().getSelectedSlot();
        ItemStack held = mc.player.getInventory().getItem(slot);
        if (lastItem != null && slot == lastSlot && (held.isEmpty() || held.getCount() < 1)) {
            swapItem(lastItem);
        }
        lastItem = held.isEmpty() ? null : held.getItem();
        lastSlot = slot;
    }

    @EventTarget
    public void onAttackEntity(AttackEntityEvent event) {
        if (!Settings.bool(this, "swordOnAttack", true) || mc.player == null) return;
        ItemStack held = mc.player.getMainHandItem();
        if (!held.isEmpty() && ItemIds.isSword(held)) return;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && ItemIds.isSword(stack)) {
                mc.player.getInventory().setSelectedSlot(i);
            }
        }
    }

    private void swapItem(Item depleted) {
        if (depleted == null || mc.player == null) return;
        String lastId = ItemIds.id(new ItemStack(depleted));
        boolean isBlock = depleted instanceof BlockItem;
        int current = mc.player.getInventory().getSelectedSlot();
        List<String> category = null;
        if (!isBlock) {
            if (matches(lastId, PROJECTILES) && !lastId.contains("leggings") && Settings.bool(this, "projectiles", true)) {
                category = PROJECTILES;
            } else if (matches(lastId, PEARLS) && Settings.bool(this, "pearls", true)) {
                category = PEARLS;
            } else if (ItemIds.isSword(new ItemStack(depleted)) && Settings.bool(this, "swords", true)) {
                category = SWORDS;
            } else if (matches(lastId, TOOLS) && Settings.bool(this, "tools", true)) {
                category = TOOLS;
            } else if (matches(lastId, RESOURCES) && Settings.bool(this, "resources", true)) {
                category = RESOURCES;
            } else {
                return;
            }
        }
        for (int offset = 1; offset <= 9; offset++) {
            int i = (current + offset) % 9;
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty() || stack.getCount() < 1) continue;
            if (stack.getItem() == depleted) {
                mc.player.getInventory().setSelectedSlot(i);
                return;
            }
            if (isBlock && isValidBlock(stack)) {
                mc.player.getInventory().setSelectedSlot(i);
                return;
            }
            if (category == SWORDS && ItemIds.isSword(stack)) {
                mc.player.getInventory().setSelectedSlot(i);
                return;
            }
            if (category != null && category != SWORDS && matches(ItemIds.id(stack), category) && !ItemIds.id(stack).contains("leggings")) {
                mc.player.getInventory().setSelectedSlot(i);
                return;
            }
        }
    }

    public static boolean isValidBlock(ItemStack stack) {
        AutoSwap module = get(AutoSwap.class);
        if (module == null || !module.getState() || !Settings.bool(module, "blocks", true)) return false;
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) return false;
        return matches(ItemIds.id(stack), ALLOWED_BLOCKS);
    }

    private static boolean matches(String id, List<String> tokens) {
        for (String token : tokens) {
            if (id.contains(token)) return true;
        }
        return false;
    }
}
