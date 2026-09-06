package wtf.tatp.meowtils.stats.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.ChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.module.hypixel.Stats;
import wtf.tatp.meowtils.stats.StatsContainer;
import wtf.tatp.meowtils.util.NameUtil;
import wtf.tatp.meowtils.util.PlayerUtil;
import wtf.tatp.meowtils.util.Settings;

/** Original 2.0.1 chat surfaces for /s, /info, /recent, /playerstatus, and who-line checks. */
public final class ChatStats {
    private static final String LINE = ChatFormatting.DARK_GRAY + "---------------------------------------------";
    private static final String SEPARATOR = ChatFormatting.DARK_GRAY + " | ";
    private static final String HYPIXEL_API_ERROR = ChatFormatting.RED + "You do not have a " + ChatFormatting.BLUE + "Hypixel API"
            + ChatFormatting.RED + " key set." + ChatFormatting.WHITE + " Use: /meowapi <key>";

    private ChatStats() {}

    public static void stats(String mode, String player) {
        if (mode == null) return;
        if (mode.equalsIgnoreCase("sw") || mode.equalsIgnoreCase("skywars")) showSkywars(player, true, false);
        if (mode.equalsIgnoreCase("bw") || mode.equalsIgnoreCase("bedwars")) showBedwars(player, true, false);
    }

    public static void checkUrchin(String name, boolean manualCheck) {
        Stats.checkUrchin(name, manualCheck);
    }

    public static void showInfo(String name) {
        String player = resolve(name);
        if (PlayerUtil.isNicked(PlayerUtil.getProfile(player))) {
            Meowtils.addMessage(ChatFormatting.RED + "Can't get info for nicked player: " + ChatFormatting.GOLD + player);
            return;
        }
        Stats.getStats(player, stats -> {
            Stats module = module();
            if (stats == null || stats.general == null) {
                if (Stats.hypixelKey().isEmpty()) {
                    if (module != null && !Settings.text(module, "api", module.api).equals("Hypixel")) {
                        Meowtils.addMessage("This command requires " + ChatFormatting.BLUE + "Hypixel API" + ChatFormatting.WHITE + ".");
                    } else {
                        Meowtils.addMessage(HYPIXEL_API_ERROR);
                    }
                    return;
                }
                Meowtils.addMessage(ChatFormatting.RED + "Failed to fetch info for: " + ChatFormatting.GRAY + player);
                return;
            }
            String firstLogin = hidden(stats.general.firstLogin);
            String lastLogin = hidden(stats.general.lastLogin);
            String lastLogout = hidden(stats.general.lastLogout);
            String lastReward = hidden(stats.general.lastClaimedReward);
            String lastExp = hidden(stats.general.lastClaimedExp);
            double networkLevel = (Math.sqrt((2 * stats.general.networkExp) + 30625) / 50.0d) - 2.5d;
            Meowtils.addMessage(LINE);
            Meowtils.addMessage("Player: " + StatsUtil.getFormattedRankDisplay(stats.general.rank, stats.general.plusColor) + player);
            Meowtils.addMessage("First Login: " + firstLogin);
            Meowtils.addMessage("Last Login: " + lastLogin);
            Meowtils.addMessage("Last Logout: " + lastLogout);
            Meowtils.addMessage("Last Reward: " + lastReward);
            Meowtils.addMessage("Last EXP: " + lastExp);
            Meowtils.addMessage("Network Level: " + stats.general.plusColor + ((int) networkLevel));
            Meowtils.addMessage("AP: " + ChatFormatting.GOLD + stats.general.achievementPoints);
            Meowtils.addMessage("Karma: " + ChatFormatting.LIGHT_PURPLE + stats.general.karma);
            Meowtils.addMessage("Language: " + (StatsUtil.isUnknown(stats.general.language) ? ChatFormatting.RED + "?" : ChatFormatting.BLUE + stats.general.language));
            Meowtils.addMessage("Channel: " + (StatsUtil.isUnknown(stats.general.channel) ? ChatFormatting.RED + "?" : StatsUtil.formattedChannel(stats.general.channel)));
            Meowtils.addMessage(LINE);
        });
    }

    public static void showBedwars(String name, boolean warnNicked, boolean compact) {
        showBedwarsStats(name, warnNicked, compact);
    }

    public static void showBedwarsStats(String name, boolean warnNicked, boolean compact) {
        String player = resolve(name);
        if (!warnNicked && StatsChatGuard.alreadyShown(player, compact ? "bw-c" : "bw")) return;
        if (PlayerUtil.isNicked(PlayerUtil.getProfile(player))) {
            if (warnNicked) Meowtils.addMessage(ChatFormatting.RED + "Can't get stats for nicked player: " + ChatFormatting.GOLD + player);
            return;
        }
        Stats.getStats(player, stats -> {
            Stats module = module();
            if (stats == null || stats.general == null || stats.bedwars == null) {
                if (Stats.hypixelKey().isEmpty() && module != null && Settings.text(module, "api", module.api).equals("Hypixel")) {
                    Meowtils.addMessage(HYPIXEL_API_ERROR);
                } else {
                    Meowtils.addMessage(ChatFormatting.RED + "Failed to fetch bedwars stats for: " + ChatFormatting.GRAY + player);
                }
                return;
            }
            String rank;
            if (module != null && Settings.bool(module, "useTeamColor", module.useTeamColor) && Bedwars.GAME.isActive()) {
                rank = NameUtil.getTabDisplayName(player);
            } else {
                rank = StatsUtil.getFormattedRankDisplay(stats.general.rank, stats.general.plusColor) + player;
            }
            String ws = stats.bedwars.ws > 1 ? SEPARATOR + BedwarsStatsUtil.getWSColor(stats.bedwars.ws) + "WS: " + stats.bedwars.ws : "";
            if (compact) {
                Meowtils.addMessage(BedwarsStatsUtil.getFormattedLevel(stats.bedwars.level) + " " + rank + SEPARATOR
                        + BedwarsStatsUtil.getFKDRColor(stats.bedwars.fkdr) + "FKDR: " + fmt1(stats.bedwars.fkdr) + SEPARATOR
                        + BedwarsStatsUtil.getWLRColor(stats.bedwars.wlr) + "WLR: " + fmt1(stats.bedwars.wlr) + ws);
            } else {
                Meowtils.addMessage(BedwarsStatsUtil.getFormattedLevel(stats.bedwars.level) + " " + rank + SEPARATOR
                        + BedwarsStatsUtil.getFinalsColor(stats.bedwars.finals) + "Finals: " + stats.bedwars.finals + SEPARATOR
                        + BedwarsStatsUtil.getFKDRColor(stats.bedwars.fkdr) + "FKDR: " + fmt1(stats.bedwars.fkdr) + SEPARATOR
                        + BedwarsStatsUtil.getWLRColor(stats.bedwars.wlr) + "WLR: " + fmt1(stats.bedwars.wlr) + ws + SEPARATOR
                        + BedwarsStatsUtil.getClutchRatioColor(stats.bedwars.clutchRatio) + "CR: "
                        + BigDecimal.valueOf(stats.bedwars.clutchRatio).setScale(2, RoundingMode.DOWN).toPlainString().replace(",", "."));
            }
        });
    }

    public static void showSkywars(String name, boolean warnNicked, boolean compact) {
        showSkywarsStats(name, warnNicked, compact);
    }

    public static void showSkywarsStats(String name, boolean warnNicked, boolean compact) {
        String player = resolve(name);
        if (!warnNicked && StatsChatGuard.alreadyShown(player, compact ? "sw-c" : "sw")) return;
        if (PlayerUtil.isNicked(PlayerUtil.getProfile(player))) {
            if (warnNicked) Meowtils.addMessage(ChatFormatting.RED + "Can't get stats for nicked player: " + ChatFormatting.GOLD + player);
            return;
        }
        Stats.getStats(player, stats -> {
            Stats module = module();
            if (stats == null || stats.skywars == null) {
                if (Stats.hypixelKey().isEmpty() && module != null && Settings.text(module, "api", module.api).equals("Hypixel")) {
                    Meowtils.addMessage(HYPIXEL_API_ERROR);
                } else {
                    Meowtils.addMessage(ChatFormatting.RED + "Failed to fetch skywars stats for: " + ChatFormatting.GRAY + player);
                }
                return;
            }
            String rank = StatsUtil.getFormattedRankDisplay(stats.general.rank, stats.general.plusColor) + player;
            if (compact) {
                Meowtils.addMessage(stats.skywars.level + "✯ " + rank + SEPARATOR
                        + SkywarsStatsUtil.getKdrColor(stats.skywars.kdr) + "KDR: " + fmt1(stats.skywars.kdr) + SEPARATOR
                        + SkywarsStatsUtil.getWlrColor(stats.skywars.wlr) + "WLR: " + fmt1(stats.skywars.wlr));
            } else {
                Meowtils.addMessage(stats.skywars.level + "✯ " + rank + SEPARATOR
                        + SkywarsStatsUtil.getKillsColor(stats.skywars.kills) + "Kills: " + stats.skywars.kills + SEPARATOR
                        + SkywarsStatsUtil.getWinsColor(stats.skywars.kills) + "Wins: " + stats.skywars.wins + SEPARATOR
                        + SkywarsStatsUtil.getKdrColor(stats.skywars.kdr) + "KDR: " + fmt1(stats.skywars.kdr) + SEPARATOR
                        + SkywarsStatsUtil.getWlrColor(stats.skywars.wlr) + "WLR: " + fmt1(stats.skywars.wlr));
            }
        });
    }

    public static void showRecent(String name) {
        showRecentGames(name);
    }

    public static void showRecentGames(String name) {
        String player = resolve(name);
        if (PlayerUtil.isNicked(PlayerUtil.getProfile(player))) {
            Meowtils.addMessage(ChatFormatting.RED + "Can't get recent games for nicked player: " + ChatFormatting.GOLD + player);
            return;
        }
        wtf.tatp.meowtils.util.MojangNameToUUID.lookup(player, uuid -> {
            if (uuid == null || uuid.isEmpty()) {
                Meowtils.addMessage(ChatFormatting.RED + "Failed to get player UUID.");
                return;
            }
            Stats.getRecent(uuid, stats -> {
                Stats module = module();
                if (stats == null || stats.recent == null) {
                    if (Stats.hypixelKey().isEmpty()) {
                        if (module != null && !Settings.text(module, "api", module.api).equals("Hypixel")) {
                            Meowtils.addMessage("This command requires " + ChatFormatting.BLUE + "Hypixel API" + ChatFormatting.WHITE + ".");
                        } else {
                            Meowtils.addMessage(HYPIXEL_API_ERROR);
                        }
                        return;
                    }
                    Meowtils.addMessage(ChatFormatting.RED + "Couldn't find recent games for: " + ChatFormatting.GRAY + player);
                    return;
                }
                if (stats.recent.isEmpty()) {
                    Meowtils.addMessage(ChatFormatting.RED + "No recent stats available.");
                    return;
                }
                Meowtils.addMessage(ChatFormatting.DARK_GRAY + "--------------" + ChatFormatting.DARK_RED.toString() + ChatFormatting.BOLD + " Recent " + ChatFormatting.DARK_GRAY + "--------------");
                for (StatsContainer.RecentGames recent : stats.recent) {
                    Meowtils.addMessage("Game: " + (StatsUtil.isUnknown(recent.gameType) ? ChatFormatting.RED + "?" : ChatFormatting.RED + recent.gameType));
                    Meowtils.addMessage("Mode: " + (StatsUtil.isUnknown(recent.mode) ? ChatFormatting.RED + "?" : ChatFormatting.BLUE + recent.mode));
                    Meowtils.addMessage("Map: " + (StatsUtil.isUnknown(recent.map) ? ChatFormatting.RED + "?" : ChatFormatting.GOLD + recent.map));
                    Meowtils.addMessage("Started: " + (recent.date == 0 ? ChatFormatting.RED + "?" : ChatFormatting.LIGHT_PURPLE + Meowtils.formatTimestamp(recent.date)));
                    Meowtils.addMessage("Ended: " + (recent.date == 0 ? ChatFormatting.RED + "?" : ChatFormatting.LIGHT_PURPLE + Meowtils.formatTimestamp(recent.ended)));
                    Meowtils.addMessage(ChatFormatting.DARK_GRAY + "------------------------------------");
                }
            });
        });
    }

    public static void showStatus(String name) {
        String player = resolve(name);
        if (PlayerUtil.isNicked(PlayerUtil.getProfile(player))) {
            Meowtils.addMessage(ChatFormatting.RED + "Can't get status for nicked player: " + ChatFormatting.GOLD + player);
            return;
        }
        wtf.tatp.meowtils.util.MojangNameToUUID.lookup(player, uuid -> {
            if (uuid == null || uuid.isEmpty()) {
                Meowtils.addMessage(ChatFormatting.RED + "Failed to get player UUID.");
                return;
            }
            Stats.getStatus(uuid, stats -> {
                Stats module = module();
                if (stats == null || stats.status == null) {
                    if (Stats.hypixelKey().isEmpty()) {
                        if (module != null && !Settings.text(module, "api", module.api).equals("Hypixel")) {
                            Meowtils.addMessage("This command requires " + ChatFormatting.BLUE + "Hypixel API" + ChatFormatting.WHITE + ".");
                        } else {
                            Meowtils.addMessage(HYPIXEL_API_ERROR);
                        }
                        return;
                    }
                    Meowtils.addMessage(ChatFormatting.RED + "Couldn't find status for: " + ChatFormatting.GRAY + player);
                    return;
                }
                Meowtils.addMessage(ChatFormatting.DARK_GRAY + "--------------" + ChatFormatting.DARK_RED.toString() + ChatFormatting.BOLD + " Status " + ChatFormatting.DARK_GRAY + "--------------");
                Meowtils.addMessage("Online: " + (stats.status.online ? ChatFormatting.GREEN + "✓" : ChatFormatting.RED + "✗"));
                Meowtils.addMessage("Game: " + (StatsUtil.isUnknown(stats.status.gameType) ? ChatFormatting.RED + "?" : ChatFormatting.RED + stats.status.gameType));
                Meowtils.addMessage("Mode: " + (StatsUtil.isUnknown(stats.status.mode) ? ChatFormatting.RED + "?" : ChatFormatting.BLUE + stats.status.mode));
                Meowtils.addMessage("Map: " + (StatsUtil.isUnknown(stats.status.map) ? ChatFormatting.RED + "?" : ChatFormatting.GOLD + stats.status.map));
                Meowtils.addMessage(ChatFormatting.DARK_GRAY + "-----------------------------------");
            });
        });
    }

    private static String hidden(long value) {
        return value == 0 ? ChatFormatting.RED + "Hidden" : ChatFormatting.GRAY + Meowtils.formatTimestamp(value);
    }

    private static String fmt1(double value) {
        return String.format("%.1f", value).replace(",", ".");
    }

    private static String resolve(String name) {
        if (name == null || name.isBlank()) {
            var client = net.minecraft.client.Minecraft.getInstance();
            return client.player == null ? "" : client.player.getGameProfile().name();
        }
        return name;
    }

    private static Stats module() {
        return Module.get(Stats.class);
    }
}
