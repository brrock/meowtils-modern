package wtf.tatp.meowtils.module.hypixel;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.CommandManager;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ButtonValue;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.TextValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.lists.UrchinManager;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.stats.StatsCache;
import wtf.tatp.meowtils.stats.StatsContainer;
import wtf.tatp.meowtils.stats.StatsManager;
import wtf.tatp.meowtils.stats.StatsSource;
import wtf.tatp.meowtils.stats.api.abyss.AbyssPlayer;
import wtf.tatp.meowtils.stats.api.hypixel.HypixelPlayer;
import wtf.tatp.meowtils.stats.api.hypixel.HypixelRecent;
import wtf.tatp.meowtils.stats.api.hypixel.HypixelStatus;
import wtf.tatp.meowtils.stats.api.urchin.UrchinPlayer;
import wtf.tatp.meowtils.stats.util.ChatStats;
import wtf.tatp.meowtils.stats.util.StatsChatGuard;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.DelayedTask;
import wtf.tatp.meowtils.util.NameUtil;
import wtf.tatp.meowtils.util.PlayerUtil;
import wtf.tatp.meowtils.util.Settings;

/** Core modern port of Meowtils Stats: UUID lookup and Hypixel BedWars/SkyWars stats. */
public final class Stats extends Module {
    private static final Pattern PLAYER_PATTERN = Pattern.compile("(?:\\[[^]]+]\\s+|§7)([A-Za-z0-9_]{1,16})");
    private static final StatsSource HYPIXEL_PLAYER = new HypixelPlayer();
    private static final StatsSource HYPIXEL_RECENT = new HypixelRecent();
    private static final StatsSource HYPIXEL_STATUS = new HypixelStatus();
    private static final StatsSource URCHIN_PLAYER = new UrchinPlayer();
    private static final StatsSource ABYSS = new AbyssPlayer();
    private static boolean LOGGED_URCHIN_KEY;
    private static boolean LOGGED_HYPIXEL_KEY;

    @Config public String api = "Abyss";
    @Config public String apiKey = "";
    @Config public int cache = 30;
    @Config public int cooldown = 100;
    @Config public String displayMode = "Compact";
    @Config public boolean tablist = true;
    @Config public boolean nametag = true;
    @Config public boolean chat = true;
    @Config public boolean autoCheck = true;
    @Config public boolean useTeamColor = false;
    @Config public boolean urchinApi = true;
    @Config public boolean urchinIgnoreSelf = false;
    @Config public boolean urchinIcon = true;
    @Config public boolean urchinChat = true;
    @Config public String urchinApiKey = "";
    @Config public boolean bedwars = true;
    @Config public boolean bedwarsLevel = true;
    @Config public boolean bedwarsKills = false;
    @Config public boolean bedwarsFinals = false;
    @Config public boolean bedwarsFkdr = true;
    @Config public boolean bedwarsWlr = false;
    @Config public boolean bedwarsWs = true;
    @Config public boolean bedwarsCr = false;
    @Config public boolean skywars = true;
    @Config public boolean skywarsLevel = true;
    @Config public boolean skywarsKills = false;
    @Config public boolean skywarsWins = false;
    @Config public boolean skywarsKdr = true;
    @Config public boolean skywarsWlr = true;

    public Stats() {
        super("Stats", Category.Hypixel);
        tag(ModuleTag.LEGIT);
        tooltip("Displays certain stats of players.");
        addMode(new ModeValue("API", Arrays.asList("Hypixel", "Abyss"), "api", this));
        addButton(new ButtonValue("Clear cache", 5.0f, () -> {
            StatsCache.clearCache();
            if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) Meowtils.addMessage("Cleared stats cache.");
            if (Notifications.getMode() != Notifications.Mode.CHAT) {
                NotificationManager.show("Stats", "Cleared cache.", NotificationManager.Type.INFO, 1500L);
            }
        }));
        SliderValue cacheDuration = new SliderValue("Cache duration", 5.0d, 60.0d, 5.0d, "min", "cache", this, Integer.TYPE);
        SliderValue fetchCooldown = new SliderValue("Fetch cooldown", 0.0d, 2000.0d, 50.0d, "ms", "cooldown", this, Integer.TYPE);
        addSlider(cacheDuration);
        addSlider(fetchCooldown);
        cacheDuration.set(30);
        fetchCooldown.set(100);
        addMode(new ModeValue("Display mode", Arrays.asList("Full", "Compact", "Lowercase"), "displayMode", this));
        addExpand(new ExpandValue("Display", expand -> {
            expand.addToggle(new ToggleValue("Tablist", "tablist", this));
            expand.addToggle(new ToggleValue("Nametags", "nametag", this));
            expand.addToggle(new ToggleValue("Chat", "chat", this));
        }, this));
        addExpand(new ExpandValue("Chat options", expand -> {
            expand.addToggle(new ToggleValue("Auto-check certain players", "autoCheck", this));
            expand.addToggle(new ToggleValue("Use team colors", "useTeamColor", this));
        }, this));
        addExpand(new ExpandValue("Urchin", expand -> {
            expand.addToggle(new ToggleValue("Check Urchin API", "urchinApi", this));
            expand.addToggle(new ToggleValue("Ignore self", "urchinIgnoreSelf", this));
            expand.addCheck(new CheckValue("Show name icons", "urchinIcon", this));
            expand.addCheck(new CheckValue("Show in chat", "urchinChat", this));
            expand.addText(new TextValue("API key", "Urchin API key", "urchinApiKey", this));
        }, this));
        addExpand(new ExpandValue("Bedwars", expand -> {
            expand.addToggle(new ToggleValue("Enabled", "bedwars", this));
            expand.addCheck(new CheckValue("Level", "bedwarsLevel", this));
            expand.addCheck(new CheckValue("Final kills", "bedwarsFinals", this));
            expand.addCheck(new CheckValue("FKDR", "bedwarsFkdr", this));
            expand.addCheck(new CheckValue("WLR", "bedwarsWlr", this));
            expand.addCheck(new CheckValue("Winstreak", "bedwarsWs", this));
            expand.addCheck(new CheckValue("Clutch ratio", "bedwarsCr", this));
        }, this));
        addExpand(new ExpandValue("Skywars", expand -> {
            expand.addToggle(new ToggleValue("Enabled", "skywars", this));
            expand.addCheck(new CheckValue("Level", "skywarsLevel", this));
            expand.addCheck(new CheckValue("Kills", "skywarsKills", this));
            expand.addCheck(new CheckValue("Wins", "skywarsWins", this));
            expand.addCheck(new CheckValue("KDR", "skywarsKdr", this));
            expand.addCheck(new CheckValue("WLR", "skywarsWlr", this));
        }, this));
        addText(new TextValue("Hypixel API key", "apiKey", this));
        CommandManager.register("s", root -> root
                .executes(context -> { Meowtils.addMessage(ChatFormatting.RED + "Usage: /s <sw|bw> <player>"); return 1; })
                .then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("mode", StringArgumentType.word())
                        .executes(context -> { stats(StringArgumentType.getString(context, "mode"), selfName()); return 1; })
                        .then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("player", StringArgumentType.word())
                                .executes(context -> { stats(StringArgumentType.getString(context, "mode"), StringArgumentType.getString(context, "player")); return 1; }))),
                "meowstat", "meowstats");
        CommandManager.register("meowapi", root -> root
                .executes(context -> { Meowtils.addMessage(ChatFormatting.RED + "Usage: /meowapi <key>"); return 1; })
                .then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("key", StringArgumentType.greedyString())
                        .executes(context -> {
                            apiKey = StringArgumentType.getString(context, "key");
                            settingsStorage().put("apiKey", apiKey);
                            ConfigManager.save();
                            Meowtils.addMessage("Set API key.");
                            return 1;
                        })),
                "meowapikey", "meowtilsapi");
    }

    public void request(String name) {
        if (Skywars.GAME.isActive() || Skywars.MINI.isActive()) ChatStats.showSkywars(name, false, true);
        else ChatStats.showBedwars(name, false, true);
    }

    public static void stats(String mode, String player) { ChatStats.stats(mode, player); }
    public static void showInfo(String name) { ChatStats.showInfo(name); }
    public static void showBedwarsStats(String name, boolean warnNicked, boolean compact) { ChatStats.showBedwars(name, warnNicked, compact); }
    public static void showSkywarsStats(String name, boolean warnNicked, boolean compact) { ChatStats.showSkywars(name, warnNicked, compact); }
    public static void showRecentGames(String name) { ChatStats.showRecent(name); }
    public static void showStatus(String name) { ChatStats.showStatus(name); }

    public static StatsSource getStatsSource() {
        Stats stats = module();
        String mode = stats == null ? "Abyss" : Settings.text(stats, "api", stats.api);
        return switch (mode) {
            case "Hypixel" -> HYPIXEL_PLAYER;
            case "Abyss" -> ABYSS;
            default -> null;
        };
    }

    public static void getStats(String name, StatsManager.Callback callback) {
        try {
            StatsSource source = getStatsSource();
            if (source != null) StatsManager.request(name, source, callback);
            else Meowtils.addMessage(ChatFormatting.RED + "Stats source is invalid.");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void getRecent(String uuid, StatsManager.Callback callback) {
        try {
            StatsManager.request(uuid, HYPIXEL_RECENT, callback);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void getStatus(String uuid, StatsManager.Callback callback) {
        try {
            StatsManager.request(uuid, HYPIXEL_STATUS, callback);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static StatsContainer getCached(String name) {
        StatsSource source = getStatsSource();
        if (source == null || name == null) return null;
        return StatsCache.getValid(source.getId() + ":" + name.toLowerCase());
    }

    public static void checkUrchin(String name, boolean manualCheck) {
        Minecraft mc = Minecraft.getInstance();
        Stats stats = module();
        if (stats == null || name == null || name.isEmpty() || isNicked(name)) return;
        if (!manualCheck && !Settings.bool(stats, "urchinApi", stats.urchinApi)) return;
        if (mc.player != null && name.equals(mc.player.getGameProfile().name())
                && Settings.bool(stats, "urchinIgnoreSelf", stats.urchinIgnoreSelf) && !manualCheck) {
            return;
        }
        if (urchinKey().isEmpty()) {
            if (manualCheck) Meowtils.addMessage(ChatFormatting.RED + "No Urchin API key is set!");
            return;
        }
        String missing = "This player is not in the " + ChatFormatting.DARK_PURPLE + "Urchin" + ChatFormatting.WHITE + " blacklist.";
        StatsContainer cached = getCachedUrchin(name);
        if (cached != null) {
            if (cached.urchinTags == null || cached.urchinTags.isEmpty()) {
                if (manualCheck) Meowtils.addMessage(missing);
                return;
            }
            sendUrchinAlert(name, cached, manualCheck);
            return;
        }
        requestUrchin(name, result -> {
            if (result == null) {
                if (manualCheck) Meowtils.addMessage(ChatFormatting.RED + "Failed to fetch Urchin tags for: " + ChatFormatting.GRAY + name);
                return;
            }
            if (result.urchinTags == null || result.urchinTags.isEmpty()) {
                if (manualCheck) Meowtils.addMessage(missing);
                return;
            }
            sendUrchinAlert(name, result, manualCheck);
        });
    }

    public static void requestUrchin(String name, StatsManager.Callback callback) {
        if (name == null || name.isEmpty()) {
            if (callback != null) callback.call(null);
            return;
        }
        if (urchinKey().isEmpty()) {
            logMissingUrchinKey();
            if (callback != null) callback.call(getCachedUrchin(name));
            return;
        }
        try {
            StatsManager.request(name, URCHIN_PLAYER, callback);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static StatsContainer getCachedUrchin(String name) {
        if (name == null || name.isEmpty()) return null;
        StatsContainer cached = StatsCache.getValid(URCHIN_PLAYER.getId() + ":" + name.toLowerCase());
        if (cached != null) return cached;
        UrchinManager.Entry saved = UrchinManager.get(name);
        if (saved == null || saved.tags == null || saved.tags.isEmpty()) return null;
        StatsContainer container = new StatsContainer();
        container.updateUrchinTags(saved.tags);
        return container;
    }

    @EventTarget
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.isOverlay() || mc.player == null || Server.HYPIXEL.isNotActive()) return;
        String raw = event.getText();
        String msg = ColorUtil.unformattedText(raw);
        if (StatsChatGuard.isOwnLine(raw) || StatsChatGuard.isOwnLine(msg)) return;
        if (Bedwars.GAME.isActive() && msg.startsWith("ONLINE:")) {
            try {
                String list = msg.startsWith("ONLINE: ") ? msg.substring(8) : msg.substring(7);
                for (String name : whoNames(list)) {
                    if (Settings.bool(this, "chat", chat)) ChatStats.showBedwars(name, false, true);
                    checkUrchin(name, false);
                }
            } catch (Exception exception) {
                Meowtils.error("Unable to parse who message: " + exception);
            }
        }
        if ((Skywars.GAME.isActive() || Skywars.MINI.isActive()) && msg.startsWith("Team #")) {
            try {
                int split = msg.indexOf(": ");
                for (String name : whoNames(split < 0 ? "" : msg.substring(split + 2))) {
                    if (Settings.bool(this, "chat", chat)) ChatStats.showSkywars(name, false, true);
                    checkUrchin(name, false);
                }
            } catch (Exception exception) {
                Meowtils.error("Unable to parse who message: " + exception);
            }
        }
        if (!Settings.bool(this, "autoCheck", autoCheck) || msg.contains("Guild >>") || msg.contains("Party >>")) return;
        Matcher playerMatcher = PLAYER_PATTERN.matcher(raw.isEmpty() ? msg : raw);
        Matcher playerMatcherFirst = PLAYER_PATTERN.matcher(raw.isEmpty() ? msg : raw);
        boolean isPartyFormat = (msg.contains("Party Members") || msg.contains("Party Leader") || msg.contains("Party Moderators")) && !msg.contains("summoned");
        try {
            if (Bedwars.ALL.isActive() && isPartyFormat) {
                while (playerMatcher.find()) {
                    String name = playerMatcher.group(1);
                    new DelayedTask(() -> ChatStats.showBedwars(name, false, false), 10);
                }
            } else if (Skywars.ALL.isActive() && isPartyFormat) {
                while (playerMatcher.find()) {
                    String name = playerMatcher.group(1);
                    new DelayedTask(() -> ChatStats.showSkywars(name, false, false), 10);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        String self = mc.player.getGameProfile().name();
        if (msg.contains(self) && !msg.contains("*") && msg.contains(":")) {
            while (playerMatcherFirst.find()) {
                try {
                    String name = playerMatcherFirst.group(1);
                    if (name.equalsIgnoreCase(self)) continue;
                    if (isNicked(name)) {
                        Meowtils.addMessage(ChatFormatting.GOLD + name + ChatFormatting.GRAY + " mentioned your name but is nicked!");
                    } else if (Bedwars.ALL.isActive()) {
                        ChatStats.showBedwars(name, false, false);
                    } else if (Skywars.ALL.isActive()) {
                        ChatStats.showSkywars(name, false, false);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
        if (Bedwars.PRE_GAME.isActive()) {
            while (playerMatcherFirst.find()) {
                try {
                    String name = playerMatcherFirst.group(1);
                    if (name.equalsIgnoreCase(self)) continue;
                    if (isNicked(name)) {
                        Meowtils.addMessage(ChatFormatting.GOLD + name + ChatFormatting.GRAY + " talked in pre-game but is nicked!");
                    } else {
                        ChatStats.showBedwars(name, false, true);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    return;
                }
            }
        }
    }

    private static Set<String> whoNames(String list) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (String token : ColorUtil.unformattedText(list).split(",")) {
            String plain = token.trim();
            if (plain.isEmpty()) continue;
            Matcher matcher = Pattern.compile("([A-Za-z0-9_]{1,16})$").matcher(plain);
            names.add(matcher.find() ? matcher.group(1) : plain);
        }
        return names;
    }

    public static Set<String> namesIn(String raw, String plain) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        collect(PLAYER_PATTERN.matcher(raw == null ? "" : raw), names);
        if (plain != null && !plain.equals(raw)) collect(PLAYER_PATTERN.matcher(plain), names);
        return names;
    }

    private static void collect(Matcher matcher, Set<String> names) {
        while (matcher.find()) names.add(matcher.group(1));
    }

    private static void sendUrchinAlert(String name, StatsContainer stats, boolean isManual) {
        String tagNames = stats.urchinTags.stream().map(tag -> formatUrchinTag(tag.type)).collect(java.util.stream.Collectors.joining(", "));
        String reasons = stats.urchinTags.stream().map(tag -> (tag.reason == null || tag.reason.isEmpty()) ? "Unknown" : tag.reason)
                .collect(java.util.stream.Collectors.joining(", "));
        if (name == null || name.isEmpty()) {
            Meowtils.addMessage(ChatFormatting.RED + "Unable to get Urchin information.");
            return;
        }
        Meowtils.addMessage(NameUtil.getTabDisplayName(name) + ChatFormatting.GRAY + " is tagged on " + ChatFormatting.DARK_PURPLE + "Urchin"
                + ChatFormatting.GRAY + " as " + ChatFormatting.RED + tagNames);
        Meowtils.addMessage(ChatFormatting.GRAY + "Reason: " + ChatFormatting.RED + reasons);
        if (!isManual) PartyNotifier.urchin(name, tagNames);
    }

    private static String formatUrchinTag(String tag) {
        return tag == null ? "unknown" : tag.replace('_', ' ');
    }

    private static boolean isNicked(String name) {
        return PlayerUtil.isNicked(PlayerUtil.getProfile(name));
    }

    public static String hypixelKey() {
        Stats stats = module();
        if (stats == null) return "";
        String stored = Settings.text(stats, "apiKey", stats.apiKey);
        return stored == null ? "" : stored.trim();
    }

    public static String urchinKey() {
        Stats stats = module();
        if (stats == null) return "";
        String stored = Settings.text(stats, "urchinApiKey", stats.urchinApiKey);
        return stored == null ? "" : stored.trim();
    }

    public static void logMissingUrchinKey() {
        if (LOGGED_URCHIN_KEY) return;
        LOGGED_URCHIN_KEY = true;
        Meowtils.warn("(Urchin) API key is missing; tab checks are skipped until you set one.");
    }

    public static void logMissingHypixelKey() {
        if (LOGGED_HYPIXEL_KEY) return;
        LOGGED_HYPIXEL_KEY = true;
        Meowtils.warn("(Hypixel API) API key is missing.");
    }

    private static Stats module() {
        return Module.get(Stats.class);
    }

    private static String selfName() {
        Minecraft client = Minecraft.getInstance();
        return client.player == null ? "" : client.player.getGameProfile().name();
    }
}
