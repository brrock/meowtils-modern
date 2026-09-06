package wtf.tatp.meowtils.module.bedwars;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import wtf.tatp.meowtils.util.ColorUtil;

/** Parses Hypixel /map chat for {@link HeightOverlay}. */
public final class HeightOverlayMap {
    private static final String GAME_START = "The game starts in 1 second!";
    private static final String RESPAWN = "You will respawn in 6 seconds!";
    private static final Pattern MAP_PATTERN = Pattern.compile("You are currently playing on (.+)", Pattern.CASE_INSENSITIVE);

    private HeightOverlayMap() {}

    public static boolean isMapTrigger(String message) {
        if (message == null || message.isEmpty()) return false;
        String plain = ColorUtil.unformattedText(message).trim();
        return plain.equals(GAME_START) || plain.equals(RESPAWN);
    }

    public static String parseMapName(String message) {
        if (message == null || message.isEmpty()) return null;
        String plain = ColorUtil.unformattedText(message).trim();
        Matcher matcher = MAP_PATTERN.matcher(plain);
        if (!matcher.find()) return null;
        return cleanMapName(matcher.group(1));
    }

    private static String cleanMapName(String raw) {
        String name = raw.trim();
        while (!name.isEmpty() && isTrailingPunctuation(name.charAt(name.length() - 1))) {
            name = name.substring(0, name.length() - 1).trim();
        }
        return name.isEmpty() ? null : name;
    }

    private static boolean isTrailingPunctuation(char c) {
        return c == '.' || c == '!' || c == '?';
    }
}
