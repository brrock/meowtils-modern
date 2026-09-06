package wtf.tatp.meowtils.util;

/** Strip legacy section codes so ViaVersion/1.8.9 text matches modern Component.getString(). */
public final class ColorUtil {
    private ColorUtil() {}
    public static String unformattedText(String text) {
        if (text == null) return "";
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) { i++; continue; }
            out.append(c);
        }
        return out.toString();
    }
    public static String plainLower(String text) { return unformattedText(text).toLowerCase(java.util.Locale.ROOT); }
    public static int rgb(int red, int green, int blue) {
        return 0xFF000000 | (red & 255) << 16 | (green & 255) << 8 | (blue & 255);
    }
    public static int rgba(int red, int green, int blue, int alpha) {
        return (alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | (blue & 255);
    }
    public static int convertOpacity(float opacity) {
        float scaled = opacity <= 100 ? opacity * 2.55f : opacity;
        return Math.max(0, Math.min(255, Math.round(scaled)));
    }
    /** Original 2.0.1 name used by Prefix, AntiCheat, and color pickers. */
    public static net.minecraft.ChatFormatting getColorFromString(String colorName) {
        return formattingFromName(colorName);
    }

    public static net.minecraft.ChatFormatting formattingFromName(String config) {
        if (config == null || config.isBlank()) return net.minecraft.ChatFormatting.WHITE;
        String key = unformattedText(config).replace(' ', '_').toUpperCase(java.util.Locale.ROOT);
        try { return net.minecraft.ChatFormatting.valueOf(key); } catch (IllegalArgumentException ignored) { return net.minecraft.ChatFormatting.WHITE; }
    }
    public static boolean isColor(net.minecraft.ChatFormatting formatting) {
        return switch (formatting) {
            case BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE -> true;
            default -> false;
        };
    }
    public static int rgbFromFormatting(net.minecraft.ChatFormatting formatting) {
        return 0xFF000000 | switch (formatting) {
            case BLACK -> 0x000000;
            case DARK_BLUE -> 0x0000AA;
            case DARK_GREEN -> 0x00AA00;
            case DARK_AQUA -> 0x00AAAA;
            case DARK_RED -> 0xAA0000;
            case DARK_PURPLE -> 0xAA00AA;
            case GOLD -> 0xFFAA00;
            case GRAY -> 0xAAAAAA;
            case DARK_GRAY -> 0x555555;
            case BLUE -> 0x5555FF;
            case GREEN -> 0x55FF55;
            case AQUA -> 0x55FFFF;
            case RED -> 0xFF5555;
            case LIGHT_PURPLE -> 0xFF55FF;
            case YELLOW -> 0xFFFF55;
            default -> 0xFFFFFF;
        };
    }
    public static String convertFormatting(String text) {
        if (text == null) return null;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&' && isFormatCode(chars[i + 1])) chars[i] = '§';
        }
        return new String(chars);
    }
    private static boolean isFormatCode(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')
                || c == 'k' || c == 'l' || c == 'm' || c == 'n' || c == 'o' || c == 'r'
                || c == 'K' || c == 'L' || c == 'M' || c == 'N' || c == 'O' || c == 'R';
    }
}
