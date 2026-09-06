package wtf.tatp.meowtils.util;

import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.meowtils.Settings;

/** Chat prefix from the original Settings module. */
public final class Prefix {
    private static Supplier<String> prefixSupplier = Prefix::meowtilsPrefix;

    private Prefix() {}

    public static String getPrefix() {
        return prefixSupplier.get();
    }

    public static void setPrefix(String prefix) {
        if (prefix == null) return;
        prefixSupplier = () -> prefix;
    }

    public static void resetPrefix() {
        prefixSupplier = Prefix::meowtilsPrefix;
    }

    private static String meowtilsPrefix() {
        Settings settings = Module.get(Settings.class);
        boolean lower = settings != null && wtf.tatp.meowtils.util.Settings.bool(settings, "lowerCase", false);
        String letter = lower ? "m" : "M";
        String mode = settings == null ? "Default" : wtf.tatp.meowtils.util.Settings.text(settings, "prefix", "Default");
        return switch (mode) {
            case "Myau" -> ChatFormatting.GRAY + "[" + ChatFormatting.RED + letter + ChatFormatting.GOLD + "e" + ChatFormatting.YELLOW + "o" + ChatFormatting.GREEN + "w" + ChatFormatting.GRAY + "] " + ChatFormatting.WHITE;
            case "Fire" -> ChatFormatting.GRAY + "[" + ChatFormatting.YELLOW + letter + ChatFormatting.GOLD + "e" + ChatFormatting.RED + "o" + ChatFormatting.DARK_RED + "w" + ChatFormatting.GRAY + "] " + ChatFormatting.WHITE;
            case "Nebula" -> ChatFormatting.GRAY + "[" + ChatFormatting.DARK_RED + letter + ChatFormatting.RED + "e" + ChatFormatting.LIGHT_PURPLE + "o" + ChatFormatting.DARK_PURPLE + "w" + ChatFormatting.GRAY + "] " + ChatFormatting.WHITE;
            case "Air" -> ChatFormatting.GRAY + "[" + ChatFormatting.AQUA + letter + ChatFormatting.WHITE + "e" + ChatFormatting.GRAY + "o" + ChatFormatting.DARK_GRAY + "w" + ChatFormatting.GRAY + "] " + ChatFormatting.WHITE;
            case "Custom" -> color(settings, "themeFirstBracket", "GRAY") + "[" + color(settings, "themeM", "BLUE") + letter + color(settings, "themeE", "DARK_AQUA") + "e" + color(settings, "themeO", "AQUA") + "o" + color(settings, "themeW", "WHITE") + "w" + color(settings, "themeSecondBracket", "GRAY") + "] " + ChatFormatting.WHITE;
            case "Short" -> color(settings, "themeFirstBracket", "GRAY") + "[" + color(settings, "themeM", "BLUE") + ChatFormatting.BOLD + letter + color(settings, "themeSecondBracket", "GRAY") + "] " + ChatFormatting.WHITE;
            default -> ChatFormatting.GRAY + "[" + ChatFormatting.BLUE + letter + ChatFormatting.DARK_AQUA + "e" + ChatFormatting.AQUA + "o" + ChatFormatting.WHITE + "w" + ChatFormatting.GRAY + "] " + ChatFormatting.WHITE;
        };
    }

    private static String color(Settings settings, String key, String fallback) {
        if (settings != null) {
            String field = switch (key) {
                case "themeM" -> settings.themeM;
                case "themeE" -> settings.themeE;
                case "themeO" -> settings.themeO;
                case "themeW" -> settings.themeW;
                case "themeFirstBracket" -> settings.themeFirstBracket;
                case "themeSecondBracket" -> settings.themeSecondBracket;
                default -> null;
            };
            if (field != null && !field.isBlank()) return ColorUtil.formattingFromName(field).toString();
        }
        return ColorUtil.formattingFromName(settings == null ? fallback : wtf.tatp.meowtils.util.Settings.text(settings, key, fallback)).toString();
    }
}
