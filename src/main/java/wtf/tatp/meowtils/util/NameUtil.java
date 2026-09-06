package wtf.tatp.meowtils.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.scores.PlayerTeam;

/** Tab-styled name used by Skywars/Antisnipe alerts. */
public final class NameUtil {
    private NameUtil() {}

    public static String getTabDisplayName(String playerName) {
        if (playerName == null || playerName.isBlank()) return "NONE";
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfoIgnoreCase(playerName);
            if (info != null && info.getTabListDisplayName() != null) {
                String shown = info.getTabListDisplayName().getString();
                if (!shown.isBlank()) return shown;
            }
        }
        if (mc.level == null) return playerName;
        PlayerTeam team = mc.level.getScoreboard().getPlayersTeam(playerName);
        if (team == null) return playerName;
        return team.getPlayerPrefix().getString() + playerName + team.getPlayerSuffix().getString();
    }
}
