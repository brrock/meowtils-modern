package wtf.tatp.meowtils.module.render;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.CameraType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.RenderWorldLastEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;

/** World-space health bars beside players (original Raven billboard). */
public final class HealthESP extends Module {
    private static final Map<UUID, Float> LAST_RATIO = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> MISSES = new ConcurrentHashMap<>();
    @Config public boolean showSelf;

    public HealthESP() {
        super("HealthESP", Category.Render);
        addToggle(new ToggleValue("Show self", "showSelf", this));
        tag(ModuleTag.SAFE);
        tooltip("Renders a health bar on players.");
    }

    @EventTarget
    public void onWorld(RenderWorldLastEvent event) {
        if (mc.player == null || mc.level == null) return;
        boolean self = Settings.bool(this, "showSelf", false);
        float partial = WorldOverlay.partialTick();
        HashSet<UUID> seen = new HashSet<>();
        for (Player player : mc.level.players()) {
            if (TeamUtil.isBot(player) || player.deathTime > 0) continue;
            if (!self && player == mc.player) continue;
            if (self && player == mc.player && mc.options.getCameraType() == CameraType.FIRST_PERSON) continue;
            UUID id = player.getUUID();
            seen.add(id);
            float last = LAST_RATIO.getOrDefault(id, 1.0f);
            float target = HealthEspTrack.displayRatio(player.getHealth(), player.getMaxHealth(),
                    player.getAbsorptionAmount(), last, player.deathTime > 0);
            float ratio = HealthEspTrack.smooth(last, target, 0.35f);
            LAST_RATIO.put(id, ratio);
            MISSES.remove(id);
            AABB box = WorldOverlay.interpolated(player, partial);
            WorldOverlay.healthBar(event, (box.minX + box.maxX) * 0.5, box.minY + 0.15, (box.minZ + box.maxZ) * 0.5, ratio);
        }
        LAST_RATIO.entrySet().removeIf(entry -> {
            int misses = seen.contains(entry.getKey()) ? 0 : MISSES.merge(entry.getKey(), 1, Integer::sum);
            if (!HealthEspTrack.isStale(misses)) return false;
            MISSES.remove(entry.getKey());
            return true;
        });
    }
}
