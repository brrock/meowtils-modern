package wtf.tatp.meowtils.stats.util;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.Prefix;

/** Stops Stats auto-check from re-parsing its own chat and reprinting forever. */
public final class StatsChatGuard {
    private static final ConcurrentHashMap<String, Long> RECENT = new ConcurrentHashMap<>();
    private static final long RECENT_MS = 2500L;

    private StatsChatGuard() {}

    public static boolean isOwnLine(String text) {
        String plain = ColorUtil.unformattedText(text);
        if (plain.isEmpty()) return true;
        if (plain.contains("FKDR:") || (plain.contains("KDR:") && plain.contains("WLR:"))) return true;
        String prefix = ColorUtil.unformattedText(Prefix.getPrefix()).trim();
        return !prefix.isEmpty() && plain.startsWith(prefix);
    }

    public static boolean alreadyShown(String player, String kind) {
        if (player == null || player.isBlank()) return true;
        String key = kind + ":" + player.toLowerCase(Locale.ROOT);
        long now = System.currentTimeMillis();
        Long last = RECENT.get(key);
        if (last != null && now - last < RECENT_MS) return true;
        RECENT.put(key, now);
        return false;
    }

    public static void reset() {
        RECENT.clear();
    }
}
