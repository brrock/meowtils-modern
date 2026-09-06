package wtf.tatp.meowtils.command;

import java.util.Locale;
import wtf.tatp.meowtils.module.antisnipe.AntiCheat;

/**
 * 26.2 chat clicks use {@code sendUnattendedCommand}, which never hits Fabric client
 * commands. Route the original prefix/color pickers back onto the client.
 */
public final class ChatClicks {
    private ChatClicks() {}

    public static boolean handle(String raw) {
        if (raw == null || raw.isBlank()) return false;
        String command = raw.charAt(0) == '/' ? raw.substring(1) : raw;
        String[] parts = command.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return false;
        return switch (parts[0].toLowerCase(Locale.ROOT)) {
            case "setflagmessagecolor" -> {
                if (parts.length < 3) yield false;
                AntiCheat.applyClickedColor(parts[1], parts[2]);
                yield true;
            }
            case "settheme" -> {
                if (parts.length < 3) yield false;
                RegisterCommand.applyClickedTheme(parts[1], parts[2]);
                yield true;
            }
            default -> false;
        };
    }
}
