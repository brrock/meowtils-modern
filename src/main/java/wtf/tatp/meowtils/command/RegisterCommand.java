package wtf.tatp.meowtils.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import wtf.tatp.meowtils.CommandManager;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.MeowtilsClient;
import wtf.tatp.meowtils.MeowtilsData;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.extension.ExtensionManager;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.Value;
import wtf.tatp.meowtils.manager.lists.BlacklistManager;
import wtf.tatp.meowtils.manager.lists.FriendlistManager;
import wtf.tatp.meowtils.manager.lists.SafelistManager;
import wtf.tatp.meowtils.manager.session.SessionManager;
import wtf.tatp.meowtils.module.ChatFilter;
import wtf.tatp.meowtils.module.antisnipe.AutoBlacklist;
import wtf.tatp.meowtils.module.hypixel.Stats;
import wtf.tatp.meowtils.module.meowtils.GUI;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.module.meowtils.Settings;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.DelayedTask;
import wtf.tatp.meowtils.util.MojangNameToUUID;
import wtf.tatp.meowtils.util.NameUtil;
import wtf.tatp.meowtils.util.Prefix;

/** Original 2.0.1 client commands that modules do not already register. */
public final class RegisterCommand {
    private static final String HELP_LINE = ChatFormatting.GRAY.toString() + ChatFormatting.STRIKETHROUGH + "--------------------------------------------------";
    private static final String LIST_LINE = ChatFormatting.GRAY.toString() + ChatFormatting.STRIKETHROUGH + "---------------------------------------------------";
    private static final String FILTER_LINE = ChatFormatting.GRAY.toString() + ChatFormatting.STRIKETHROUGH + "--------------------------------";
    private static final String HELP_SEP = " » ";
    private static final String LIST_SEP = ChatFormatting.DARK_GRAY + " » ";
    private static final String FILTER_SEP = ChatFormatting.DARK_GRAY + " » " + ChatFormatting.GRAY;
    private static final ChatFormatting[] THEME_COLORS = {
            ChatFormatting.BLACK, ChatFormatting.DARK_GRAY, ChatFormatting.GRAY, ChatFormatting.WHITE,
            ChatFormatting.DARK_RED, ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW,
            ChatFormatting.DARK_GREEN, ChatFormatting.GREEN, ChatFormatting.DARK_AQUA, ChatFormatting.AQUA,
            ChatFormatting.DARK_BLUE, ChatFormatting.BLUE, ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE
    };
    private static long lastShout;
    private static boolean initialized;

    private RegisterCommand() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        registerAll();
    }

    private static void registerAll() {
        meow();
        play("1s", "/play bedwars_eight_one", "solo bedwars", "bw1");
        play("2s", "/play bedwars_eight_two", "doubles bedwars", "bw2");
        play("3s", "/play bedwars_four_three", "threes bedwars", "bw3");
        play("4s", "/play bedwars_four_four", "fours bedwars", "bw4");
        play("4v4", "/play bedwars_two_four", "4v4 bedwars", "bw4v4");
        play("sw", "/play solo_normal", "solo skywars");
        play("si", "/play solo_insane", "solo insane skywars");
        play("sw2", "/play teams_normal", "team skywars");
        play("sm", "/play mini_normal", "mini skywars", "mini");
        play("blitz1", "/play blitz_solo_normal", "blitz solo");
        play("blitz2", "/play blitz_teams_normal", "blitz teams");
        play("bridge4", "/play duels_bridge_four", "bridge 4v4 duels");
        play("bridge1", "/play duels_bridge_duel", "bridge duels");
        play("bridge3", "/play duels_bridge_threes", "bridge 3v3 duels");
        play("bridge2", "/play duels_bridge_doubles", "bridge 2v2 duels");
        play("classic", "/play duels_classic_duel", "classic duels");
        play("swduel", "/play duels_sw_duel", "skywars duels");
        play("sumo", "/play duels_sumo_duel", "sumo duels");
        play("uhc", "/play duels_uhc_duel", "uhc duels");
        play("mwfaceoff", "/play mw_face_off", "mega walls faceoff");
        play("mw", "/play mw_standard", "mega walls");
        play("tntrun", "/play tnt_tntrun", "tnt run");
        play("tnttag", "/play tnt_tntag", "tnt tag");
        play("blitz", "/play duels_blitz_duel", "blitz duels");
        play("mma", "/play murder_assasins", "murder mystery assasins");
        play("mmd", "/play murder_double_up", "murder mystery double up");
        play("mm", "/play murder_classic", "murder mystery");
        play("mmi", "/play murder_infection", "murder mystery infection");
        playCommands();
        shortcut("pt", args -> need(args, "/pt <player>", "/party transfer " + args[0]));
        bare("poffline", "/party kickoffline", "pko");
        bare("disband", "/party disband", "pdis");
        colorCodes();
        urchinTags();
        shout();
        lists();
        theme();
        debug();
        site("plancke", "pla", "'s stats on Plancke", name -> "https://plancke.io/hypixel/player/stats/" + name);
        site("namemc", "nmc", "'s profile on NameMC", name -> "https://namemc.com/profile/" + name);
        site("laby", null, "'s stats on Laby", name -> "https://laby.net/@" + name);
        site("karma", "kar", "'s stats on 25karma", name -> "https://25karma.xyz/player/" + name);
        site("shmeado", "shm", "'s stats on Shmeado", name -> "https://shmeado.club/player/stats/" + name);
        site("minemen", "mmc", "'s stats on Minemen Club", name -> "https://minemen.club/player/" + name);
        fakeMessage();
        block();
        bare("cp", "/chat party");
        bare("cg", "/chat guild");
        bare("ca", "/chat all");
        bare("cc", "/chat coop");
        bare("co", "/chat officer");
        bare("psa", "/party settings allinvite");
        shortcut("pp", args -> need(args, "/pp <player>", "/party promote " + args[0]));
        shortcut("gi", args -> need(args, "/gi <player>", "/guild info " + args[0]));
        bare("gt", "/guild toggle");
        CommandManager.register("slb", root -> greedyOptional(root, args -> {
            if (args.length == 0) usage("/slb <Lobby number>");
            else Meowtils.sendCleanMessage("/swaplobby " + args[0]);
        }));
        bare("ta", "/tip all");
        bare("rp", "/replay", "rpl");
        bare("glo", "/guild online");
        shortcut("unblock", args -> need(args, "/unblock <player>", "/block remove " + args[0]));
        bare("offline", "/status offline");
        bare("online", "/status online");
        bare("busy", "/status busy");
        bare("away", "/status away");
        shortcut("pd", args -> need(args, "/pd <player>", "/party demote " + args[0]));
        bare("renick", "/nick reuse");
        sendCommand();
        playerInfo();
        folder("capefolder", MeowtilsData.customCape(), "cape");
        shortcuts();
        resetGui();
        CommandManager.register("meowtest", root -> root.executes(c -> { Meowtils.addMessage("Ran test command."); return 1; }));
        friends();
        statsHelpers();
        gui();
        folder("skinfolder", MeowtilsData.customSkins(), "skin");
        CommandManager.register("extension", root -> root.executes(c -> {
            Meowtils.addMessage(ChatFormatting.RED.toString() + ChatFormatting.BOLD + "ONLY USE EXTENSIONS FROM TRUSTED SOURCES");
            Meowtils.openFolder(MeowtilsData.extensions(), "extension");
            return 1;
        }), "extensions", "extensionfolder");
        CommandManager.register("reload", root -> root.executes(c -> {
            Meowtils.addMessage("Reloading extensions...");
            try { ExtensionManager.reload(); }
            catch (Throwable t) {
                Meowtils.error("Extension reload failed: " + t.getMessage());
                Meowtils.addMessage(ChatFormatting.RED + "An error occurred while reloading extensions. Check logs for details.");
                t.printStackTrace();
            }
            return 1;
        }), "reloadextensions", "reloadextension");
        CommandManager.register("recent", root -> playerArg(root, Stats::showRecentGames), "recentgames", "recentgame");
        CommandManager.register("playerstatus", root -> playerArg(root, Stats::showStatus), "ps", "pstatus");
        CommandManager.register("urchin", root -> root
                .executes(c -> { Meowtils.addMessage(ChatFormatting.RED + "Usage: /urchin <player>"); return 1; })
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("player", StringArgumentType.word())
                        .executes(c -> { Stats.checkUrchin(StringArgumentType.getString(c, "player"), true); return 1; })),
                "urc", "ur");
        CommandManager.register("rj", root -> root.executes(c -> { Meowtils.sendCleanMessage("/rejoin"); return 1; }));
        bind();
        CommandManager.register("psp", root -> root.executes(c -> { Meowtils.sendCleanMessage("/party settings private"); return 1; }));
        ping();
        CommandManager.register("meowlog", root -> root.executes(c -> { openLog(); return 1; }));
        CommandManager.register("meowtilsfolder", root -> root.executes(c -> { Meowtils.openFolder(MeowtilsData.root(), "Meowtils"); return 1; }), "meowfolder");
        filter();
        gamemode();
        report();
    }

    private static void meow() {
        CommandManager.register("meow", root -> root
                .executes(c -> { help(1); return 1; })
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, Integer>argument("page", IntegerArgumentType.integer())
                        .executes(c -> { help(IntegerArgumentType.getInteger(c, "page")); return 1; })));
    }

    private static void help(int requested) {
        int page = Math.max(1, Math.min(7, requested));
        MutableComponent header = Component.empty();
        if (page > 1) {
            header.append(Component.literal(ChatFormatting.DARK_PURPLE + "                             «")
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/meow " + (page - 1)))));
        } else {
            header.append(Component.literal(ChatFormatting.DARK_PURPLE + "                             «"));
        }
        header.append(Component.literal(ChatFormatting.DARK_PURPLE + " Meowtils " + page + " "));
        if (page < 7) {
            header.append(Component.literal(ChatFormatting.DARK_PURPLE + "»")
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/meow " + (page + 1)))));
        } else {
            header.append(Component.literal(ChatFormatting.DARK_PURPLE + "»"));
        }
        Meowtils.addChat(header);
        Meowtils.addCleanMessage(HELP_LINE);
        switch (page) {
            case 1 -> {
                helpLine("/meow <page>", "Meowtils help");
                helpLine("/playcommands", "Show all short play commands");
                helpLine("/shortcuts", "Show all other shortcut commands");
                helpLine("/blacklist | /bl <player>", "Add to blacklist");
                helpLine("/unblacklist | /ubl <player>", "Remove from blacklist");
                helpLine("/safelist | /sl <player>", "Add to safelist");
                helpLine("/unsafelist | /usl <player>", "Remove from safelist");
            }
            case 2 -> {
                helpLine("/meowfriend | /mf <player>", "Add to friend list");
                helpLine("/meowunfriend | /muf <player>", "Remove from friend list");
                helpLine("/namemc | /nmc <ign>", "Link to NameMC");
                helpLine("/plancke | /pla <ign>", "Link to Plancke");
                helpLine("/laby <ign>", "Link to Laby");
                helpLine("/rq", "Requeues last played Hypixel game");
                helpLine("/s <bw|sw> <ign>", "Show stats for player");
            }
            case 3 -> {
                helpLine("/theme", "Change custom prefix color");
                helpLine("/itemblacklist <item>", "Add to item blacklist");
                helpLine("/itemunblacklist <item>", "Remove from item blacklist");
                helpLine("/itemsafelist <item>", "Add to item safelist");
                helpLine("/itemunsafelist <item>", "Remove from item safelist");
                helpLine("/capefolder", "Opens cape folder");
                helpLine("/skinfolder", "Opens skin folder");
            }
            case 4 -> {
                helpLine("/meowcolor", "Show all formatting codes");
                helpLine("/urchin <player>", "Check a player in Urchin API");
                helpLine("/urchintags", "Show Urchin tag colors");
                helpLine("/meowdebug", "Toggle debug mode");
                helpLine("/playerinfo <ign>", "Shows info about player");
                helpLine("/send <msg|command>", "Send message directly to server");
                helpLine("/resetgui", "Resets GUI positions");
                helpLine("/meowtilsgui", "Opens GUI");
            }
            case 5 -> {
                helpLine("/karma | /kar <ign>", "Link to 25karma");
                helpLine("/shmeado | /shm <ign>", "Link to Shmeado");
                helpLine("/recent <ign>", "Show recent Hypixel games");
                helpLine("/playerstatus | /ps <ign>", "Show player status");
                helpLine("/meowapi <key>", "Set Hypixel API key");
                helpLine("/extension", "Opens extension folder");
                helpLine("/reload", "Reloads all extensions");
            }
            case 6 -> {
                helpLine("/anticheat", "Change anticheat alert color");
                helpLine("/minemen | /mmc <ign>", "Link to Minemen Club");
                helpLine("/bind <key>", "Set GUI bind");
                helpLine("/fakemsg <msg>", "Display a fake message in chat");
                helpLine("/nickbot", "Show NickBot commands");
                helpLine("/meowping", "Show current ping");
                helpLine("/customname <text>", "Set AccountHider name");
            }
            default -> {
                helpLine("/meowlog", "Opens log");
                helpLine("/autogg", "Display Auto GG list options");
                helpLine("/autogl", "Display Auto GL list options");
                helpLine("/meowtilsfolder", "Opens Meowtils folder");
                helpLine("/meowfilter", "Show filter commands");
                helpLine("/gm <mode>", "Gamemode shortcut");
            }
        }
        Meowtils.addCleanMessage(HELP_LINE);
    }

    private static void helpLine(String command, String desc) {
        Meowtils.addCleanMessage(ChatFormatting.GREEN + command + ChatFormatting.YELLOW + HELP_SEP + ChatFormatting.DARK_GRAY + desc);
    }

    private static void playCommands() {
        CommandManager.register("playcommands", root -> root.executes(c -> {
            Meowtils.addCleanMessage(LIST_LINE);
            Meowtils.addCleanMessage(ChatFormatting.GOLD.toString() + ChatFormatting.BOLD + "Bedwars:");
            playLine("/1s or /bw1", "/play bedwars_eight_one");
            playLine("/2s or /bw2", "/play bedwars_eight_two");
            playLine("/3s or /bw3", "/play bedwars_four_three");
            playLine("/4s or /bw4", "/play bedwars_four_four");
            playLine("/4v4 or /bw4v4", "/play bedwars_four_two");
            Meowtils.addCleanMessage(ChatFormatting.GOLD.toString() + ChatFormatting.BOLD + "Skywars:");
            playLine("/sw", "/play solo_normal");
            playLine("/sw2", "/play teams_normal");
            playLine("/si", "/play solo_insane");
            playLine("/sm", "/play mini_normal");
            Meowtils.addCleanMessage(ChatFormatting.GOLD.toString() + ChatFormatting.BOLD + "Mega Walls:");
            playLine("/mw", "/play mw_standard");
            playLine("/mwfaceoff", "/play mw_face_off");
            Meowtils.addCleanMessage(ChatFormatting.GOLD.toString() + ChatFormatting.BOLD + "Blitz:");
            playLine("/blitz1", "/play blitz_solo_normal");
            playLine("/blitz2", "/play blitz_teams_normal");
            Meowtils.addCleanMessage(ChatFormatting.GOLD.toString() + ChatFormatting.BOLD + "TNT Games:");
            playLine("/tntrun", "/play tnt_tntrun");
            playLine("/tnttag", "/play tnt_tntag");
            Meowtils.addCleanMessage(ChatFormatting.GOLD.toString() + ChatFormatting.BOLD + "Duels:");
            playLine("/classic", "/play duels_classic_duel");
            playLine("/swduel", "/play duels_sw_duel");
            playLine("/uhcduel", "/play duels_uhc_duel");
            playLine("/sumo", "/play duels_sumo_duel");
            playLine("/bridge1", "/play duels_bridge_duel");
            playLine("/bridge2", "/play duels_bridge_doubles");
            playLine("/bridge3", "/play duels_bridge_threes");
            playLine("/bridge4", "/play duels_bridge_four");
            playLine("/blitz", "/play duels_blitz_duel");
            Meowtils.addCleanMessage(ChatFormatting.GOLD.toString() + ChatFormatting.BOLD + "Murder Mystery:");
            playLine("/mm", "/play murder_classic");
            playLine("/mmd", "/play murder_double_up");
            playLine("/mma", "/play murder_assasins");
            playLine("/mmi", "/play murder_infection");
            Meowtils.addCleanMessage(LIST_LINE);
            return 1;
        }));
    }

    private static void playLine(String command, String replacement) {
        Meowtils.addCleanMessage(ChatFormatting.YELLOW + command + LIST_SEP + ChatFormatting.GRAY + replacement);
    }

    private static void shortcuts() {
        CommandManager.register("shortcuts", root -> root.executes(c -> {
            Meowtils.addCleanMessage(LIST_LINE);
            playLine("/renick", "/nick reuse");
            playLine("/away", "/status away");
            playLine("/busy", "/status busy");
            playLine("/offline", "/status offline");
            playLine("/online", "/status online");
            playLine("/block", "/block add");
            playLine("/unblock", "/block remove");
            playLine("/ca", "/chat all");
            playLine("/cc", "/chat coop");
            playLine("/cg", "/chat guild");
            playLine("/co", "/chat officer");
            playLine("/cp", "/chat party");
            playLine("/gi", "/guild info");
            playLine("/glo", "/guild online");
            playLine("/gt", "/guild toggle");
            playLine("/pd", "/party demote");
            playLine("/disband", "/party disband");
            playLine("/poffline", "/party kickoffline");
            playLine("/pp", "/party promote");
            playLine("/psa", "/party settings allinvite");
            playLine("/pt", "/party transfer");
            playLine("/rp", "/replay");
            playLine("/sh", "/shout");
            playLine("/slb", "/swaplobby");
            playLine("/ta", "/tip all");
            playLine("/rj", "/rejoin");
            playLine("/psp", "/p settings private");
            Meowtils.addCleanMessage(LIST_LINE);
            return 1;
        }));
    }

    private static void colorCodes() {
        CommandManager.register("meowcolor", root -> root.executes(c -> {
            Meowtils.addCleanMessage(LIST_LINE);
            Meowtils.addCleanMessage(ChatFormatting.BLACK + "&0 » BLACK");
            Meowtils.addCleanMessage(ChatFormatting.DARK_BLUE + "&1 » DARK_BLUE");
            Meowtils.addCleanMessage(ChatFormatting.DARK_GREEN + "&2 » DARK_GREEN");
            Meowtils.addCleanMessage(ChatFormatting.DARK_AQUA + "&3 » DARK_AQUA");
            Meowtils.addCleanMessage(ChatFormatting.DARK_RED + "&4 » DARK_RED");
            Meowtils.addCleanMessage(ChatFormatting.DARK_PURPLE + "&5 » DARK_PURPLE");
            Meowtils.addCleanMessage(ChatFormatting.GOLD + "&6 » GOLD");
            Meowtils.addCleanMessage(ChatFormatting.GRAY + "&7 » GRAY");
            Meowtils.addCleanMessage(ChatFormatting.DARK_GRAY + "&8 » DARK_GRAY");
            Meowtils.addCleanMessage(ChatFormatting.BLUE + "&9 » BLUE");
            Meowtils.addCleanMessage(ChatFormatting.GREEN + "&a » GREEN");
            Meowtils.addCleanMessage(ChatFormatting.AQUA + "&b » AQUA");
            Meowtils.addCleanMessage(ChatFormatting.RED + "&c » RED");
            Meowtils.addCleanMessage(ChatFormatting.LIGHT_PURPLE + "&d » LIGHT_PURPLE");
            Meowtils.addCleanMessage(ChatFormatting.YELLOW + "&e » YELLOW");
            Meowtils.addCleanMessage(ChatFormatting.WHITE + "&f » WHITE");
            Meowtils.addCleanMessage(ChatFormatting.GRAY.toString() + ChatFormatting.UNDERLINE + "Formatting codes:");
            Meowtils.addCleanMessage(ChatFormatting.WHITE + "&k » " + ChatFormatting.OBFUSCATED + "OBFUSCATED");
            Meowtils.addCleanMessage(ChatFormatting.WHITE + "&m » " + ChatFormatting.STRIKETHROUGH + "STRIKETHROUGH");
            Meowtils.addCleanMessage(ChatFormatting.WHITE + "&o » " + ChatFormatting.ITALIC + "ITALIC");
            Meowtils.addCleanMessage(ChatFormatting.WHITE + "&l » " + ChatFormatting.BOLD + "BOLD");
            Meowtils.addCleanMessage(ChatFormatting.WHITE + "&n » " + ChatFormatting.UNDERLINE + "UNDERLINE");
            Meowtils.addCleanMessage(ChatFormatting.WHITE + "&r » RESET");
            Meowtils.addCleanMessage(LIST_LINE);
            return 1;
        }));
    }

    private static void urchinTags() {
        String arrow = ChatFormatting.BLUE + " » ";
        CommandManager.register("urchintags", root -> root.executes(c -> {
            Meowtils.addCleanMessage(LIST_LINE);
            Meowtils.addCleanMessage(ChatFormatting.DARK_RED + "✹" + arrow + ChatFormatting.DARK_RED + "Blatant Cheater");
            Meowtils.addCleanMessage(ChatFormatting.DARK_PURPLE + "✹" + arrow + ChatFormatting.DARK_PURPLE + "Confirmed Cheater");
            Meowtils.addCleanMessage(ChatFormatting.YELLOW + "✴" + arrow + ChatFormatting.YELLOW + "Closet Cheater");
            Meowtils.addCleanMessage(ChatFormatting.RED + "✹" + arrow + ChatFormatting.RED + "Sniper");
            Meowtils.addCleanMessage(ChatFormatting.YELLOW + "ⓘ" + arrow + ChatFormatting.YELLOW + "Caution");
            Meowtils.addCleanMessage(ChatFormatting.GRAY + "✹" + arrow + ChatFormatting.GRAY + "Info");
            Meowtils.addCleanMessage(ChatFormatting.DARK_GRAY + "✹" + arrow + ChatFormatting.DARK_GRAY + "Account");
            Meowtils.addCleanMessage(LIST_LINE);
            return 1;
        }), "utags", "utag");
    }

    private static void shout() {
        CommandManager.register("sh", root -> greedy(root, "/sh <msg>", args -> {
            Notifications notifications = Module.get(Notifications.class);
            boolean cooldown = notifications != null && wtf.tatp.meowtils.util.Settings.bool(notifications, "shoutCooldown", false);
            long now = System.currentTimeMillis();
            if (!cooldown || now - lastShout >= 60000) {
                lastShout = now;
                Meowtils.sendCleanMessage("/shout " + String.join(" ", args));
            } else {
                long left = Math.max(1, (60000 - (now - lastShout) + 999) / 1000);
                Meowtils.addMessage(ChatFormatting.BOLD + "Shout cooldown ends in " + ChatFormatting.RED.toString() + ChatFormatting.BOLD + left + ChatFormatting.WHITE.toString() + ChatFormatting.BOLD + " seconds!");
            }
        }), "shout");
    }

    private static void lists() {
        CommandManager.register("blacklist", root -> root
                .executes(c -> usage("/blacklist <player> [reasons]"))
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("player", StringArgumentType.greedyString())
                        .executes(c -> { blacklist(StringArgumentType.getString(c, "player").split(" ")); return 1; })),
                "bl");
        CommandManager.register("unblacklist", root -> playerWord(root, "/unblacklist <player>", RegisterCommand::unblacklist),
                "ubl", "blr", "blacklistremove", "unbl");
        CommandManager.register("safelist", root -> root
                .executes(c -> usage("/safelist <player>"))
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("player", StringArgumentType.greedyString())
                        .executes(c -> { safelist(StringArgumentType.getString(c, "player").split(" ")); return 1; })),
                "sl");
        CommandManager.register("unsafelist", root -> playerWord(root, "/unsafelist <player>", RegisterCommand::unsafelist),
                "usl", "slr", "safelistremove", "unsl");
    }

    private static void friends() {
        CommandManager.register("meowfriend", root -> root
                .executes(c -> usage("/meowfriend <player>"))
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("player", StringArgumentType.greedyString())
                        .executes(c -> { friend(StringArgumentType.getString(c, "player").split(" ")); return 1; })),
                "mf");
        CommandManager.register("meowunfriend", root -> playerWord(root, "/meowunfriend <player>", RegisterCommand::unfriend), "muf");
    }

    private static void blacklist(String[] args) {
        if (args.length == 0) { usage("/blacklist <player> [reasons]"); return; }
        if (args[0].equalsIgnoreCase("info")) {
            if (args.length < 2) { usage("/blacklist info <player>"); return; }
            String player = args[1];
            MojangNameToUUID.lookup(player, uuid -> {
                if (BlacklistManager.isBlacklisted(uuid)) Meowtils.addMessage(NameUtil.getTabDisplayName(player) + ChatFormatting.RED + " is blacklisted since: " + BlacklistManager.getFormattedEntry(uuid));
                else if (BlacklistManager.isBlacklisted(player)) Meowtils.addMessage(NameUtil.getTabDisplayName(player) + ChatFormatting.RED + " is blacklisted since: " + BlacklistManager.getFormattedEntry(player));
                else Meowtils.addMessage(NameUtil.getTabDisplayName(player) + ChatFormatting.GREEN + " is not blacklisted.");
            });
            return;
        }
        String player = args[0];
        String reason = args.length > 1 ? BlacklistManager.formatReasons(Arrays.copyOfRange(args, 1, args.length)) : "cheating";
        MojangNameToUUID.lookup(player, uuid -> {
            BlacklistManager.appendReason(uuid != null ? uuid : player, reason);
            Meowtils.addMessage(ChatFormatting.GREEN + "Updated blacklist for " + ChatFormatting.RESET + NameUtil.getTabDisplayName(player) + ChatFormatting.GREEN + ": " + BlacklistManager.colorReasons(reason));
        });
    }

    private static void unblacklist(String player) {
        MojangNameToUUID.lookup(player, uuid -> {
            if (BlacklistManager.isBlacklisted(uuid)) {
                BlacklistManager.remove(uuid);
                Meowtils.addMessage(ChatFormatting.YELLOW + "Removed " + ChatFormatting.RESET + NameUtil.getTabDisplayName(player) + ChatFormatting.YELLOW + " from the blacklist.");
            } else if (BlacklistManager.isBlacklisted(player)) {
                BlacklistManager.remove(player);
                Meowtils.addMessage(ChatFormatting.YELLOW + "Removed " + ChatFormatting.RESET + NameUtil.getTabDisplayName(player) + ChatFormatting.YELLOW + " from the blacklist.");
            } else {
                Meowtils.addMessage(NameUtil.getTabDisplayName(player) + ChatFormatting.GREEN + " is not blacklisted.");
            }
        });
    }

    private static void safelist(String[] args) {
        if (args.length == 0) { usage("/safelist <player>"); return; }
        if (args[0].equalsIgnoreCase("info")) {
            if (args.length < 2) { usage("/safelist info <player>"); return; }
            String player = args[1];
            MojangNameToUUID.lookup(player, uuid -> {
                if (SafelistManager.isSafelisted(uuid) || SafelistManager.isSafelisted(player)) Meowtils.addMessage(NameUtil.getTabDisplayName(player) + ChatFormatting.GREEN + " is safelisted.");
                else Meowtils.addMessage(NameUtil.getTabDisplayName(player) + ChatFormatting.RED + " is not in the safelist.");
            });
            return;
        }
        String player = args[0];
        MojangNameToUUID.lookup(player, uuid -> {
            if (SafelistManager.isSafelisted(uuid)) Meowtils.addMessage(NameUtil.getTabDisplayName(player) + ChatFormatting.GREEN + " is already safelisted.");
            else if (SafelistManager.isSafelisted(player)) Meowtils.addMessage(NameUtil.getTabDisplayName(player) + ChatFormatting.GREEN + " is already safelisted by name.");
            else if (uuid != null) {
                SafelistManager.add(uuid);
                Meowtils.addMessage(ChatFormatting.GREEN + "Safelisted " + ChatFormatting.RESET + NameUtil.getTabDisplayName(player) + ChatFormatting.GREEN + ".");
            } else {
                SafelistManager.add(player);
                Meowtils.addMessage(ChatFormatting.GREEN + "Safelisted " + ChatFormatting.RESET + NameUtil.getTabDisplayName(player) + ChatFormatting.GREEN + " by name.");
            }
        });
    }

    private static void unsafelist(String player) {
        MojangNameToUUID.lookup(player, uuid -> {
            if (SafelistManager.isSafelisted(uuid)) {
                SafelistManager.remove(uuid);
                Meowtils.addMessage(ChatFormatting.YELLOW + "Removed " + ChatFormatting.RESET + NameUtil.getTabDisplayName(player) + ChatFormatting.YELLOW + " from the safelist.");
            } else if (SafelistManager.isSafelisted(player)) {
                SafelistManager.remove(player);
                Meowtils.addMessage(ChatFormatting.YELLOW + "Removed " + ChatFormatting.RESET + NameUtil.getTabDisplayName(player) + ChatFormatting.YELLOW + " from the safelist.");
            } else {
                Meowtils.addMessage(NameUtil.getTabDisplayName(player) + ChatFormatting.RED + " is not in the safelist.");
            }
        });
    }

    private static void friend(String[] args) {
        if (args.length == 0) { usage("/meowfriend <player>"); return; }
        if (args[0].equalsIgnoreCase("info")) {
            if (args.length < 2) { usage("/meowfriend info <player>"); return; }
            String player = args[1];
            MojangNameToUUID.lookup(player, uuid -> {
                if (FriendlistManager.isFriendlisted(uuid) || FriendlistManager.isFriendlisted(player)) Meowtils.addMessage(NameUtil.getTabDisplayName(player) + ChatFormatting.GOLD + " is in your friend list.");
                else Meowtils.addMessage(NameUtil.getTabDisplayName(player) + ChatFormatting.RED + " is not in your friend list.");
            });
            return;
        }
        String player = args[0];
        MojangNameToUUID.lookup(player, uuid -> {
            if (FriendlistManager.isFriendlisted(uuid)) Meowtils.addMessage(NameUtil.getTabDisplayName(player) + ChatFormatting.GOLD + " is already in your friend list.");
            else if (FriendlistManager.isFriendlisted(player)) Meowtils.addMessage(NameUtil.getTabDisplayName(player) + ChatFormatting.GOLD + " is already in your friend list by name.");
            else if (uuid != null) {
                FriendlistManager.add(uuid);
                Meowtils.addMessage(ChatFormatting.GOLD + "Added " + ChatFormatting.RESET + NameUtil.getTabDisplayName(player) + ChatFormatting.GOLD + " to your friend list.");
            } else {
                FriendlistManager.add(player);
                Meowtils.addMessage(ChatFormatting.GOLD + "Added " + ChatFormatting.RESET + NameUtil.getTabDisplayName(player) + ChatFormatting.GOLD + " to your friend list by name.");
            }
        });
    }

    private static void unfriend(String player) {
        MojangNameToUUID.lookup(player, uuid -> {
            if (FriendlistManager.isFriendlisted(uuid)) {
                FriendlistManager.remove(uuid);
                Meowtils.addMessage(ChatFormatting.GRAY + "Removed " + ChatFormatting.RESET + NameUtil.getTabDisplayName(player) + ChatFormatting.GRAY + " from your friend list.");
            } else if (FriendlistManager.isFriendlisted(player)) {
                FriendlistManager.remove(player);
                Meowtils.addMessage(ChatFormatting.GRAY + "Removed " + ChatFormatting.RESET + NameUtil.getTabDisplayName(player) + ChatFormatting.GRAY + " from your friend list.");
            } else {
                Meowtils.addMessage(NameUtil.getTabDisplayName(player) + ChatFormatting.RED + " is not in your friend list.");
            }
        });
    }

    private static void theme() {
        CommandManager.register("theme", root -> root.executes(c -> {
            openTheme("m"); openTheme("e"); openTheme("o"); openTheme("w");
            Meowtils.addMessage("Bracket color:");
            openTheme("["); openTheme("]");
            return 1;
        }));
        CommandManager.register("settheme", root -> root
                .executes(c -> usage("/settheme <m|e|o|w> <COLOR_NAME>"))
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("letter", StringArgumentType.word())
                        .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("color", StringArgumentType.word())
                                .executes(c -> { setTheme(StringArgumentType.getString(c, "letter"), StringArgumentType.getString(c, "color")); return 1; }))));
    }

    private static void openTheme(String letter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        MutableComponent base = Meowtils.prefixed(ChatFormatting.WHITE + letter.toUpperCase(Locale.ROOT) + ": ").copy();
        for (ChatFormatting color : THEME_COLORS) {
            if (!ColorUtil.isColor(color)) continue;
            MutableComponent swatch = Component.literal("⬛").withStyle(color);
            swatch.setStyle(swatch.getStyle()
                    .withClickEvent(new ClickEvent.RunCommand("/settheme " + letter + " " + color.name()))
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to set " + letter.toUpperCase(Locale.ROOT) + " to " + color + color.name()))));
            base.append(swatch);
        }
        Meowtils.addChat(base);
    }

    static void applyClickedTheme(String letter, String color) {
        setTheme(letter, color);
    }

    private static void setTheme(String letterRaw, String colorRaw) {
        Settings settings = Module.get(Settings.class);
        if (settings == null) return;
        String letter = letterRaw.toLowerCase(Locale.ROOT);
        String colorName = colorRaw.toUpperCase(Locale.ROOT);
        try { ChatFormatting.valueOf(colorName); }
        catch (IllegalArgumentException ignored) { Meowtils.addMessage(ChatFormatting.RED + "Invalid color name."); return; }
        switch (letter) {
            case "m" -> settings.themeM = colorName;
            case "e" -> settings.themeE = colorName;
            case "o" -> settings.themeO = colorName;
            case "w" -> settings.themeW = colorName;
            case "[" -> settings.themeFirstBracket = colorName;
            case "]" -> settings.themeSecondBracket = colorName;
            default -> { Meowtils.addMessage(ChatFormatting.RED + "Invalid letter."); return; }
        }
        settings.syncThemeStorage();
        ConfigManager.save();
        Meowtils.addMessage(ChatFormatting.GREEN + "Set color for '" + letter.toUpperCase(Locale.ROOT) + "' to " + colorName);
    }

    private static void debug() {
        CommandManager.register("meowdebug", root -> root.executes(c -> {
            GUI gui = Module.get(GUI.class);
            if (gui == null) return 0;
            gui.debugMode = !gui.debugMode;
            if (gui.debugMode) {
                SessionManager.hypixel = true;
                SessionManager.bedwarsGame = true;
                SessionManager.skywarsGame = true;
            } else {
                SessionManager.hypixel = false;
                SessionManager.bedwarsGame = false;
                SessionManager.skywarsGame = false;
            }
            ConfigManager.save();
            String toggle = gui.debugMode
                    ? ChatFormatting.GREEN.toString() + ChatFormatting.BOLD + "ON"
                    : ChatFormatting.RED.toString() + ChatFormatting.BOLD + "OFF";
            Meowtils.addMessage("Debug Mode: " + toggle);
            return 1;
        }));
    }

    private static void fakeMessage() {
        CommandManager.register("fakemessage", root -> greedy(root, "/fakemessage <msg>", args ->
                Meowtils.addCleanMessage(ColorUtil.convertFormatting(String.join(" ", args)))), "fakemsg");
    }

    private static void sendCommand() {
        CommandManager.register("send", root -> greedy(root, "/send <msg | command>", args ->
                Meowtils.sendCleanMessage(String.join(" ", args))));
    }

    private static void playerInfo() {
        CommandManager.register("playerinfo", root -> playerArg(root, RegisterCommand::showPlayerInfo), "pi");
    }

    private static void showPlayerInfo(String name) {
        Minecraft client = Minecraft.getInstance();
        String playerName = name == null || name.isBlank() ? self() : name;
        if (client.level == null || client.getConnection() == null) {
            Meowtils.addMessage(ChatFormatting.RED + "Player not found in world.");
            return;
        }
        Player player = null;
        for (Player candidate : client.level.players()) {
            if (candidate.getGameProfile().name().equalsIgnoreCase(playerName)) { player = candidate; break; }
        }
        if (player == null) { Meowtils.addMessage(ChatFormatting.RED + "Player not found in world."); return; }
        PlayerInfo info = client.getConnection().getPlayerInfo(player.getUUID());
        if (info == null) { Meowtils.addMessage(ChatFormatting.RED + "Could not retrieve tablist info for " + playerName); return; }
        Meowtils.addMessage(ChatFormatting.GOLD + "Player Info: " + player.getGameProfile().name());
        Meowtils.addMessage(ChatFormatting.YELLOW + "Name: " + ChatFormatting.WHITE + player.getGameProfile().name());
        Meowtils.addMessage(ChatFormatting.YELLOW + "UUID: " + ChatFormatting.WHITE + player.getUUID());
        Meowtils.addMessage(ChatFormatting.YELLOW + "Existed: " + ChatFormatting.WHITE + player.tickCount + " ticks");
        Meowtils.addMessage(ChatFormatting.YELLOW + "Position: " + ChatFormatting.WHITE + String.format("%.2f, %.2f, %.2f", player.getX(), player.getY(), player.getZ()));
        Meowtils.addMessage(ChatFormatting.YELLOW + "Rotation: " + ChatFormatting.WHITE + String.format("%.2f, %.2f", player.getYRot(), player.getXRot()));
        Meowtils.addMessage(ChatFormatting.YELLOW + "On Ground: " + state(player.onGround()));
        Meowtils.addMessage(ChatFormatting.YELLOW + "Sneaking: " + state(player.isShiftKeyDown()));
        Meowtils.addMessage(ChatFormatting.YELLOW + "Health: " + ChatFormatting.WHITE + player.getHealth());
        Meowtils.addMessage(ChatFormatting.YELLOW + "Absorption: " + ChatFormatting.WHITE + player.getAbsorptionAmount());
        Meowtils.addMessage(ChatFormatting.YELLOW + "Dead: " + state(player.isDeadOrDying()));
        var held = player.getMainHandItem();
        Meowtils.addMessage(ChatFormatting.YELLOW + "Held Item: " + ChatFormatting.WHITE + (held.isEmpty() ? "None" : held.getHoverName().getString()));
        for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{
                net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
            var stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) Meowtils.addMessage(ChatFormatting.YELLOW + "Armor: " + ChatFormatting.WHITE + stack.getHoverName().getString());
        }
        Meowtils.addMessage(ChatFormatting.YELLOW + "Ping: " + ChatFormatting.WHITE + info.getLatency() + "ms");
        Meowtils.addMessage(ChatFormatting.YELLOW + "Scoreboard Name: " + ChatFormatting.WHITE + (info.getTabListDisplayName() != null ? info.getTabListDisplayName().getString() : player.getGameProfile().name()));
        Meowtils.addMessage(ChatFormatting.YELLOW + "Team: " + ChatFormatting.WHITE + player.getTeam());
        Meowtils.addMessage(ChatFormatting.YELLOW + "Gamemode: " + ChatFormatting.WHITE + info.getGameMode());
    }

    private static String state(boolean value) { return value ? ChatFormatting.GREEN + "true" : ChatFormatting.RED + "false"; }

    private static void resetGui() {
        CommandManager.register("resetgui", root -> root
                .executes(c -> usage("/resetgui <normal|side>"))
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("type", StringArgumentType.word())
                        .executes(c -> { resetFrames(StringArgumentType.getString(c, "type")); return 1; })));
    }

    private static void resetFrames(String type) {
        var frames = ConfigManager.guiConfig.frames;
        int index = 0;
        for (Module.Category category : Module.Category.values()) {
            var state = ConfigManager.guiConfig.frame(category.name(), index);
            if (type.equalsIgnoreCase("normal")) {
                state.x = index < 7 ? 5 + index * 85 : 5 + (index - 7) * 85;
                state.y = index < 7 ? 5 : 200;
            } else if (type.equalsIgnoreCase("side")) {
                state.x = 5;
                state.y = 5 + index * 20;
            } else {
                Meowtils.addMessage(ChatFormatting.RED + "Unknown type.");
                return;
            }
            state.open = false;
            frames.put(category.name(), state);
            index++;
        }
        ConfigManager.save();
        Meowtils.addMessage("Reset GUI positions.");
    }

    private static void gui() {
        CommandManager.register("meowtilsgui", root -> root.executes(c -> {
            new DelayedTask(() -> {
                MeowtilsClient.openClickGui();
                Meowtils.addMessage("Opened GUI! " + ChatFormatting.GREEN + "Make sure to bind GUI by middle clicking the \"" + ChatFormatting.YELLOW + "GUI" + ChatFormatting.GREEN + "\" module.");
            }, 5);
            return 1;
        }));
    }

    private static void bind() {
        CommandManager.register("bind", root -> root
                .executes(c -> usage("/bind <key>"))
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("key", StringArgumentType.word())
                        .executes(c -> { bindKey(StringArgumentType.getString(c, "key")); return 1; })));
    }

    private static void bindKey(String raw) {
        if (raw.length() >= 2) {
            Meowtils.addMessage(ChatFormatting.RED + "Invalid key, to set other keys use the GUI.");
            return;
        }
        char letter = Character.toUpperCase(raw.charAt(0));
        int key;
        if (letter >= 'A' && letter <= 'Z') key = GLFW.GLFW_KEY_A + (letter - 'A');
        else if (letter >= '0' && letter <= '9') key = GLFW.GLFW_KEY_0 + (letter - '0');
        else { Meowtils.addMessage(ChatFormatting.RED + "Unknown key: " + raw); return; }
        try {
            GUI gui = Module.get(GUI.class);
            if (gui == null) return;
            gui.key = key;
            gui.setKey(key);
            ConfigManager.save();
            Meowtils.addMessage("Set key to: " + ChatFormatting.YELLOW + letter);
        } catch (Exception e) {
            e.printStackTrace();
            Meowtils.addMessage(ChatFormatting.RED + "Error while attempting to set bind.");
        }
    }

    private static void ping() {
        CommandManager.register("meowping", root -> root.executes(c -> {
            Minecraft client = Minecraft.getInstance();
            if (client.getConnection() == null || client.hasSingleplayerServer()) {
                Meowtils.addMessage(ChatFormatting.RED + "Ping command only works in multiplayer!");
                return 1;
            }
            PlayerInfo info = client.player == null ? null : client.getConnection().getPlayerInfo(client.player.getUUID());
            int ping = info == null ? 0 : info.getLatency();
            ChatFormatting color = ping < 80 ? ChatFormatting.GREEN : ping < 150 ? ChatFormatting.YELLOW : ChatFormatting.RED;
            Meowtils.addMessage("Ping: " + color + ping + "ms");
            return 1;
        }));
    }

    private static void filter() {
        CommandManager.register("meowfilter", root -> root
                .executes(c -> { filterHelp(); return 1; })
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("syntax").executes(c -> { filterSyntax(); return 1; }))
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("reload").executes(c -> { filterReload(); return 1; }))
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("folder").executes(c -> { Meowtils.openFolder(MeowtilsData.chatFilters(), "filter"); return 1; }))
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("select")
                        .executes(c -> usage("/meowfilter select <filter name>"))
                        .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("name", StringArgumentType.greedyString())
                                .executes(c -> { filterSelect(StringArgumentType.getString(c, "name")); return 1; }))));
    }

    private static void filterHelp() {
        Meowtils.addMessage(FILTER_LINE);
        Meowtils.addMessage(ChatFormatting.YELLOW + "/meowfilter reload" + FILTER_SEP + "Reload current filter.");
        Meowtils.addMessage(ChatFormatting.YELLOW + "/meowfilter folder" + FILTER_SEP + "Open filter folder.");
        Meowtils.addMessage(ChatFormatting.YELLOW + "/meowfilter select <filter name>" + FILTER_SEP + "Selects filter.");
        Meowtils.addMessage(ChatFormatting.YELLOW + "/meowfilter syntax" + FILTER_SEP + "Shows list syntax.");
        Meowtils.addMessage(FILTER_LINE);
    }

    private static void filterSyntax() {
        Meowtils.addMessage(FILTER_LINE);
        Meowtils.addMessage(ChatFormatting.GREEN + "# <text>" + FILTER_SEP + "Comment (text after is ignored)");
        Meowtils.addMessage(ChatFormatting.GREEN + "-ServerName" + FILTER_SEP + "Limit to server");
        Meowtils.addMessage(ChatFormatting.GREEN + "&" + FILTER_SEP + "Chain conditions together");
        Meowtils.addMessage(ChatFormatting.GREEN + "?" + FILTER_SEP + "Contains");
        Meowtils.addMessage(ChatFormatting.GREEN + "=" + FILTER_SEP + "Equals");
        Meowtils.addMessage(ChatFormatting.GREEN + "<" + FILTER_SEP + "Starts with");
        Meowtils.addMessage(ChatFormatting.GREEN + ">" + FILTER_SEP + "Ends with");
        Meowtils.addMessage(ChatFormatting.GREEN + "!?" + FILTER_SEP + "Does not contain");
        Meowtils.addMessage(ChatFormatting.GREEN + "!=" + FILTER_SEP + "Does not equal");
        Meowtils.addMessage(ChatFormatting.GREEN + "!<" + FILTER_SEP + "Does not start with");
        Meowtils.addMessage(ChatFormatting.GREEN + "!>" + FILTER_SEP + "Does not end with");
        Meowtils.addMessage(FILTER_LINE);
    }

    private static void filterReload() {
        ChatFilter filter = Module.get(ChatFilter.class);
        if (filter != null) filter.reloadFilter();
        String selected = filter == null ? "default" : String.valueOf(filter.settingsStorage().getOrDefault("selectedFilter", "default"));
        Meowtils.addMessage("Reloaded filter: " + ChatFormatting.GREEN.toString() + ChatFormatting.ITALIC + selected);
    }

    @SuppressWarnings("unchecked")
    private static void filterSelect(String name) {
        ChatFilter filter = Module.get(ChatFilter.class);
        if (filter == null) return;
        filter.settingsStorage().put("selectedFilter", name);
        for (Object raw : filter.getAllValues()) {
            if (raw instanceof Value<?> value && "selectedFilter".equals(value.getConfig())) {
                ((Value<String>) value).setValue(name);
            }
        }
        filter.reloadFilter();
        ConfigManager.save();
    }

    private static void gamemode() {
        CommandManager.register("gm", root -> root
                .executes(c -> usage("/gm <0|1|2|3|s|c|a|spec"))
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("mode", StringArgumentType.word())
                        .executes(c -> {
                            String arg = StringArgumentType.getString(c, "mode");
                            if (arg.equalsIgnoreCase("0")) Meowtils.sendCleanMessage("/gamemode 0");
                            else if (arg.equalsIgnoreCase("1")) Meowtils.sendCleanMessage("/gamemode 1");
                            else if (arg.equalsIgnoreCase("2")) Meowtils.sendCleanMessage("/gamemode 2");
                            else if (arg.equalsIgnoreCase("3")) Meowtils.sendCleanMessage("/gamemode 3");
                            else Meowtils.addMessage(ChatFormatting.RED + "That gamemode doesn't exist.");
                            return 1;
                        })));
    }

    private static void report() {
        CommandManager.register("report", root -> greedyOptional(root, args -> {
            if (args.length == 0) { Meowtils.sendCleanMessage("/report"); return; }
            String msg = String.join(" ", args);
            String reasons = args.length > 1 ? BlacklistManager.formatReasons(Arrays.copyOfRange(args, 1, args.length)) : "cheating";
            Meowtils.sendCleanMessage("/report " + msg);
            AutoBlacklist auto = Module.get(AutoBlacklist.class);
            if (auto != null && wtf.tatp.meowtils.util.Settings.bool(auto, "forReports", auto.forReports)
                    && wtf.tatp.meowtils.util.Settings.bool(auto, "whenReportCommand", auto.whenReportCommand)) {
                AutoBlacklist.blacklistPlayer(args[0], reasons);
            }
        }));
        CommandManager.register("wdr", root -> greedyOptional(root, args -> {
            if (args.length == 0) { Meowtils.sendCleanMessage("/wdr"); return; }
            String msg = String.join(" ", args);
            String reasons = args.length > 1 ? BlacklistManager.formatReasons(Arrays.copyOfRange(args, 1, args.length)) : "cheating";
            Meowtils.sendCleanMessage("/wdr " + msg);
            AutoBlacklist auto = Module.get(AutoBlacklist.class);
            if (auto != null && wtf.tatp.meowtils.util.Settings.bool(auto, "forReports", auto.forReports)
                    && wtf.tatp.meowtils.util.Settings.bool(auto, "whenWdrCommand", auto.whenWdrCommand)) {
                AutoBlacklist.blacklistPlayer(args[0], reasons);
            }
        }), "watchdogreport");
    }

    private static void block() {
        CommandManager.register("block", root -> greedy(root, "/block <player>", args -> {
            String arg = args[0];
            if (arg.equalsIgnoreCase("list")) { Meowtils.sendCleanMessage("/block list"); return; }
            if (arg.equalsIgnoreCase("help")) { Meowtils.sendCleanMessage("/block help"); return; }
            if (arg.equalsIgnoreCase("removeall")) {
                if (args.length < 2) Meowtils.sendCleanMessage("/block removeall");
                else if (args[1].equalsIgnoreCase("cancel")) Meowtils.sendCleanMessage("/block removeall cancel");
                else if (args[1].equalsIgnoreCase("yesiamabouttodeleteallmyblocks")) Meowtils.sendCleanMessage("/block removeall yesiamabouttodeleteallmyblocks");
                return;
            }
            if (arg.equalsIgnoreCase("add") || arg.equalsIgnoreCase("remove")) {
                if (args.length < 2) usage("/block " + arg + " <player>");
                else Meowtils.sendCleanMessage("/block " + arg.toLowerCase(Locale.ROOT) + " " + args[1]);
                return;
            }
            Meowtils.sendCleanMessage("/block add " + arg);
        }));
    }

    private static void statsHelpers() {
        CommandManager.register("info", root -> playerArg(root, Stats::showInfo));
    }

    private static void openLog() {
        wtf.tatp.meowtils.manager.log.LogManager.open();
    }

    private static void play(String name, String command, String label, String... aliases) {
        CommandManager.register(name, root -> root.executes(c -> {
            Meowtils.sendCleanMessage(command);
            Meowtils.addMessage(ChatFormatting.GREEN + "Sending you to a " + ChatFormatting.DARK_AQUA.toString() + ChatFormatting.ITALIC + label + ChatFormatting.GREEN + " game.");
            return 1;
        }), aliases);
    }

    private static void bare(String name, String command, String... aliases) {
        CommandManager.register(name, root -> root.executes(c -> { Meowtils.sendCleanMessage(command); return 1; }), aliases);
    }

    private static void bare(String name, String ignored, java.util.function.Consumer<String[]> handler) {
        CommandManager.register(name, root -> greedyOptional(root, handler));
    }

    private static void shortcut(String name, java.util.function.Consumer<String[]> handler) {
        CommandManager.register(name, root -> greedy(root, null, handler));
    }

    private static void folder(String name, java.nio.file.Path path, String id) {
        CommandManager.register(name, root -> root.executes(c -> { Meowtils.openFolder(path, id); return 1; }));
    }

    private static void site(String name, String alias, String suffix, java.util.function.Function<String, String> url) {
        CommandManager.register(name, root -> playerArg(root, player -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;
            String target = player == null || player.isBlank() ? self() : player;
            MutableComponent message = Component.literal(Prefix.getPrefix() + ChatFormatting.GOLD.toString() + ChatFormatting.BOLD + target + suffix);
            message.setStyle(message.getStyle().withClickEvent(new ClickEvent.OpenUrl(URI.create(url.apply(target)))).withUnderlined(true));
            client.player.sendSystemMessage(message);
        }), alias == null ? new String[0] : new String[]{alias});
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> playerArg(LiteralArgumentBuilder<FabricClientCommandSource> root, java.util.function.Consumer<String> handler) {
        return root.executes(c -> { handler.accept(self()); return 1; })
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("player", StringArgumentType.word())
                        .executes(c -> { handler.accept(StringArgumentType.getString(c, "player")); return 1; }));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> playerWord(LiteralArgumentBuilder<FabricClientCommandSource> root, String usageText, java.util.function.Consumer<String> handler) {
        return root.executes(c -> usage(usageText))
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("player", StringArgumentType.word())
                        .executes(c -> { handler.accept(StringArgumentType.getString(c, "player")); return 1; }));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> greedy(LiteralArgumentBuilder<FabricClientCommandSource> root, String usageText, java.util.function.Consumer<String[]> handler) {
        return root.executes(c -> usageText == null ? 0 : usage(usageText))
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("args", StringArgumentType.greedyString())
                        .executes(c -> { handler.accept(StringArgumentType.getString(c, "args").split(" ")); return 1; }));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> greedyOptional(LiteralArgumentBuilder<FabricClientCommandSource> root, java.util.function.Consumer<String[]> handler) {
        return root.executes(c -> { handler.accept(new String[0]); return 1; })
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("args", StringArgumentType.greedyString())
                        .executes(c -> { handler.accept(StringArgumentType.getString(c, "args").split(" ")); return 1; }));
    }

    private static void need(String[] args, String usageText, String command) {
        if (args.length == 0) usage(usageText);
        else Meowtils.sendCleanMessage(command);
    }

    private static int usage(String text) {
        Meowtils.addMessage(ChatFormatting.RED + "Usage: " + text);
        return 1;
    }

    private static int send(String command) {
        Meowtils.sendCleanMessage(command);
        return 1;
    }

    private static String self() {
        Minecraft client = Minecraft.getInstance();
        return client.player == null ? "" : client.player.getGameProfile().name();
    }
}
