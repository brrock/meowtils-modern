package wtf.tatp.meowtils.util.anticheat.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.antisnipe.AntiCheat;
import wtf.tatp.meowtils.util.Settings;

public final class LegitScaffoldCheck {
    private static final Map<UUID, Long> LAST_CROUCH_START = new HashMap<>();
    private static final Map<UUID, Long> LAST_CROUCH_END = new HashMap<>();
    private static final Map<UUID, Boolean> WAS_SNEAKING = new HashMap<>();
    private static final Map<UUID, Long> LAST_SWING_TICK = new HashMap<>();
    private static final Map<UUID, List<Integer>> CROUCH_DURATIONS = new HashMap<>();
    private static final Map<UUID, Long> LAST_FLAG_TICK = new HashMap<>();
    private static final Map<UUID, Boolean> FLAGGED = new HashMap<>();
    private static final long COOLDOWN_TICKS = 60;

    public void anticheatCheck(Player player) {
        Minecraft mc = Minecraft.getInstance();
        AntiCheat module = Module.get(AntiCheat.class);
        if (module == null || !Settings.bool(module, "legitScaffold", true) || player == null || player == mc.player) return;
        UUID uuid = player.getUUID();
        long tick = player.tickCount;
        trackCrouch(uuid, tick, player.isShiftKeyDown() || player.isCrouching());
        if (player.swingTime == 1) LAST_SWING_TICK.put(uuid, tick);
        if (isScaffold(player)) evaluate(uuid, tick);
    }

    private static void trackCrouch(UUID uuid, long tick, boolean currSneak) {
        boolean prev = WAS_SNEAKING.getOrDefault(uuid, false);
        if (currSneak && !prev) LAST_CROUCH_START.put(uuid, tick);
        else if (!currSneak && prev) {
            long start = LAST_CROUCH_START.getOrDefault(uuid, tick - 1);
            LAST_CROUCH_END.put(uuid, tick);
            List<Integer> durations = CROUCH_DURATIONS.computeIfAbsent(uuid, key -> new ArrayList<>());
            durations.add(0, (int) (tick - start));
            if (durations.size() > 5) durations.remove(5);
        }
        WAS_SNEAKING.put(uuid, currSneak);
    }

    private static boolean isScaffold(Player player) {
        return player.getXRot() >= 60 && player.onGround() && !player.getMainHandItem().isEmpty()
                && player.getMainHandItem().getItem() instanceof BlockItem;
    }

    private static void evaluate(UUID uuid, long tick) {
        Long start = LAST_CROUCH_START.get(uuid);
        Long end = LAST_CROUCH_END.get(uuid);
        if (start == null || end == null) { FLAGGED.put(uuid, false); return; }
        int crouchDuration = (int) (end - start);
        boolean quick = crouchDuration >= 1 && crouchDuration <= 2;
        long swing = LAST_SWING_TICK.getOrDefault(uuid, Long.MIN_VALUE);
        boolean swingTiming = swing >= end && swing <= end + 3 && tick - swing <= 10;
        List<Integer> durations = CROUCH_DURATIONS.getOrDefault(uuid, List.of());
        boolean consistent = durations.size() >= 3 && durations.get(0) <= 3 && durations.get(1) <= 3 && durations.get(2) <= 3;
        if (quick && swingTiming && consistent) {
            long lastFlag = LAST_FLAG_TICK.getOrDefault(uuid, 0L);
            if (tick - lastFlag >= COOLDOWN_TICKS) {
                FLAGGED.put(uuid, true);
                LAST_FLAG_TICK.put(uuid, tick);
                return;
            }
        }
        FLAGGED.put(uuid, false);
    }

    public boolean failedLegitScaffold(UUID uuid) { return FLAGGED.getOrDefault(uuid, false); }

    public void reset(UUID uuid) {
        FLAGGED.remove(uuid);
        LAST_CROUCH_START.remove(uuid);
        LAST_CROUCH_END.remove(uuid);
        WAS_SNEAKING.remove(uuid);
        LAST_SWING_TICK.remove(uuid);
        CROUCH_DURATIONS.remove(uuid);
        LAST_FLAG_TICK.remove(uuid);
    }
}
