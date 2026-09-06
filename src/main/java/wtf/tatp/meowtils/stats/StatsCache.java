package wtf.tatp.meowtils.stats;

import java.util.concurrent.ConcurrentHashMap;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.hypixel.Stats;
import wtf.tatp.meowtils.util.Settings;

/** TTL cache plus a 5-minute retry window for failed fetches. */
public final class StatsCache {
    private static final ConcurrentHashMap<String, StatsContainer> CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> FAILED = new ConcurrentHashMap<>();
    private static final int RETRY_DELAY = 300000;

    private StatsCache() {}

    public static StatsContainer getValid(String key) {
        StatsContainer stats = CACHE.get(key);
        if (stats == null) return null;
        long ttl = ((long) cacheMinutes()) * 60000L;
        if (stats.isExpired(ttl)) {
            CACHE.remove(key);
            return null;
        }
        return stats;
    }

    public static boolean canRetry(String name) {
        Long last = FAILED.get(name);
        if (last == null) return true;
        if (System.currentTimeMillis() - last > RETRY_DELAY) {
            FAILED.remove(name);
            return true;
        }
        return false;
    }

    public static void put(String key, StatsContainer stats) {
        CACHE.put(key, stats);
    }

    public static void markFailed(String key) {
        FAILED.put(key, System.currentTimeMillis());
    }

    public static void clearCache() {
        CACHE.clear();
        FAILED.clear();
    }

    private static int cacheMinutes() {
        Stats stats = Module.get(Stats.class);
        if (stats == null) return 30;
        return Settings.integer(stats, "cache", stats.cache);
    }
}
