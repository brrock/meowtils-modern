package wtf.tatp.meowtils.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import wtf.tatp.meowtils.CommandManager;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.MeowtilsData;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.DelayedTask;
import wtf.tatp.meowtils.util.HypixelUtil;
import wtf.tatp.meowtils.util.Settings;

/** Sends /ac messages on original Hypixel game-end and game-start chat lines. */
public final class AutoGG extends Module {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<ArrayList<String>>() {}.getType();
    private static final Random RANDOM = new Random();
    private static final List<String> DEFAULT_GG = List.of("gg", "Good Game", "gf");
    private static final List<String> DEFAULT_GL = List.of("Have a nice game!", "glhf");
    private static final String LINE = ChatFormatting.GRAY.toString() + ChatFormatting.STRIKETHROUGH + "--------------------------------";
    private static final String SEPARATOR = ChatFormatting.DARK_GRAY + " » " + ChatFormatting.GRAY;
    private static ArrayList<String> ggMessages = new ArrayList<>(DEFAULT_GG);
    private static ArrayList<String> glMessages = new ArrayList<>(DEFAULT_GL);
    private static String lastGgMessage = "";
    private static String lastGlMessage = "";
    private static boolean activated;

    public AutoGG() {
        super("AutoGG", Category.Hypixel);
        tag(ModuleTag.LEGIT);
        tooltip("Automatically sends messages on game start/end.\n§d/autogg|/autogl <add|remove|list> <msg> §f- Add/remove/list messages");
        CommandManager.register(command(true));
        CommandManager.register(command(false));
    }

    public static void init() {
        try {
            load();
        } catch (Throwable ignored) {
            if (ggMessages.isEmpty()) ggMessages = new ArrayList<>(DEFAULT_GG);
            if (glMessages.isEmpty()) glMessages = new ArrayList<>(DEFAULT_GL);
        }
    }

    @Override
    public void onEnable() {
        init();
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> command(boolean gg) {
        String root = gg ? "autogg" : "autogl";
        return LiteralArgumentBuilder.<FabricClientCommandSource>literal(root)
                .executes(c -> { help(root); return 1; })
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("list").executes(c -> {
                    Meowtils.addMessage(LINE);
                    if (gg) showGgMessages();
                    else showGlMessages();
                    Meowtils.addMessage(LINE);
                    return 1;
                }))
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("add")
                        .executes(c -> { Meowtils.addMessage(ChatFormatting.RED + "Usage: /" + root + " add <msg>"); return 1; })
                        .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("message", StringArgumentType.greedyString())
                                .executes(c -> { addEntry(gg, StringArgumentType.getString(c, "message")); return 1; })))
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("remove")
                        .executes(c -> { Meowtils.addMessage(ChatFormatting.RED + "Usage: /" + root + " remove <msg>"); return 1; })
                        .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("message", StringArgumentType.greedyString())
                                .executes(c -> { removeEntry(gg, StringArgumentType.getString(c, "message")); return 1; })));
    }

    private static void help(String root) {
        Meowtils.addMessage(LINE);
        Meowtils.addMessage(ChatFormatting.YELLOW + "/" + root + " add <msg>" + SEPARATOR + "Add message to list");
        Meowtils.addMessage(ChatFormatting.YELLOW + "/" + root + " remove <msg>" + SEPARATOR + "Remove message from list");
        Meowtils.addMessage(ChatFormatting.YELLOW + "/" + root + " list" + SEPARATOR + "List all messages");
        Meowtils.addMessage(LINE);
    }

    @EventTarget
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.isOverlay()) return;
        if (Server.HYPIXEL.isNotActive() && Server.UNIVERSAL.isNotActive()) return;
        String msg = ColorUtil.unformattedText(event.getText());
        int glDelay = Settings.integer(this, "autoglDelay", 5);
        String end = glDelay == 1 ? " second!" : " seconds!";
        if (Settings.bool(this, "autoggEnabled", true) && Settings.bool(this, "sendFirstMessage", true)) {
            if (HypixelUtil.isGameEnd(msg) && !msg.contains(":") && !activated) {
                String first = Settings.text(this, "firstMessage", "gg");
                String second = Settings.text(this, "secondMessage", "<3");
                if (first.isEmpty() || second.isEmpty() || ggMessages.isEmpty()) {
                    Meowtils.addMessage(ChatFormatting.BLUE + "[AutoGG]: " + ChatFormatting.WHITE + "No message set!");
                    return;
                }
                int firstDelay = Settings.integer(this, "autoggDelay", 0) / 50;
                int secondDelay = Settings.integer(this, "autoggSecondDelay", 0) / 50;
                new DelayedTask(() -> Meowtils.sendCleanMessage("/ac " + pickGg()), firstDelay);
                if (Settings.bool(this, "sendSecondMessage", false)) {
                    new DelayedTask(() -> Meowtils.sendCleanMessage("/ac " + pickGgSecond()), secondDelay);
                }
                activated = true;
            }
        }
        if (Settings.bool(this, "autoglEnabled", false) && msg.contains("The game starts in " + glDelay + end) && !msg.contains(":")) {
            String gl = Settings.text(this, "autoglMessage", "glhf");
            if (gl.isEmpty() || glMessages.isEmpty()) {
                Meowtils.addMessage(ChatFormatting.BLUE + "[AutoGL]: " + ChatFormatting.WHITE + "No message set!");
            } else {
                Meowtils.sendCleanMessage("/ac " + pickGl());
            }
        }
    }

    private String pickGg() {
        if (Settings.bool(this, "autoggRandom", false)) {
            lastGgMessage = getRandomMessage(ggMessages, lastGgMessage);
            return lastGgMessage;
        }
        return Settings.text(this, "firstMessage", "gg");
    }

    private String pickGgSecond() {
        if (Settings.bool(this, "autoggRandom", false)) {
            lastGgMessage = getRandomMessage(ggMessages, lastGgMessage);
            return lastGgMessage;
        }
        return Settings.text(this, "secondMessage", "<3");
    }

    private String pickGl() {
        if (Settings.bool(this, "autoglRandom", false)) {
            lastGlMessage = getRandomMessage(glMessages, lastGlMessage);
            return lastGlMessage;
        }
        return Settings.text(this, "autoglMessage", "glhf");
    }

    private static String getRandomMessage(ArrayList<String> messages, String lastMessage) {
        if (messages.isEmpty()) return "";
        if (messages.size() == 1) return messages.getFirst();
        String next;
        do {
            next = messages.get(RANDOM.nextInt(messages.size()));
        } while (next.equals(lastMessage));
        return next;
    }

    public static void showGgMessages() {
        init();
        if (ggMessages.isEmpty()) {
            Meowtils.addMessage(ChatFormatting.RED + "Your AutoGG message list is empty!");
            return;
        }
        if (ggMessages.size() > 40) {
            Meowtils.addMessage(ChatFormatting.RED + "Your AutoGG message list is too large to display, please open the file instead.");
            return;
        }
        for (String text : ggMessages) Meowtils.addMessage(text);
    }

    public static void showGlMessages() {
        init();
        if (glMessages.isEmpty()) {
            Meowtils.addMessage(ChatFormatting.RED + "Your AutoGL message list is empty!");
            return;
        }
        if (glMessages.size() > 40) {
            Meowtils.addMessage(ChatFormatting.RED + "Your AutoGL message list is too large to display, please open the file instead.");
            return;
        }
        for (String text : glMessages) Meowtils.addMessage(text);
    }

    public static void addGgMessage(String msg) { ggMessages.add(msg); save(); }
    public static void removeGgMessage(String msg) { ggMessages.remove(msg); save(); }
    public static void addGlMessage(String msg) { glMessages.add(msg); save(); }
    public static void removeGlMessage(String msg) { glMessages.remove(msg); save(); }
    public static boolean hasGgMessage(String msg) { return ggMessages.contains(msg); }
    public static boolean hasGlMessage(String msg) { return glMessages.contains(msg); }

    private static void addEntry(boolean gg, String msg) {
        init();
        if (gg ? hasGgMessage(msg) : hasGlMessage(msg)) {
            Meowtils.addMessage(msg + ChatFormatting.GREEN + " is already added.");
            return;
        }
        if (gg) addGgMessage(msg);
        else addGlMessage(msg);
        Meowtils.addMessage(ChatFormatting.GREEN + "Added " + ChatFormatting.WHITE + msg + ChatFormatting.GREEN + " to the list.");
    }

    private static void removeEntry(boolean gg, String msg) {
        init();
        if (!(gg ? hasGgMessage(msg) : hasGlMessage(msg))) {
            Meowtils.addMessage(msg + ChatFormatting.RED + " is not in the list.");
            return;
        }
        if (gg) removeGgMessage(msg);
        else removeGlMessage(msg);
        Meowtils.addMessage(ChatFormatting.RED + "Removed " + ChatFormatting.WHITE + msg + ChatFormatting.RED + " from the list.");
    }

    private static Path listPath(boolean gg) {
        return gg ? MeowtilsData.autoGgList() : MeowtilsData.autoGlList();
    }

    private static Path legacyPath(boolean gg) {
        return gg ? MeowtilsData.legacyAutoGg() : MeowtilsData.legacyAutoGl();
    }

    private static boolean usable(Path path) {
        try {
            if (!Files.isRegularFile(path)) return false;
            String text = Files.readString(path).trim();
            return !text.isEmpty() && !"{}".equals(text);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void load() {
        boolean initGg = !usable(listPath(true)) && !usable(legacyPath(true));
        boolean initGl = !usable(listPath(false)) && !usable(legacyPath(false));
        if (initGg) {
            ggMessages = new ArrayList<>(DEFAULT_GG);
        } else {
            readList(true);
        }
        if (initGl) {
            glMessages = new ArrayList<>(DEFAULT_GL);
        } else {
            readList(false);
        }
        if (initGg || initGl) save();
    }

    private static void readList(boolean gg) {
        Path preferred = listPath(gg);
        Path source = usable(preferred) ? preferred : legacyPath(gg);
        try {
            ArrayList<String> data = GSON.fromJson(Files.newBufferedReader(source), TYPE);
            ArrayList<String> parsed = data != null ? data : new ArrayList<>();
            if (gg) ggMessages = parsed;
            else glMessages = parsed;
            if (!source.equals(preferred)) writeList(preferred, parsed);
        } catch (Exception e) {
            e.printStackTrace();
            if (gg) ggMessages = new ArrayList<>();
            else glMessages = new ArrayList<>();
        }
    }

    private static void save() {
        writeList(listPath(true), ggMessages);
        writeList(listPath(false), glMessages);
    }

    private static void writeList(Path path, List<String> list) {
        try {
            Files.createDirectories(path.getParent());
            try (var writer = Files.newBufferedWriter(path)) {
                GSON.toJson(list, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReset() {
        activated = false;
    }
}
