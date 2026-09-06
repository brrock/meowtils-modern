package wtf.tatp.meowtils.stats.util;

import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import wtf.tatp.meowtils.stats.StatsContainer;

public final class StatsUtil {
    private StatsUtil() {}

    public static String getRank(JsonObject playerData) {
        if (playerData.has("rank") && !playerData.get("rank").isJsonNull()) {
            String rank = playerData.get("rank").getAsString();
            if (!rank.equals("NORMAL")) {
                if (rank.equals("STAFF")) return "ዞ";
                if (rank.equals("YOUTUBER")) return "YOUTUBE";
            }
        }
        if (playerData.has("monthlyPackageRank") && !playerData.get("monthlyPackageRank").isJsonNull()
                && "SUPERSTAR".equals(playerData.get("monthlyPackageRank").getAsString())) {
            return "MVP++";
        }
        if (playerData.has("newPackageRank") && !playerData.get("newPackageRank").isJsonNull()) {
            String newRank = playerData.get("newPackageRank").getAsString();
            return "VIP_PLUS".equals(newRank) ? "VIP+" : "MVP_PLUS".equals(newRank) ? "MVP+" : newRank;
        }
        if (playerData.has("packageRank") && !playerData.get("packageRank").isJsonNull()) {
            return playerData.get("packageRank").getAsString();
        }
        return "NONE";
    }

    public static ChatFormatting getPlusColor(JsonObject playerData) {
        if (playerData.has("rankPlusColor") && !playerData.get("rankPlusColor").isJsonNull()) {
            try {
                return ChatFormatting.valueOf(playerData.get("rankPlusColor").getAsString().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return ChatFormatting.RED;
            }
        }
        return ChatFormatting.RED;
    }

    public static ChatFormatting getRankColor(String rank) {
        return switch (rank) {
            case "MVP", "MVP+" -> ChatFormatting.AQUA;
            case "MVP++" -> ChatFormatting.GOLD;
            case "VIP", "VIP+" -> ChatFormatting.GREEN;
            case "YOUTUBE", "ዞ" -> ChatFormatting.RED;
            default -> ChatFormatting.GRAY;
        };
    }

    public static String getFormattedRankDisplay(String rank, ChatFormatting plusColor) {
        return "ዞ".equals(rank) ? ChatFormatting.RED + "[" + ChatFormatting.GOLD + "ዞ" + ChatFormatting.RED + "] "
                : "YOUTUBE".equals(rank) ? ChatFormatting.RED + "[" + ChatFormatting.WHITE + "YOUTUBE" + ChatFormatting.RED + "] "
                : "MVP++".equals(rank) ? ChatFormatting.GOLD + "[MVP" + plusColor + "++" + ChatFormatting.GOLD + "] "
                : "MVP+".equals(rank) ? ChatFormatting.AQUA + "[MVP" + plusColor + "+" + ChatFormatting.AQUA + "] "
                : "VIP+".equals(rank) ? ChatFormatting.GREEN + "[VIP" + ChatFormatting.GOLD + "+" + ChatFormatting.GREEN + "] "
                : !"NONE".equals(rank) ? getRankColor(rank) + "[" + rank + "] "
                : ChatFormatting.GRAY + " ";
    }

    public static String formattedChannel(String channel) {
        return switch (channel) {
            case "PARTY" -> ChatFormatting.BLUE + "Party";
            case "ALL" -> ChatFormatting.GREEN + "All";
            case "GUILD" -> ChatFormatting.DARK_GREEN + "Guild";
            case "OFFICER" -> ChatFormatting.DARK_AQUA + "Officer";
            case "Unknown" -> ChatFormatting.RED + "Unknown";
            default -> channel;
        };
    }

    public static boolean isUnknown(String stat) {
        return stat == null || stat.equals("Unknown") || stat.equals("?");
    }

    public static JsonObject getObj(JsonObject object, String key) {
        return object != null && object.has(key) && object.get(key).isJsonObject() ? object.getAsJsonObject(key) : new JsonObject();
    }

    public static boolean getBoolean(JsonObject object, String key, boolean fallback) {
        return object == null || !object.has(key) || object.get(key).isJsonNull() ? fallback : object.get(key).getAsBoolean();
    }

    public static String getString(JsonObject object, String key, String fallback) {
        return object == null || !object.has(key) || object.get(key).isJsonNull() ? fallback : object.get(key).getAsString();
    }

    public static int getInt(JsonObject object, String key, int fallback) {
        return object == null || !object.has(key) || object.get(key).isJsonNull() ? fallback : object.get(key).getAsInt();
    }

    public static long getLong(JsonObject object, String key, long fallback) {
        return object == null || !object.has(key) || object.get(key).isJsonNull() ? fallback : object.get(key).getAsLong();
    }

    public static StatsContainer parsePlayerData(JsonObject playerData) {
        JsonObject lastExp = getObj(playerData, "eugene");
        StatsContainer.GeneralStats general = new StatsContainer.GeneralStats(
                getRank(playerData), getPlusColor(playerData),
                getLong(playerData, "firstLogin", 0L), getLong(playerData, "lastLogin", 0L), getLong(playerData, "lastLogout", 0L),
                getInt(playerData, "networkExp", 0), getInt(playerData, "karma", 0), getInt(playerData, "achievementPoints", 0),
                getLong(playerData, "lastClaimedReward", 0L), getLong(lastExp, "dailyTwoKExp", 0L),
                getString(playerData, "channel", "Unknown"), getString(playerData, "userLanguage", "Unknown"));
        JsonObject bw = getObj(getObj(playerData, "stats"), "Bedwars");
        int finalKills = getInt(bw, "final_kills_bedwars", 0);
        int finalDeaths = getInt(bw, "final_deaths_bedwars", 1);
        int beds = getInt(bw, "beds_broken_bedwars", 0);
        int bedsLost = getInt(bw, "beds_lost_bedwars", 0);
        int wins = getInt(bw, "wins_bedwars", 0);
        int losses = getInt(bw, "losses_bedwars", 1);
        double clutchRatio = bedsLost == 0 ? 0.0d : 1.0d - ((double) finalDeaths / (double) bedsLost);
        StatsContainer.BedwarsStats bedwars = new StatsContainer.BedwarsStats(
                getInt(getObj(playerData, "achievements"), "bedwars_level", 0),
                finalKills, beds, bedsLost,
                (double) finalKills / (double) finalDeaths,
                (double) wins / (double) losses,
                getInt(bw, "winstreak", 0),
                clutchRatio);
        JsonObject sw = getObj(getObj(playerData, "stats"), "SkyWars");
        int skywarsKills = getInt(sw, "kills", 0);
        int skywarsDeaths = getInt(sw, "deaths", 1);
        int skywarsWins = getInt(sw, "wins", 0);
        int skywarsLosses = getInt(sw, "losses", 1);
        StatsContainer.SkywarsStats skywars = new StatsContainer.SkywarsStats(
                getString(sw, "levelFormatted", "0"),
                skywarsKills,
                (double) skywarsKills / (double) skywarsDeaths,
                skywarsWins,
                (double) skywarsWins / (double) skywarsLosses);
        StatsContainer container = new StatsContainer();
        container.updateGeneral(general);
        container.updateBedwars(bedwars);
        container.updateSkywars(skywars);
        return container;
    }
}
