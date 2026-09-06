package wtf.tatp.meowtils.module.bedwars;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wtf.tatp.meowtils.event.PlayerInteractEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.util.ItemIds;

/** Blocks obsidian placements that are not touching a bed. */
public final class AntiMisplace extends Module {
    public AntiMisplace() {
        super("AntiMisplace", Category.Bedwars);
        tag(ModuleTag.LEGIT);
        tooltip("Prevents placing obsidian if it isn't around a bed.");
    }

    @EventTarget
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || !BedwarsSupport.inMatch()) return;
        if (mc.player == null || mc.level == null || mc.gui.screen() != null) return;
        ItemStack held = mc.player.getMainHandItem();
        if (held.isEmpty()) held = mc.player.getOffhandItem();
        if (!ItemIds.is(held, "obsidian") || ItemIds.is(held, "crying")) return;
        BlockPos place = event.getPos().relative(event.getFacing());
        if (isAdjacentToBed(mc.level, place)) return;
        event.setCancelled(true);
        BedwarsSupport.notifyMode("§cPrevented you from placing obsidian!", "§cPrevented you from placing obsidian!",
                "AntiMisplace", "Can't place here!", NotificationManager.Type.ALERT, 1500L);
    }

    private static boolean isAdjacentToBed(Level world, BlockPos pos) {
        for (Direction facing : Direction.values()) {
            if (BedwarsSupport.isBed(world.getBlockState(pos.relative(facing)))) return true;
        }
        return false;
    }
}
