package wtf.tatp.meowtils.stats;

import java.util.concurrent.CompletableFuture;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.hypixel.Stats;
import wtf.tatp.meowtils.util.Settings;

public interface StatsSource {
    String getId();

    CompletableFuture<StatsContainer> fetch(String name);

    default long getCooldown() {
        Stats stats = Module.get(Stats.class);
        if (stats == null) return 100L;
        return Settings.integer(stats, "cooldown", stats.cooldown);
    }
}
