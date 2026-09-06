package wtf.tatp.meowtils.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

/** Sidebar text helper that works on native 26.2 and ViaVersion/1.8.9 Hypixel boards. */
public final class ScoreboardUtil {
    private ScoreboardUtil() {}
    public static Scoreboard getScoreboard() {
        var level = Minecraft.getInstance().level;
        return level == null ? null : level.getScoreboard();
    }
    public static Objective getSidebar() {
        Scoreboard board = getScoreboard();
        return board == null ? null : board.getDisplayObjective(DisplaySlot.SIDEBAR);
    }
    public static String getSidebarTitle() {
        Objective sidebar = getSidebar();
        return sidebar == null ? "" : ColorUtil.plainLower(sidebar.getDisplayName().getString());
    }
    public static List<String> getSidebarLines() {
        Scoreboard board = getScoreboard();
        Objective sidebar = getSidebar();
        if (board == null || sidebar == null) return List.of();
        List<String> lines = new ArrayList<>();
        for (var entry : board.listPlayerScores(sidebar)) {
            if (entry.isHidden()) continue;
            String owner = entry.owner();
            PlayerTeam team = board.getPlayersTeam(owner);
            String built = team == null ? owner
                    : team.getPlayerPrefix().getString() + owner + team.getPlayerSuffix().getString();
            if (entry.display() != null) built = built + " " + entry.display().getString();
            String line = ColorUtil.plainLower(built);
            if (!line.isBlank()) lines.add(line);
        }
        if (!lines.isEmpty()) return lines;
        for (PlayerTeam team : board.getPlayerTeams()) {
            String line = ColorUtil.plainLower(team.getPlayerPrefix().getString() + String.join("", team.getPlayers()) + team.getPlayerSuffix().getString());
            if (!line.isBlank()) lines.add(line);
        }
        return lines;
    }
    public static boolean titleContains(String text) { return getSidebarTitle().contains(text.toLowerCase(java.util.Locale.ROOT)); }
    public static boolean lineContains(String text) {
        String needle = text.toLowerCase(java.util.Locale.ROOT);
        return getSidebarLines().stream().anyMatch(line -> line.contains(needle));
    }
}
