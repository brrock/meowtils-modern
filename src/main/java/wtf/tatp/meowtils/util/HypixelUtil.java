package wtf.tatp.meowtils.util;

public final class HypixelUtil {
    public static final String[] GAME_END_MESSAGES = {"1st Killer -", "1st Place -", "Winner:", "- Damage Dealt -", "Winning Team -", "1st -", "Winners:", "Winning Team:", " won the game!", "Top Seeker:", "1st Place:", "Last team standing!", "Winner #1 (", "Top Survivors", "Winners -", "Sumo Duel -", "WINNER", "1st Killer"};
    private HypixelUtil() {}
    public static boolean isGameEnd(String text) {
        if (text == null || text.isBlank()) return false;
        for (String marker : GAME_END_MESSAGES) if (text.contains(marker)) return true;
        return false;
    }
}
