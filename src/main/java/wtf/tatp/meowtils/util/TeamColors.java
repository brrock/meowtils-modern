package wtf.tatp.meowtils.util;

import java.util.HashMap;
import java.util.Map;

/** 2.0.1 team-name color vote: the most frequent {@code §0-§f} code wins. */
public final class TeamColors {
    private TeamColors() {}

    public static String mostFrequent(String formatted) {
        if (formatted == null || formatted.isEmpty()) return null;
        Map<String, Integer> counts = new HashMap<>();
        String best = null;
        int bestCount = 0;
        for (int i = 0; i < formatted.length() - 1; i++) {
            if (formatted.charAt(i) != '§') continue;
            char code = Character.toLowerCase(formatted.charAt(i + 1));
            if ((code < '0' || code > '9') && (code < 'a' || code > 'f')) continue;
            String key = "§" + code;
            int next = counts.merge(key, 1, Integer::sum);
            if (next > bestCount) {
                bestCount = next;
                best = key;
            }
        }
        return best;
    }
}
