package wtf.tatp.meowtils.util.anticheat.checks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.antisnipe.AntiCheat;
import wtf.tatp.meowtils.util.Settings;

public final class KillauraCheck {
    private static final int EAT_TIMEOUT = 33;
    private final Map<UUID, Integer> useItemTicks = new HashMap<>();
    private final Map<UUID, Integer> lastEatTicks = new HashMap<>();
    private final Map<UUID, Integer> violationLevels = new HashMap<>();
    private boolean failedKillauraB;

    public void anticheatCheck(Player player) {
        Minecraft mc = Minecraft.getInstance();
        AntiCheat module = Module.get(AntiCheat.class);
        if (module == null || !Settings.bool(module, "killaura", true) || player == mc.player || player.isPassenger()) return;
        UUID uuid = player.getUUID();
        int tick = player.tickCount;
        ItemStack held = player.getMainHandItem();
        boolean using = player.isUsingItem();
        boolean consumable = isConsumable(held);
        boolean attacking = player.swingTime > 0 || player.attackAnim > 0;
        int useTime = useItemTicks.getOrDefault(uuid, 0);
        if (using && consumable) {
            useTime++;
            useItemTicks.put(uuid, useTime);
        } else {
            if (useTime > 0) lastEatTicks.put(uuid, tick);
            useItemTicks.put(uuid, 0);
        }
        int sinceLastEat = tick - lastEatTicks.getOrDefault(uuid, 0);
        if (attacking && useTime > 6 && sinceLastEat < EAT_TIMEOUT && consumable) {
            int vl = violationLevels.getOrDefault(uuid, 0) + 1;
            violationLevels.put(uuid, vl);
            if (vl >= 8) failedKillauraB = true;
            return;
        }
        int vl = violationLevels.getOrDefault(uuid, 0);
        if (vl > 0) violationLevels.put(uuid, vl - 1);
    }

    public boolean failedKillauraB() { return failedKillauraB; }

    public void reset() {
        failedKillauraB = false;
        useItemTicks.clear();
        lastEatTicks.clear();
        violationLevels.clear();
    }

    private static boolean isConsumable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.has(DataComponents.FOOD) || stack.has(DataComponents.CONSUMABLE)
                || stack.getItem() == Items.POTION || stack.getItem() == Items.MILK_BUCKET
                || stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE;
    }
}
