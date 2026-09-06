package wtf.tatp.meowtils;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import wtf.tatp.meowtils.extension.ExtensionManager;
import wtf.tatp.meowtils.font.HudFont;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.MessageManager;
import wtf.tatp.meowtils.module.meowtils.GUI;
import wtf.tatp.meowtils.util.Prefix;
import wtf.tatp.meowtils.util.Util;

/** Small compatibility facade retained for extensions ported from Meowtils 2.x. */
public final class Meowtils {
    private static final ThreadLocal<Boolean> POSTING = new ThreadLocal<>();
    private Meowtils() {}
    public static void info(String message) { System.out.println("[Meowtils] " + message); }
    public static void warn(String message) { System.out.println("[Meowtils] " + message); }
    public static void error(String message) { System.err.println("[Meowtils] " + message); }
    public static void fatal(String message) { error(message); }
    public static void debugMessage(String message) {
        GUI gui = Module.get(GUI.class);
        if (gui == null || !gui.debugMode) return;
        addMessage(message);
    }
    public static void addMessage(String message) {
        if (message == null) return;
        MessageManager.add(message);
        addChat(prefixed(ChatFormatting.RESET + message));
    }
    /** Prefixed chat line without a second {@link Prefix#getPrefix()} (clickable WDR / color pickers). */
    public static Component prefixed(String text) {
        return wtf.tatp.meowtils.module.bedwars.BedwarsSupport.legacy(Prefix.getPrefix() + (text == null ? "" : text));
    }
    public static void addCleanMessage(String message) {
        if (message == null) return;
        MessageManager.add(message);
        addChat(wtf.tatp.meowtils.module.bedwars.BedwarsSupport.legacy(message));
    }
    public static void addChat(Component component) {
        var client = MeowtilsClient.client();
        if (client == null || client.player == null || client.level == null || component == null) return;
        POSTING.set(Boolean.TRUE);
        try {
            client.player.sendSystemMessage(component);
        } finally {
            POSTING.remove();
        }
    }

    /** True while {@link #addChat} is writing, so incoming-chat hooks do not re-parse our own lines. */
    public static boolean isPostingLocalChat() {
        return Boolean.TRUE.equals(POSTING.get());
    }
    public static void sendMessage(String message) {
        var client = MeowtilsClient.client();
        if (client == null || client.player == null || message == null) return;
        if (message.startsWith("/")) {
            String command = message.substring(1);
            if (command.isEmpty()) return;
            // sendCommand() is intercepted by Fabric client commands. /wdr and /report
            // are registered locally and then forwarded here; sending them back through
            // sendCommand() re-enters the same handler until the stack overflows.
            client.player.connection.send(new ServerboundChatCommandPacket(command));
        } else {
            client.player.connection.sendChat(message);
        }
    }
    public static void sendCleanMessage(String message) { sendMessage(message); }
    public static void notify(String title, String message, NotificationManager.Type type, long duration) {
        NotificationManager.show(title, message, type, duration);
    }
    public static Path extensionDirectory() { return ExtensionManager.directory(); }
    public static void openFolder(File dir, String id) { Util.openFolder(dir, id); }
    public static void openFolder(Path dir, String id) { Util.openFolder(dir.toFile(), id); }
    public static void drawString(GuiGraphicsExtractor graphics, String text, int x, int y, float scale, int color) {
        HudFont.draw(graphics, text, x, y, scale, color);
    }
    public static void drawString(String text, int x, int y, float scale, int color) {
        if (text == null || text.isEmpty()) return;
    }
    public static int offsetString(float scale) { return HudFont.lineOffset(scale); }
    public static String formatTimestamp(long ms) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(ms));
    }
    public static String version() { return BuildInfo.version(); }
    public static String githubRepo() { return BuildInfo.githubRepo(); }
}
