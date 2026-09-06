package wtf.tatp.meowtils.module.render;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.util.ItemIds;

/** Potion color behind inventory slots. Applied from AbstractContainerScreenMixin. */
public final class ShinyPots extends Module {
    public ShinyPots() {
        super("ShinyPots", Category.Render);
        tag(ModuleTag.LEGIT);
        tooltip("Renders potion color as slot background.");
    }

    public static int slotColor(Slot slot) {
        ShinyPots module = get(ShinyPots.class);
        if (module == null || !module.getState() || slot == null || !slot.hasItem()) return 0;
        ItemStack stack = slot.getItem();
        if (!ItemIds.is(stack, "potion")) return 0;
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        int color = contents == null ? PotionContents.BASE_POTION_COLOR : contents.getColor();
        return color | 0xCC000000;
    }
}
