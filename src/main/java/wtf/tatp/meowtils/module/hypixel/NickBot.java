package wtf.tatp.meowtils.module.hypixel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.network.chat.Component;
import wtf.tatp.meowtils.CommandManager;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.MeowtilsData;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.GuiOpenEvent;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ButtonValue;
import wtf.tatp.meowtils.mixin.BookViewScreenAccessor;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.DelayedTask;
import wtf.tatp.meowtils.util.Settings;

public final class NickBot extends Module {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Character, Character> ALT_LETTERS = new HashMap<>();
    private static Set<String> list = new HashSet<>();
    private static boolean active;
    private static String currentNick = "";
    private static int skippedNicks;
    private static Object lastBook;
    private static boolean bookQueued;

    static {
        ALT_LETTERS.put('0', 'o');
        ALT_LETTERS.put('1', 'i');
        ALT_LETTERS.put('3', 'e');
        ALT_LETTERS.put('4', 'a');
        ALT_LETTERS.put('5', 's');
        ALT_LETTERS.put('6', 'g');
        ALT_LETTERS.put('7', 't');
        ALT_LETTERS.put('8', 'b');
        ALT_LETTERS.put('9', 'g');
    }

    public NickBot() {
        super("NickBot", Category.Hypixel);
        tag(ModuleTag.SAFE);
        tooltip("Automatically reroll nicks on Hypixel. Press ESC to stop it.\n§d/nickbot §f- For more info about commands");
        addButton(new ButtonValue("Start", 5.0f, NickBot::start));
        CommandManager.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("nickbot")
                .executes(c -> { help(); return 1; })
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("start").executes(c -> { start(); return 1; }))
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("list").executes(c -> { showWrapped(); return 1; }))
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("add")
                        .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("text", StringArgumentType.greedyString())
                                .executes(c -> { addEntry(StringArgumentType.getString(c, "text")); return 1; })))
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("remove")
                        .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("text", StringArgumentType.greedyString())
                                .executes(c -> { removeEntry(StringArgumentType.getString(c, "text")); return 1; }))));
    }

    public static void init() { load(); }

    private static File nickListFile() {
        return MeowtilsData.nickbotList().toFile();
    }

    @EventTarget
    public void onGuiOpen(GuiOpenEvent event) {
        if (mc.player == null || mc.level == null || !active) return;
        if (!(event.getGui() instanceof BookViewScreen book)) {
            lastBook = null;
            bookQueued = false;
            return;
        }
        queueBook(book);
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        if (active && InputConstants.isKeyDown(mc.getWindow(), InputConstants.KEY_ESCAPE)) {
            stop();
            return;
        }
        if (active && mc.gui.screen() instanceof BookViewScreen book) queueBook(book);
        else if (!(mc.gui.screen() instanceof BookViewScreen)) {
            lastBook = null;
            bookQueued = false;
        }
    }

    private void queueBook(BookViewScreen book) {
        if (book == lastBook || bookQueued) return;
        lastBook = book;
        bookQueued = true;
        int clickDelay = Math.max(0, Settings.integer(this, "delay", 500) / 50);
        new DelayedTask(() -> processBook(book), clickDelay);
    }

    private void processBook(BookViewScreen book) {
        bookQueued = false;
        if (!active) return;
        if (!(mc.gui.screen() instanceof BookViewScreen)) {
            stop();
            return;
        }
        String text = getBookText(book);
        if (text == null) {
            Meowtils.addMessage(ChatFormatting.RED + "Unable to find book text.");
            stop();
            return;
        }
        if (text.contains("Uh-oh!")) {
            new DelayedTask(() -> {
                Meowtils.sendCleanMessage("/nick help setrandom");
                Meowtils.addMessage(ChatFormatting.RED + "Error occurred, rolling nick again.");
                skippedNicks++;
            }, 40);
            return;
        }
        if (!text.contains("USE NAME") || !text.contains("TRY AGAIN")) {
            if (text.contains("TRY AGAIN")) {
                Meowtils.addMessage(ChatFormatting.RED + "An error occured in " + ChatFormatting.BLUE + "NickBot" + ChatFormatting.RED + ", re-rolling...");
                Meowtils.sendCleanMessage("/nick help setrandom");
            } else {
                stop();
            }
            return;
        }
        String nick = NickBotBook.extractNick(text);
        if (nick == null || nick.isEmpty()) {
            Meowtils.addMessage(ChatFormatting.RED + "Nick was null or empty.");
            stop();
            return;
        }
        if (Settings.bool(this, "listSkippedNicks", false)) {
            Meowtils.addMessage("Skipped: " + ChatFormatting.RED.toString() + ChatFormatting.ITALIC + nick);
        }
        if (isGoodNick(nick)) {
            if (Settings.bool(this, "autoAccept", true)) Meowtils.sendCleanMessage("/nick actuallyset " + nick);
            Meowtils.addMessage("Skipped nicks: " + ChatFormatting.YELLOW + skippedNicks);
            active = false;
            skippedNicks = 0;
            return;
        }
        Meowtils.sendCleanMessage("/nick help setrandom");
        skippedNicks++;
    }

    @EventTarget(priority = EventPriority.LOW)
    public void onChatReceived(ChatReceivedEvent event) {
        String msg = ColorUtil.unformattedText(event.getText());
        if (active) {
            if (msg.contains("You are now nicked as ")) {
                currentNick = msg.replace("You are now nicked as ", "").replace("!", "").replace(" ", "");
                Meowtils.sendCleanMessage("/nick help setrandom");
            } else if (msg.equals("You are not allowed to do this!")) {
                active = false;
                Meowtils.addMessage(ChatFormatting.RED + "Unable to nick.");
            }
            if (!event.isOverlay()) event.setCancelled(true);
        }
        if (Settings.bool(this, "ignoreLimboKick", false) && msg.equals("You were spawned in Limbo.") && !active) {
            Meowtils.addMessage(ChatFormatting.BLUE + "NickBot" + ChatFormatting.WHITE + ": Ignoring limbo..");
            new DelayedTask(() -> {
                Meowtils.sendCleanMessage("/lobby");
                new DelayedTask(NickBot::start, 20);
            }, 10);
        }
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.player == null || mc.level == null || !(mc.gui.screen() instanceof BookViewScreen) || !active) return;
        var g = event.getGraphics();
        wtf.tatp.meowtils.font.HudFont.draw(g, "NickBot Active", 10, 10, 1, 0xFFFFFFFF);
        wtf.tatp.meowtils.font.HudFont.draw(g, "Press " + ChatFormatting.RED + "ESC" + ChatFormatting.WHITE + " to stop.", 10, 20, 1, 0xFFFFFFFF);
        wtf.tatp.meowtils.font.HudFont.draw(g, "Skipped nicks: " + ChatFormatting.GREEN + skippedNicks, 10, 30, 1, 0xFFFFFFFF);
    }

    private String getBookText(BookViewScreen gui) {
        String text = readPageText(((BookViewScreenAccessor) gui).meowtils$bookAccess(), 0);
        if (!isBlank(text)) return text;
        if (mc.player != null) {
            text = readPageText(BookViewScreen.BookAccess.fromItem(mc.player.getMainHandItem()), 0);
            if (!isBlank(text)) return text;
            text = readPageText(BookViewScreen.BookAccess.fromItem(mc.player.getOffhandItem()), 0);
            if (!isBlank(text)) return text;
        }
        if (gui instanceof LecternScreen lectern) {
            text = readPageText(BookViewScreen.BookAccess.fromItem(lectern.getMenu().getBook()), 0);
            if (!isBlank(text)) return text;
        }
        return null;
    }

    private static String readPageText(BookViewScreen.BookAccess access, int page) {
        if (access == null || access.getPageCount() <= page) return null;
        Component component = access.getPage(page);
        if (component == null) return null;
        return NickBotBook.flattenPageText(component.getString());
    }

    private static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private static boolean isGoodNick(String nick) {
        NickBot n = get(NickBot.class);
        if (n == null || nick == null || nick.isEmpty()) return false;
        String lower = nick.toLowerCase();
        String normalized = normalizeAlt(lower);
        if (currentNick.toLowerCase().equals(lower)) return false;
        if ((lower.startsWith("xx") || lower.endsWith("xx")) && Settings.bool(n, "specialAffix", true)) return true;
        if (lower.length() == 4 && Settings.bool(n, "fourChar", true)) {
            Meowtils.addMessage("Four char nick: " + ChatFormatting.YELLOW + nick);
            return true;
        }
        if (lower.length() == 16 && normalized.matches("^[a-z]+$") && Settings.bool(n, "maxChar", false)) {
            Meowtils.addMessage("Max char nick: " + ChatFormatting.YELLOW + nick);
            return true;
        }
        if (normalized.matches("^[a-z_]+$") && Settings.bool(n, "plainText", false)) {
            Meowtils.addMessage("Plain text nick: " + ChatFormatting.YELLOW + nick);
            return true;
        }
        if (Character.isUpperCase(nick.charAt(0)) && Settings.bool(n, "legacyNicks", true)) {
            boolean extraUpper = false;
            for (int i = 1; i < nick.length(); i++) {
                if (Character.isUpperCase(nick.charAt(i))) { extraUpper = true; break; }
            }
            if (!extraUpper) {
                char last = 0;
                int count = 1;
                for (int i = 0; i < lower.length(); i++) {
                    char c = lower.charAt(i);
                    if ("aeiouy".indexOf(c) >= 0 && c == last) {
                        count++;
                        if (count >= 3) {
                            Meowtils.addMessage("Legacy nick: " + ChatFormatting.YELLOW + nick);
                            return true;
                        }
                    } else {
                        last = c;
                        count = 1;
                    }
                }
            }
        }
        if ((matches(lower) || matches(normalized)) && Settings.bool(n, "customWords", true)) {
            Meowtils.addMessage("Nick matched list: " + ChatFormatting.YELLOW + nick);
            return true;
        }
        return false;
    }

    private static String normalizeAlt(String nick) {
        StringBuilder sb = new StringBuilder();
        for (char c : nick.toCharArray()) sb.append(ALT_LETTERS.getOrDefault(c, c));
        return sb.toString();
    }

    public static void start() {
        NickBot module = get(NickBot.class);
        if (module == null || !module.getState()) {
            Meowtils.addMessage("Enable " + ChatFormatting.BLUE + "NickBot" + ChatFormatting.WHITE + " module to use this!");
            return;
        }
        active = true;
        lastBook = null;
        bookQueued = false;
        Meowtils.sendCleanMessage("/nick reuse");
        Meowtils.addMessage(ChatFormatting.GREEN + "Started " + ChatFormatting.BLUE + "NickBot" + ChatFormatting.GREEN + ".");
    }

    public static void stop() {
        active = false;
        bookQueued = false;
        Meowtils.addMessage(ChatFormatting.RED + "Stopped " + ChatFormatting.BLUE + "NickBot" + ChatFormatting.RED + ".");
    }

    public static boolean matches(String name) {
        if (name == null) return false;
        String name2 = name.toLowerCase();
        for (String entry : list) {
            if (entry == null || entry.length() < 2) continue;
            char type = entry.charAt(0);
            String value = entry.substring(1).toLowerCase();
            switch (type) {
                case '<' -> { if (name2.startsWith(value)) return true; }
                case '=' -> { if (name2.equals(value)) return true; }
                case '>' -> { if (name2.endsWith(value)) return true; }
                case '?' -> { if (name2.contains(value)) return true; }
                default -> { if (name2.contains(entry.toLowerCase())) return true; }
            }
        }
        return false;
    }

    public static void showList() {
        if (list.isEmpty()) {
            Meowtils.addMessage(ChatFormatting.RED + "Your list is empty!");
            return;
        }
        for (String text : list) Meowtils.addMessage(text);
    }

    public static boolean hasEntry(String name) { return list.contains(name.toLowerCase().replace(" ", "")); }
    public static void addName(String name) { list.add(name.toLowerCase().replace(" ", "")); save(); }
    public static void removeName(String name) { list.remove(name.toLowerCase().replace(" ", "")); save(); }

    private static void help() {
        String line = ChatFormatting.GRAY.toString() + ChatFormatting.STRIKETHROUGH + "--------------------------------";
        String sep = ChatFormatting.DARK_GRAY + " » " + ChatFormatting.GRAY;
        Meowtils.addMessage(line);
        Meowtils.addMessage(ChatFormatting.YELLOW + "/nickbot start" + sep + "Start the NickBot");
        Meowtils.addMessage(ChatFormatting.YELLOW + "/nickbot add" + sep + "Add nick entry");
        Meowtils.addMessage(ChatFormatting.YELLOW + "/nickbot remove" + sep + "Remove nick entry");
        Meowtils.addMessage(ChatFormatting.YELLOW + "/nickbot list" + sep + "Show word list");
        Meowtils.addMessage(ChatFormatting.GOLD.toString() + ChatFormatting.BOLD + "Syntax:");
        Meowtils.addMessage(ChatFormatting.GREEN + "?meow" + sep + "Contains meow");
        Meowtils.addMessage(ChatFormatting.GREEN + "=meow" + sep + "Equals meow");
        Meowtils.addMessage(ChatFormatting.GREEN + "<meow" + sep + "Starts with meow");
        Meowtils.addMessage(ChatFormatting.GREEN + ">meow" + sep + "Ends with meow");
        Meowtils.addMessage("Example: " + ChatFormatting.GREEN + "/nickbot add ?Ninja");
        Meowtils.addMessage(line);
    }

    private static void showWrapped() {
        String line = ChatFormatting.GRAY.toString() + ChatFormatting.STRIKETHROUGH + "--------------------------------";
        Meowtils.addMessage(line);
        showList();
        Meowtils.addMessage(line);
    }

    private static void addEntry(String text) {
        if (hasEntry(text)) Meowtils.addMessage(ChatFormatting.RED + "This is already an entry.");
        else {
            addName(text);
            Meowtils.addMessage(ChatFormatting.GREEN + "Added " + ChatFormatting.WHITE + text + ChatFormatting.GREEN + " to the list.");
        }
    }

    private static void removeEntry(String text) {
        if (!hasEntry(text)) Meowtils.addMessage(ChatFormatting.RED + "This is not an entry.");
        else {
            removeName(text);
            Meowtils.addMessage(ChatFormatting.RED + "Removed " + ChatFormatting.WHITE + text + ChatFormatting.RED + " from the list.");
        }
    }

    private static void load() {
        File file = nickListFile();
        if (!file.exists()) return;
        try {
            list = NickBotList.parse(java.nio.file.Files.readString(file.toPath()));
        } catch (Exception e) {
            list = new HashSet<>();
        }
    }

    private static void save() {
        File file = nickListFile();
        try {
            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) { GSON.toJson(list, writer); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReset() {
        active = false;
        currentNick = "";
        bookQueued = false;
        lastBook = null;
    }

    @Override
    public void onEnable() {
        Meowtils.addMessage(ChatFormatting.BLUE + "NickBot" + ChatFormatting.WHITE + " is still in beta, it may not be complete.");
    }
}
