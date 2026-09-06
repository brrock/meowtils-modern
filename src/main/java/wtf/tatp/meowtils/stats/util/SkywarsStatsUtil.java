package wtf.tatp.meowtils.stats.util;

import net.minecraft.ChatFormatting;

public final class SkywarsStatsUtil {
    private SkywarsStatsUtil() {}

    public static ChatFormatting getKillsColor(int kills) {
        return kills == 0 ? ChatFormatting.BLUE : kills < 1000 ? ChatFormatting.GRAY : kills < 2000 ? ChatFormatting.WHITE
                : kills < 3000 ? ChatFormatting.GREEN : kills < 5000 ? ChatFormatting.DARK_GREEN : kills < 15000 ? ChatFormatting.YELLOW
                : kills < 30000 ? ChatFormatting.GOLD : kills < 50000 ? ChatFormatting.RED : kills < 70000 ? ChatFormatting.DARK_RED
                : kills < 100000 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_PURPLE;
    }

    public static ChatFormatting getKdrColor(double kdr) {
        return kdr == 0.0d ? ChatFormatting.BLUE : kdr < 0.3d ? ChatFormatting.GRAY : kdr < 0.9d ? ChatFormatting.WHITE
                : kdr < 1.5d ? ChatFormatting.GREEN : kdr < 2.0d ? ChatFormatting.DARK_GREEN : kdr < 3.0d ? ChatFormatting.YELLOW
                : kdr < 6.0d ? ChatFormatting.GOLD : kdr < 9.0d ? ChatFormatting.RED : kdr < 15.0d ? ChatFormatting.DARK_RED
                : kdr < 30.0d ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_PURPLE;
    }

    public static ChatFormatting getWinsColor(int wins) {
        return wins == 0 ? ChatFormatting.BLUE : wins < 100 ? ChatFormatting.GRAY : wins < 500 ? ChatFormatting.WHITE
                : wins < 1000 ? ChatFormatting.GREEN : wins < 1500 ? ChatFormatting.DARK_GREEN : wins < 3000 ? ChatFormatting.YELLOW
                : wins < 5000 ? ChatFormatting.GOLD : wins < 8000 ? ChatFormatting.RED : wins < 10000 ? ChatFormatting.DARK_RED
                : ChatFormatting.LIGHT_PURPLE;
    }

    public static ChatFormatting getWlrColor(double wlr) {
        return wlr == 0.0d ? ChatFormatting.BLUE : wlr < 0.1d ? ChatFormatting.GRAY : wlr < 0.5d ? ChatFormatting.WHITE
                : wlr < 1.0d ? ChatFormatting.GREEN : wlr < 2.0d ? ChatFormatting.DARK_GREEN : wlr < 3.0d ? ChatFormatting.YELLOW
                : wlr < 4.0d ? ChatFormatting.GOLD : wlr < 7.0d ? ChatFormatting.RED : wlr < 10.0d ? ChatFormatting.DARK_RED
                : wlr < 15.0d ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_PURPLE;
    }
}
