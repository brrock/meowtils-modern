package wtf.tatp.meowtils.manager.icons.impl;

import com.mojang.authlib.GameProfile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.icons.IconProvider;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.module.hypixel.Stats;
import wtf.tatp.meowtils.stats.StatsContainer;
import wtf.tatp.meowtils.stats.util.BedwarsStatsUtil;
import wtf.tatp.meowtils.stats.util.SkywarsStatsUtil;
import wtf.tatp.meowtils.util.PlayerUtil;
import wtf.tatp.meowtils.util.Settings;

public final class StatsIcon implements IconProvider {
    private static final String SEPARATOR = net.minecraft.ChatFormatting.DARK_GRAY + " ● ";
    private static final String STARTER = " ▶ ";

    @Override
    public String getPrefix(GameProfile profile, boolean tablist, boolean nametag) {
        Stats module = module();
        if (!visible(module, profile, tablist, nametag)) return "";
        String name = profile.name();
        StatsContainer stats = Stats.getCached(name);
        if (stats == null && inGame()) {
            if (!PlayerUtil.isNicked(profile)) Stats.getStats(name, ignored -> {});
            return "";
        }
        if (Bedwars.GAME.isActive()) {
            if (stats == null || stats.bedwars == null) return "";
            String level = BedwarsStatsUtil.getFormattedLevel(stats.bedwars.level);
            return nametag ? level.replace("[", "").replace("]", "") + " " : level + " ";
        }
        if ((Skywars.GAME.isActive() || Skywars.MINI.isActive()) && stats != null && stats.skywars != null) {
            return stats.skywars.level + "✯ ";
        }
        return "";
    }

    @Override
    public String getSuffix(GameProfile profile, boolean tablist, boolean nametag) {
        Stats module = module();
        if (!visible(module, profile, tablist, nametag)) return "";
        if (nametag && Settings.text(module, "displayMode", module.displayMode).equals("Compact")) return "";
        boolean lowercase = Settings.text(module, "displayMode", module.displayMode).equals("Lowercase");
        boolean compact = Settings.text(module, "displayMode", module.displayMode).equals("Compact");
        String name = profile.name();
        StatsContainer stats = Stats.getCached(name);
        if (stats == null && inGame()) {
            if (!PlayerUtil.isNicked(profile)) Stats.getStats(name, ignored -> {});
            return "";
        }
        if (Bedwars.GAME.isActive()) {
            if (stats == null || stats.bedwars == null) return "";
            List<String> parts = new ArrayList<>();
            if (Settings.bool(module, "bedwarsFinals", module.bedwarsFinals)) {
                String label = compact ? "" : lowercase ? "f: " : "F: ";
                parts.add(BedwarsStatsUtil.getFinalsColor(stats.bedwars.finals) + label + stats.bedwars.finals);
            }
            if (Settings.bool(module, "bedwarsFkdr", module.bedwarsFkdr)) {
                String label = compact ? "" : lowercase ? "fkd: " : "FKD: ";
                parts.add(BedwarsStatsUtil.getFKDRColor(stats.bedwars.fkdr) + label + format(stats.bedwars.fkdr));
            }
            if (Settings.bool(module, "bedwarsWlr", module.bedwarsWlr)) {
                String label = compact ? "" : lowercase ? "wl: " : "WL: ";
                parts.add(BedwarsStatsUtil.getWLRColor(stats.bedwars.wlr) + label + format(stats.bedwars.wlr));
            }
            if (Settings.bool(module, "bedwarsWs", module.bedwarsWs)) {
                String label = compact ? "" : lowercase ? "ws: " : "WS: ";
                String winstreak = stats.bedwars.ws > 1 ? BedwarsStatsUtil.getWSColor(stats.bedwars.ws) + label + stats.bedwars.ws : "";
                if (!winstreak.isEmpty()) parts.add(winstreak);
            }
            if (Settings.bool(module, "bedwarsCr", module.bedwarsCr)) {
                String label = compact ? "" : lowercase ? "cr: " : "CR: ";
                parts.add(BedwarsStatsUtil.getClutchRatioColor(stats.bedwars.clutchRatio) + label
                        + BigDecimal.valueOf(stats.bedwars.clutchRatio).setScale(2, RoundingMode.DOWN).toPlainString().replace(",", "."));
            }
            return join(parts);
        }
        if ((!Skywars.GAME.isActive() && !Skywars.MINI.isActive()) || stats == null || stats.skywars == null) return "";
        List<String> parts = new ArrayList<>();
        if (Settings.bool(module, "skywarsKills", module.skywarsKills)) {
            String label = compact ? "" : lowercase ? "k: " : "K: ";
            parts.add(SkywarsStatsUtil.getKillsColor(stats.skywars.kills) + label + stats.skywars.kills);
        }
        if (Settings.bool(module, "skywarsWins", module.skywarsWins)) {
            String label = compact ? "" : lowercase ? "w: " : "W: ";
            parts.add(SkywarsStatsUtil.getWinsColor(stats.skywars.wins) + label + stats.skywars.wins);
        }
        if (Settings.bool(module, "skywarsKdr", module.skywarsKdr)) {
            String label = compact ? "" : lowercase ? "kd: " : "KD: ";
            parts.add(SkywarsStatsUtil.getKdrColor(stats.skywars.kdr) + label + format(stats.skywars.kdr));
        }
        if (Settings.bool(module, "skywarsWlr", module.skywarsWlr)) {
            String label = compact ? "" : lowercase ? "wl: " : "WL: ";
            parts.add(SkywarsStatsUtil.getWlrColor(stats.skywars.wlr) + label + format(stats.skywars.wlr));
        }
        return join(parts);
    }

    private static boolean visible(Stats module, GameProfile profile, boolean tablist, boolean nametag) {
        if (module == null || !module.getState() || Server.HYPIXEL.isNotActive() || profile == null) return false;
        if (nametag && !Settings.bool(module, "nametag", module.nametag)) return false;
        if (tablist && !Settings.bool(module, "tablist", module.tablist)) return false;
        return profile.name() != null && !PlayerUtil.isNicked(profile) && profile.id() != null && profile.id().version() != 2;
    }

    private static boolean inGame() {
        return Bedwars.GAME.isActive() || Skywars.GAME.isActive() || Skywars.MINI.isActive();
    }

    private static String format(double value) {
        return String.format("%.1f", value).replace(",", ".");
    }

    private static String join(List<String> parts) {
        return parts.isEmpty() ? "" : STARTER + String.join(SEPARATOR, parts);
    }

    private static Stats module() {
        return Module.get(Stats.class);
    }
}
