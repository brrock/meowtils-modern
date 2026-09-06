package wtf.tatp.meowtils.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.module.meowtils.Notifications;

public final class Util {
    public enum Sound { PING, PING_DEEP, PING_MEDIUM, LEVEL, ANVIL, MEOW, ANVIL_BREAK, ERROR, ERROR_DEEP, CRIT }

    private Util() {}

    public static void playSound(Sound sound, int volume) {
        wtf.tatp.meowtils.manager.SoundLoader.play(sound, volume);
    }

    public static void openFolder(File dir, String id) {
        try {
            if (!dir.exists()) dir.mkdirs();
            if (Desktop.isDesktopSupported()) {
                if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) Meowtils.addMessage("Opening " + id + " folder.");
                if (Notifications.getMode() != Notifications.Mode.CHAT) {
                    NotificationManager.show("Opening folder", id, NotificationManager.Type.INFO, 2000L);
                }
                Desktop.getDesktop().open(dir);
            } else {
                Meowtils.addMessage("§cFailed to open " + id + " folder on this system!");
                Meowtils.addMessage("§7Open it manually: §e" + dir.getAbsolutePath());
            }
        } catch (IOException e) {
            Meowtils.addMessage("§cFailed to open " + id + " folder: " + e.getMessage());
        }
    }

    public static int parseIntFromString(String string, int start) {
        int result = 0;
        for (int i = start; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c < '0' || c > '9') break;
            result = (result * 10) + (c - '0');
        }
        return result;
    }
}
