package wtf.tatp.meowtils.stats.util;

import net.minecraft.ChatFormatting;

public final class BedwarsStatsUtil {
    private BedwarsStatsUtil() {}

    public static ChatFormatting getFKDRColor(double fkdr) {
        return fkdr == 0.0d ? ChatFormatting.BLUE : fkdr < 1.0d ? ChatFormatting.GRAY : fkdr < 3.0d ? ChatFormatting.WHITE
                : fkdr < 5.0d ? ChatFormatting.GREEN : fkdr < 7.0d ? ChatFormatting.DARK_GREEN : fkdr < 10.0d ? ChatFormatting.YELLOW
                : fkdr < 20.0d ? ChatFormatting.GOLD : fkdr < 30.0d ? ChatFormatting.RED : fkdr < 50.0d ? ChatFormatting.DARK_RED
                : fkdr < 100.0d ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_PURPLE;
    }

    public static ChatFormatting getWLRColor(double wlr) {
        return wlr == 0.0d ? ChatFormatting.BLUE : wlr < 0.5d ? ChatFormatting.GRAY : wlr < 0.9d ? ChatFormatting.WHITE
                : wlr < 1.5d ? ChatFormatting.GREEN : wlr < 2.0d ? ChatFormatting.DARK_GREEN : wlr < 3.0d ? ChatFormatting.YELLOW
                : wlr < 6.0d ? ChatFormatting.GOLD : wlr < 9.0d ? ChatFormatting.RED : wlr < 15.0d ? ChatFormatting.DARK_RED
                : wlr < 30.0d ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_PURPLE;
    }

    public static ChatFormatting getWSColor(int ws) {
        return ws == 0 ? ChatFormatting.BLUE : ws < 3 ? ChatFormatting.GRAY : ws < 5 ? ChatFormatting.WHITE
                : ws < 15 ? ChatFormatting.GREEN : ws < 20 ? ChatFormatting.DARK_GREEN : ws < 30 ? ChatFormatting.YELLOW
                : ws < 40 ? ChatFormatting.GOLD : ws < 50 ? ChatFormatting.RED : ws < 80 ? ChatFormatting.DARK_RED
                : ChatFormatting.LIGHT_PURPLE;
    }

    public static ChatFormatting getFinalsColor(int finals) {
        return finals == 0 ? ChatFormatting.BLUE : finals < 1000 ? ChatFormatting.GRAY : finals < 2000 ? ChatFormatting.WHITE
                : finals < 3000 ? ChatFormatting.GREEN : finals < 5000 ? ChatFormatting.DARK_GREEN : finals < 15000 ? ChatFormatting.YELLOW
                : finals < 30000 ? ChatFormatting.GOLD : finals < 50000 ? ChatFormatting.RED : finals < 70000 ? ChatFormatting.DARK_RED
                : finals < 100000 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_PURPLE;
    }

    public static ChatFormatting getClutchRatioColor(double cr) {
        return cr == 0.0d ? ChatFormatting.BLUE : cr < 0.01d ? ChatFormatting.GRAY : cr < 0.05d ? ChatFormatting.WHITE
                : cr < 0.1d ? ChatFormatting.GREEN : cr < 0.2d ? ChatFormatting.DARK_GREEN : cr < 0.3d ? ChatFormatting.YELLOW
                : cr < 0.4d ? ChatFormatting.GOLD : cr < 0.5d ? ChatFormatting.RED : cr < 0.6d ? ChatFormatting.DARK_RED
                : cr < 0.7d ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_PURPLE;
    }

    public static String getFormattedLevel(int level) {
        LevelFormat format = formatFor(level);
        String digits = String.valueOf(level);
        StringBuilder out = new StringBuilder();
        out.append(format.left).append("[");
        for (int i = 0; i < digits.length(); i++) {
            ChatFormatting color = i < format.digits.length ? format.digits[i] : format.digits[format.digits.length - 1];
            out.append(color).append(digits.charAt(i));
        }
        if (format.star != null) out.append(format.star);
        if (format.bold) out.append(ChatFormatting.BOLD);
        out.append(format.icon).append(ChatFormatting.RESET).append(format.right).append("]");
        return out.toString() + ChatFormatting.RESET;
    }

    private record LevelFormat(ChatFormatting left, ChatFormatting[] digits, ChatFormatting star, String icon, ChatFormatting right, boolean bold) {}

    private static LevelFormat solid(ChatFormatting color, String icon) {
        return new LevelFormat(color, new ChatFormatting[]{color}, color, icon, color, false);
    }

    private static LevelFormat formatFor(int level) {
        if (level < 100) return solid(ChatFormatting.GRAY, "✫");
        if (level < 200) return solid(ChatFormatting.WHITE, "✫");
        if (level < 300) return solid(ChatFormatting.GOLD, "✫");
        if (level < 400) return solid(ChatFormatting.AQUA, "✫");
        if (level < 500) return solid(ChatFormatting.DARK_GREEN, "✫");
        if (level < 600) return solid(ChatFormatting.DARK_AQUA, "✫");
        if (level < 700) return solid(ChatFormatting.DARK_RED, "✫");
        if (level < 800) return solid(ChatFormatting.LIGHT_PURPLE, "✫");
        if (level < 900) return solid(ChatFormatting.BLUE, "✫");
        if (level < 1000) return solid(ChatFormatting.DARK_PURPLE, "✫");
        if (level < 1100) return new LevelFormat(ChatFormatting.RED, new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.AQUA}, ChatFormatting.LIGHT_PURPLE, "✫", ChatFormatting.DARK_PURPLE, false);
        if (level < 1200) return new LevelFormat(ChatFormatting.GRAY, new ChatFormatting[]{ChatFormatting.WHITE}, ChatFormatting.GRAY, "✪", ChatFormatting.GRAY, false);
        if (level < 1300) return new LevelFormat(ChatFormatting.GRAY, new ChatFormatting[]{ChatFormatting.YELLOW}, ChatFormatting.GOLD, "✪", ChatFormatting.GRAY, false);
        if (level < 1400) return new LevelFormat(ChatFormatting.GRAY, new ChatFormatting[]{ChatFormatting.AQUA}, ChatFormatting.DARK_AQUA, "✪", ChatFormatting.GRAY, false);
        if (level < 1500) return new LevelFormat(ChatFormatting.GRAY, new ChatFormatting[]{ChatFormatting.GREEN}, ChatFormatting.DARK_GREEN, "✪", ChatFormatting.GRAY, false);
        if (level < 1600) return new LevelFormat(ChatFormatting.GRAY, new ChatFormatting[]{ChatFormatting.DARK_AQUA}, ChatFormatting.BLUE, "✪", ChatFormatting.GRAY, false);
        if (level < 1700) return new LevelFormat(ChatFormatting.GRAY, new ChatFormatting[]{ChatFormatting.RED}, ChatFormatting.DARK_RED, "✪", ChatFormatting.GRAY, false);
        if (level < 1800) return new LevelFormat(ChatFormatting.GRAY, new ChatFormatting[]{ChatFormatting.LIGHT_PURPLE}, ChatFormatting.DARK_PURPLE, "✪", ChatFormatting.GRAY, false);
        if (level < 1900) return new LevelFormat(ChatFormatting.GRAY, new ChatFormatting[]{ChatFormatting.BLUE}, ChatFormatting.DARK_BLUE, "✪", ChatFormatting.GRAY, false);
        if (level < 2000) return new LevelFormat(ChatFormatting.GRAY, new ChatFormatting[]{ChatFormatting.DARK_PURPLE}, ChatFormatting.DARK_GRAY, "✪", ChatFormatting.GRAY, false);
        if (level < 2100) return new LevelFormat(ChatFormatting.DARK_GRAY, new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.WHITE, ChatFormatting.WHITE, ChatFormatting.GRAY}, ChatFormatting.GRAY, "✪", ChatFormatting.DARK_GRAY, false);
        if (level < 2200) return new LevelFormat(ChatFormatting.WHITE, new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.YELLOW, ChatFormatting.YELLOW, ChatFormatting.GOLD}, ChatFormatting.GOLD, "⚝", ChatFormatting.GOLD, true);
        if (level < 2300) return new LevelFormat(ChatFormatting.GOLD, new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.WHITE, ChatFormatting.WHITE, ChatFormatting.AQUA}, ChatFormatting.DARK_AQUA, "⚝", ChatFormatting.DARK_AQUA, true);
        if (level < 2400) return new LevelFormat(ChatFormatting.DARK_PURPLE, new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE, ChatFormatting.LIGHT_PURPLE, ChatFormatting.GOLD}, ChatFormatting.YELLOW, "⚝", ChatFormatting.YELLOW, true);
        if (level < 2500) return new LevelFormat(ChatFormatting.AQUA, new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.WHITE, ChatFormatting.WHITE, ChatFormatting.GRAY}, ChatFormatting.GRAY, "⚝", ChatFormatting.DARK_GRAY, true);
        if (level < 2600) return new LevelFormat(ChatFormatting.WHITE, new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.GREEN, ChatFormatting.GREEN, ChatFormatting.DARK_GREEN}, ChatFormatting.DARK_GREEN, "⚝", ChatFormatting.DARK_GREEN, true);
        if (level < 2700) return new LevelFormat(ChatFormatting.DARK_RED, new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.RED, ChatFormatting.RED, ChatFormatting.LIGHT_PURPLE}, ChatFormatting.LIGHT_PURPLE, "⚝", ChatFormatting.DARK_PURPLE, true);
        if (level < 2800) return new LevelFormat(ChatFormatting.YELLOW, new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.WHITE, ChatFormatting.WHITE, ChatFormatting.DARK_GRAY}, ChatFormatting.DARK_GRAY, "⚝", ChatFormatting.DARK_GRAY, true);
        if (level < 2900) return new LevelFormat(ChatFormatting.GREEN, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.DARK_GREEN, ChatFormatting.DARK_GREEN, ChatFormatting.GOLD}, ChatFormatting.GOLD, "⚝", ChatFormatting.YELLOW, true);
        if (level < 3000) return new LevelFormat(ChatFormatting.AQUA, new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.DARK_AQUA, ChatFormatting.DARK_AQUA, ChatFormatting.BLUE}, ChatFormatting.BLUE, "⚝", ChatFormatting.DARK_BLUE, true);
        if (level < 3100) return new LevelFormat(ChatFormatting.YELLOW, new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.GOLD, ChatFormatting.GOLD, ChatFormatting.RED}, ChatFormatting.RED, "⚝", ChatFormatting.DARK_RED, true);
        if (level < 3200) return new LevelFormat(ChatFormatting.BLUE, new ChatFormatting[]{ChatFormatting.BLUE, ChatFormatting.DARK_AQUA, ChatFormatting.DARK_AQUA, ChatFormatting.GOLD}, ChatFormatting.GOLD, "✥", ChatFormatting.YELLOW, false);
        if (level < 3300) return new LevelFormat(ChatFormatting.RED, new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.DARK_RED}, ChatFormatting.RED, "✥", ChatFormatting.RED, false);
        if (level < 3400) return new LevelFormat(ChatFormatting.BLUE, new ChatFormatting[]{ChatFormatting.BLUE, ChatFormatting.BLUE, ChatFormatting.LIGHT_PURPLE, ChatFormatting.RED}, ChatFormatting.RED, "✥", ChatFormatting.DARK_RED, false);
        if (level < 3500) return new LevelFormat(ChatFormatting.DARK_GREEN, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.LIGHT_PURPLE, ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_PURPLE}, ChatFormatting.DARK_PURPLE, "✥", ChatFormatting.DARK_GREEN, false);
        if (level < 3600) return new LevelFormat(ChatFormatting.RED, new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.DARK_RED, ChatFormatting.DARK_RED, ChatFormatting.DARK_GREEN}, ChatFormatting.GREEN, "✥", ChatFormatting.GREEN, false);
        if (level < 3700) return new LevelFormat(ChatFormatting.GREEN, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.GREEN, ChatFormatting.AQUA, ChatFormatting.BLUE}, ChatFormatting.BLUE, "✥", ChatFormatting.DARK_BLUE, false);
        if (level < 3800) return new LevelFormat(ChatFormatting.DARK_RED, new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.RED, ChatFormatting.RED, ChatFormatting.AQUA}, ChatFormatting.DARK_AQUA, "✥", ChatFormatting.DARK_AQUA, false);
        if (level < 3900) return new LevelFormat(ChatFormatting.DARK_BLUE, new ChatFormatting[]{ChatFormatting.DARK_BLUE, ChatFormatting.BLUE, ChatFormatting.DARK_PURPLE, ChatFormatting.DARK_PURPLE}, ChatFormatting.LIGHT_PURPLE, "✥", ChatFormatting.DARK_BLUE, false);
        if (level < 4000) return new LevelFormat(ChatFormatting.RED, new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.GREEN, ChatFormatting.GREEN, ChatFormatting.DARK_AQUA}, ChatFormatting.BLUE, "✥", ChatFormatting.BLUE, false);
        if (level < 4100) return new LevelFormat(ChatFormatting.DARK_PURPLE, new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.RED, ChatFormatting.RED, ChatFormatting.GOLD}, ChatFormatting.GOLD, "✥", ChatFormatting.YELLOW, false);
        if (level < 4200) return new LevelFormat(ChatFormatting.YELLOW, new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.GOLD, ChatFormatting.RED, ChatFormatting.LIGHT_PURPLE}, ChatFormatting.LIGHT_PURPLE, "✥", ChatFormatting.DARK_PURPLE, false);
        if (level < 4300) return new LevelFormat(ChatFormatting.DARK_BLUE, new ChatFormatting[]{ChatFormatting.BLUE, ChatFormatting.DARK_AQUA, ChatFormatting.AQUA, ChatFormatting.WHITE}, ChatFormatting.GRAY, "✥", ChatFormatting.GRAY, false);
        if (level < 4400) return new LevelFormat(ChatFormatting.BLACK, new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.DARK_GRAY, ChatFormatting.DARK_GRAY, ChatFormatting.DARK_PURPLE}, ChatFormatting.DARK_PURPLE, "✥", ChatFormatting.BLACK, false);
        if (level < 4500) return new LevelFormat(ChatFormatting.DARK_GREEN, new ChatFormatting[]{ChatFormatting.DARK_GREEN, ChatFormatting.GREEN, ChatFormatting.YELLOW, ChatFormatting.GOLD}, ChatFormatting.DARK_PURPLE, "✥", ChatFormatting.LIGHT_PURPLE, false);
        if (level < 4600) return new LevelFormat(ChatFormatting.WHITE, new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.AQUA, ChatFormatting.AQUA, ChatFormatting.DARK_AQUA}, ChatFormatting.DARK_AQUA, "✥", ChatFormatting.DARK_AQUA, false);
        if (level < 4700) return new LevelFormat(ChatFormatting.DARK_AQUA, new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.YELLOW, ChatFormatting.YELLOW, ChatFormatting.GOLD}, ChatFormatting.LIGHT_PURPLE, "✥", ChatFormatting.DARK_PURPLE, false);
        if (level < 4800) return new LevelFormat(ChatFormatting.WHITE, new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.RED, ChatFormatting.RED, ChatFormatting.BLUE}, ChatFormatting.DARK_BLUE, "✥", ChatFormatting.BLUE, false);
        if (level < 4900) return new LevelFormat(ChatFormatting.DARK_PURPLE, new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW}, ChatFormatting.AQUA, "✥", ChatFormatting.DARK_AQUA, false);
        if (level < 5000) return new LevelFormat(ChatFormatting.DARK_GREEN, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.WHITE, ChatFormatting.WHITE, ChatFormatting.GREEN}, ChatFormatting.GREEN, "✥", ChatFormatting.DARK_GREEN, false);
        return new LevelFormat(ChatFormatting.DARK_RED, new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.DARK_PURPLE, ChatFormatting.BLUE, ChatFormatting.BLUE}, ChatFormatting.DARK_BLUE, "✥", ChatFormatting.BLACK, false);
    }
}
