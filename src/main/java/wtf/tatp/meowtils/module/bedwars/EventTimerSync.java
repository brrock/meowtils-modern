package wtf.tatp.meowtils.module.bedwars;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Map Hypixel sidebar countdowns onto the original 2.0.1 event schedule. */
public final class EventTimerSync {
    static final int DIAMOND_II = 360;
    static final int EMERALD_II = 720;
    static final int DIAMOND_III = 1080;
    static final int EMERALD_III = 1440;
    static final int BED_GONE = 1800;
    static final int SUDDEN_DEATH = 2400;
    static final int GAME_END = 3000;
    private static final Pattern CLOCK = Pattern.compile("(\\d{1,2}):(\\d{2})");
    private static final Object[][] EVENTS = {
            { "diamond iii", "diamond 3", DIAMOND_III },
            { "emerald iii", "emerald 3", EMERALD_III },
            { "diamond ii", "diamond 2", DIAMOND_II },
            { "emerald ii", "emerald 2", EMERALD_II },
            { "bed gone", "beds gone", BED_GONE },
            { "sudden death", "sudden death", SUDDEN_DEATH },
            { "game end", "game over", GAME_END },
    };

    private EventTimerSync() {}

    public static Integer secondsOnLine(String line) {
        if (line == null) return null;
        Matcher matcher = CLOCK.matcher(line);
        if (!matcher.find()) return null;
        int minutes = Integer.parseInt(matcher.group(1));
        int seconds = Integer.parseInt(matcher.group(2));
        if (seconds > 59) return null;
        return minutes * 60 + seconds;
    }

    /** Elapsed match seconds from sidebar text, or null if no event clock is visible. */
    public static Integer elapsedFromLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) return null;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            for (Object[] event : EVENTS) {
                if (!contains((String) event[0], line) && !contains((String) event[1], line)) continue;
                Integer remaining = secondsOnLine(line);
                if (remaining == null && i + 1 < lines.size()) remaining = secondsOnLine(lines.get(i + 1));
                if (remaining == null) continue;
                return Math.max(0, (Integer) event[2] - remaining);
            }
        }
        return null;
    }

    private static boolean contains(String needle, String line) {
        return needle != null && line != null && line.contains(needle);
    }
}
