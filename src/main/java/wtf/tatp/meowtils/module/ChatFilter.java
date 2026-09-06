package wtf.tatp.meowtils.module;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.MeowtilsData;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.ReceivePacketEvent;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ButtonValue;
import wtf.tatp.meowtils.manager.session.SessionManager;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.ScoreboardUtil;
import wtf.tatp.meowtils.util.Settings;

/** Hides configured game messages, optionally limited to a `-ServerName` prefix. */
public final class ChatFilter extends Module {
    private static final Pattern AND_SPLIT = Pattern.compile(" & (?=[?=<>!])");
    private static final List<String> DEFAULT = List.of(
            "# Visit docs.tatp.wtf/modules/utility/chatfilter for full documentation.",
            "# # - comment, will ignore any text after this",
            "# ?meow - contains meow",
            "# =meow - equals meow",
            "# <meow - starts with meow",
            "# >meow - ends with meow",
            "# !<any operator> - reversed condition",
            "# ? - combines conditions",
            "# -server - server/game name in scoreboard, limits filter to this only",
            "-hypixel",
            "?You are still radiating with Generosity!",
            "?Your game was boosted by",
            "<You tipped",
            ">'s Network Booster)",
            "=You are AFK. Move around to return from AFK.",
            "?joined the lobby!",
            "=If you get disconnected use /rejoin to join back in the game.",
            "?Click here to watch the Replay!",
            "<Teaming is not allowed",
            "=[WATCHDOG ANNOUNCEMENT]",
            "?Watchdog has banned",
            "=Blacklisted modifications are a bannable offense!",
            "=Rate this map by clicking: [5] [4] [3] [2] [1]",
            "=Prepare your defenses!",
            "=Click with any sword or bow to activate your skill!",
            "=Resource pack not working? Type /resource to fix it!",
            "=All games in this lobby are currently in development.",
            "<Click here to leave feedback!",
            "<PIT! Latest update:",
            "<>>> [MVP++] & ?joined the lobby!",
            "<Staff have banned an additional & >in the last 7 days.",
            "=Cross-teaming is not allowed! Report cross-teamers using /report.",
            "=Teaming with the Murderer is not allowed!",
            "?Gain XP and coins by » CLICKING HERE! «",
            "=Command Failed: This command is on cooldown! Try again in about a second!",
            "=Buy Network Boosters at https://store.hypixel.net");
    private static List<FilterRule> activeRules = Collections.emptyList();
    private static String allowedServer;
    private static long lastRefreshTime;
    private static boolean shouldFilter;

    public ChatFilter() {
        super("ChatFilter", Category.Utility);
        tag(ModuleTag.LEGIT);
        tooltip("Removes filtered messages.\n§d/meowfilter §f- Show commands");
        addButton(new ButtonValue("Open folder", 5.0f, () -> Meowtils.openFolder(MeowtilsData.chatFilters(), "filter")));
        addButton(new ButtonValue("Reload", 5.0f, () -> {
            reloadFilter();
            Meowtils.addMessage("Reloaded filter: " + ChatFormatting.GREEN.toString() + ChatFormatting.ITALIC
                    + Settings.text(this, "selectedFilter", "default"));
        }));
    }

    public static void init() {
        ChatFilter filter = get(ChatFilter.class);
        if (filter != null) filter.createDefault();
        reloadFilter();
    }

    public static void reloadFilter() {
        ChatFilter filter = get(ChatFilter.class);
        if (filter == null) return;
        try {
            activeRules = parseFilter(filter.loadFilter());
            filter.refreshGating();
        } catch (Exception e) {
            e.printStackTrace();
            activeRules = List.of();
        }
    }

    @Override
    public void onEnable() {
        reloadFilter();
    }

    @EventTarget(priority = EventPriority.LOWEST)
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.isOverlay() || activeRules.isEmpty() || !shouldFilter) return;
        String msg = ColorUtil.unformattedText(event.getText());
        try {
            for (FilterRule rule : activeRules) {
                if (rule.matches(msg)) event.setCancelled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Meowtils.addMessage(ChatFormatting.RED + "ChatFilter error, using: " + ChatFormatting.WHITE
                    + Settings.text(this, "selectedFilter", "default"));
        }
    }

    @EventTarget
    public void onReceivePacket(ReceivePacketEvent event) {
        Packet<?> packet = event.getPacket();
        if (!(packet instanceof ClientboundSetObjectivePacket)
                && !(packet instanceof ClientboundSetScorePacket)
                && !(packet instanceof ClientboundSetDisplayObjectivePacket)
                && !(packet instanceof ClientboundSetPlayerTeamPacket)) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastRefreshTime < 2000) return;
        lastRefreshTime = now;
        try {
            refreshGating();
        } catch (Exception e) {
            e.printStackTrace();
            Meowtils.addMessage(ChatFormatting.RED + "ChatFilter error, using: " + ChatFormatting.WHITE
                    + Settings.text(this, "selectedFilter", "default"));
        }
    }

    private void refreshGating() {
        if (allowedServer == null || allowedServer.isEmpty() || allowedServer.equalsIgnoreCase("all")) {
            shouldFilter = true;
        } else {
            shouldFilter = matchesServer(allowedServer);
        }
    }

    private static boolean matchesServer(String name) {
        if (ScoreboardUtil.lineContains(name) || ScoreboardUtil.titleContains(name)) return true;
        var info = Minecraft.getInstance().getCurrentServer();
        if (info != null) {
            String needle = name.toLowerCase(Locale.ROOT);
            if (String.valueOf(info.ip).toLowerCase(Locale.ROOT).contains(needle)) return true;
            if (String.valueOf(info.name).toLowerCase(Locale.ROOT).contains(needle)) return true;
        }
        return name.equalsIgnoreCase("hypixel") && SessionManager.hypixel;
    }

    private Path loadFilter() {
        String selected = Settings.text(this, "selectedFilter", "default");
        if (selected == null || selected.isBlank()) {
            selected = "default";
            settingsStorage().put("selectedFilter", selected);
            ConfigManager.save();
        }
        Path dir = MeowtilsData.chatFilters();
        Path filter = dir.resolve(selected.replace(".txt", "") + ".txt");
        if (Files.isRegularFile(filter)) return filter;
        Meowtils.warn("Filter: " + selected + " does not exist, changing to default.");
        Meowtils.addMessage(ChatFormatting.RED + "Filter does not exist. Changed to " + ChatFormatting.WHITE
                + ChatFormatting.ITALIC + "default" + ChatFormatting.RED + " filter.");
        settingsStorage().put("selectedFilter", "default");
        ConfigManager.save();
        createDefault();
        return MeowtilsData.defaultChatFilter();
    }

    private static List<FilterRule> parseFilter(Path file) {
        List<FilterRule> rules = new ArrayList<>();
        allowedServer = null;
        try {
            for (String raw : Files.readAllLines(file)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (line.startsWith("-")) {
                    allowedServer = line.substring(1).trim();
                } else {
                    FilterRule rule = parseRule(line);
                    if (rule != null) rules.add(rule);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.copyOf(rules);
    }

    private static FilterRule parseRule(String line) {
        String[] parts = AND_SPLIT.split(line);
        List<Condition> conditions = new ArrayList<>();
        for (String part : parts) {
            Condition condition = parseCondition(part.trim());
            if (condition != null) conditions.add(condition);
        }
        return conditions.isEmpty() ? null : new FilterRule(conditions);
    }

    private void createDefault() {
        Path file = MeowtilsData.defaultChatFilter();
        try {
            Files.createDirectories(MeowtilsData.chatFilters());
            if (!Files.isRegularFile(file) || Files.size(file) == 0 || isPortStub(file)) {
                Files.write(file, DEFAULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isPortStub(Path file) {
        try {
            String text = Files.readString(file).trim();
            return text.startsWith("# Meowtils chat filter") && !text.contains("-hypixel");
        } catch (Exception ignored) {
            return false;
        }
    }

    private static Condition parseCondition(String part) {
        if (part.isEmpty()) return null;
        boolean logicalNot = part.charAt(0) == '!';
        if (logicalNot) part = part.substring(1);
        if (part.isEmpty()) return null;
        char operator = part.charAt(0);
        String value = part.substring(1);
        return switch (operator) {
            case '<' -> new Condition(Condition.Type.STARTS_WITH, value, logicalNot);
            case '=' -> new Condition(Condition.Type.EQUALS, value, logicalNot);
            case '>' -> new Condition(Condition.Type.ENDS_WITH, value, logicalNot);
            case '?' -> new Condition(Condition.Type.CONTAINS, value, logicalNot);
            default -> null;
        };
    }

    private record FilterRule(List<Condition> conditions) {
        boolean matches(String message) {
            for (Condition condition : conditions) {
                if (!condition.matches(message)) return false;
            }
            return true;
        }
    }

    private record Condition(Type type, String value, boolean logicalNot) {
        enum Type { CONTAINS, EQUALS, STARTS_WITH, ENDS_WITH }

        Condition(Type type, String value, boolean logicalNot) {
            this.type = type;
            this.value = value.toLowerCase(Locale.ROOT);
            this.logicalNot = logicalNot;
        }

        boolean matches(String message) {
            String msg = message.toLowerCase(Locale.ROOT);
            boolean result = switch (type) {
                case CONTAINS -> msg.contains(value);
                case EQUALS -> msg.equals(value);
                case STARTS_WITH -> msg.startsWith(value);
                case ENDS_WITH -> msg.endsWith(value);
            };
            return logicalNot != result;
        }
    }
}
