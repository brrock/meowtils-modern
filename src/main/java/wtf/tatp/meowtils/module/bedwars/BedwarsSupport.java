package wtf.tatp.meowtils.module.bedwars;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.TeamColor;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.gui.GuiUtil;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.lists.SafelistManager;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.SessionManager;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.module.meowtils.Teams;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;

/** Shared Bedwars-port helpers. Session gates match 2.0.1 GAME / Duels.BEDWARS checks. */
public final class BedwarsSupport {
    private BedwarsSupport() {}

    public static boolean inMatch() {
        return SessionManager.bedwarsGame || SessionManager.duelsBedwars;
    }

    public static boolean inMatchLive() {
        return inMatch() && !SessionManager.bedwarsGamePre;
    }

    public static boolean inAnyBedwars() {
        return SessionManager.bedwars || SessionManager.duelsBedwars;
    }

    public static boolean inEditor() {
        return GuiUtil.inEditor();
    }

    public static boolean skipPlayer(Player player, boolean skipBots) {
        Minecraft mc = Minecraft.getInstance();
        if (player == null || mc.player == null || player == mc.player) return true;
        if (skipBots && TeamUtil.isBot(player)) return true;
        if (TeamUtil.isTeam(player)) return true;
        return ignoreFriend(player);
    }

    public static boolean ignoreFriend(Player player) {
        Teams teams = Module.get(Teams.class);
        if (teams == null || !Settings.bool(teams, "ignoreFriends", true)) return false;
        return SafelistManager.isSafelisted(player.getUUID().toString())
                || SafelistManager.isSafelisted(player.getGameProfile().name());
    }

    public static String displayName(Player player) {
        return player == null ? "" : displayName(player.getGameProfile().name());
    }

    public static String displayName(String name) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || name == null) return name == null ? "" : name;
        PlayerTeam team = mc.level.getScoreboard().getPlayersTeam(name);
        if (team == null) return name;
        return team.getPlayerPrefix().getString() + name + team.getPlayerSuffix().getString();
    }

    public static TeamColor teamColor(String playerName) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || playerName == null) return null;
        PlayerTeam team = mc.level.getScoreboard().getPlayersTeam(playerName);
        if (team != null && team.getColor().isPresent()) return team.getColor().get();
        String formatted = displayName(playerName);
        for (int i = 0; i < formatted.length() - 1; i++) {
            if (formatted.charAt(i) == '§') {
                ChatFormatting formatting = ChatFormatting.getByCode(formatted.charAt(i + 1));
                if (formatting != null) {
                    return switch (formatting) {
                        case RED -> TeamColor.RED;
                        case BLUE -> TeamColor.BLUE;
                        case GREEN -> TeamColor.GREEN;
                        case YELLOW -> TeamColor.YELLOW;
                        case AQUA -> TeamColor.AQUA;
                        case WHITE -> TeamColor.WHITE;
                        case LIGHT_PURPLE -> TeamColor.LIGHT_PURPLE;
                        case DARK_GRAY -> TeamColor.DARK_GRAY;
                        default -> null;
                    };
                }
            }
        }
        return null;
    }

    public static String formattedTeamName(TeamColor color) {
        if (color == null) return null;
        return switch (color) {
            case RED -> "§c§lRed Team";
            case BLUE -> "§9§lBlue Team";
            case GREEN -> "§a§lGreen Team";
            case YELLOW -> "§e§lYellow Team";
            case AQUA -> "§b§lAqua Team";
            case WHITE -> "§f§lWhite Team";
            case LIGHT_PURPLE -> "§d§lPink Team";
            case DARK_GRAY -> "§8§lGray Team";
            default -> null;
        };
    }

    public static void play(Sound kind) {
        if (kind == null) return;
        wtf.tatp.meowtils.manager.SoundLoader.Sound mapped = switch (kind) {
            case PING -> wtf.tatp.meowtils.manager.SoundLoader.Sound.PING;
            case PING_MEDIUM -> wtf.tatp.meowtils.manager.SoundLoader.Sound.PING_MEDIUM;
            case MEOW -> wtf.tatp.meowtils.manager.SoundLoader.Sound.MEOW;
            case LEVEL -> wtf.tatp.meowtils.manager.SoundLoader.Sound.LEVEL;
            case ERROR -> wtf.tatp.meowtils.manager.SoundLoader.Sound.ERROR;
            case ANVIL -> wtf.tatp.meowtils.manager.SoundLoader.Sound.ANVIL;
        };
        wtf.tatp.meowtils.manager.SoundLoader.play(mapped, 100);
    }

    public static void alert(Module module, String chat, String title, String notification, String alertType) {
        if (!"Notification".equals(alertType)) Meowtils.addMessage(chat);
        if (!"Chat".equals(alertType)) NotificationManager.show(title, notification, NotificationManager.Type.ALERT, 1500L);
    }

    public static void notifyMode(String chatTitle, String chat, String notifyTitle, String notify, NotificationManager.Type type, long time) {
        if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) Meowtils.addMessage(chat);
        if (Notifications.getMode() != Notifications.Mode.CHAT) NotificationManager.show(notifyTitle, notify, type, time);
    }

    public static String formatTime(int seconds) {
        return (seconds / 60) + "m " + (seconds % 60) + "s";
    }

    public static int rgb(int red, int green, int blue) {
        return 0xFF000000 | (red & 255) << 16 | (green & 255) << 8 | (blue & 255);
    }

    public static int rgba(int red, int green, int blue, int alpha) {
        return (alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | (blue & 255);
    }

    public static int opacityAlpha(double opacity) {
        if (opacity <= 100) return (int) Math.round(Math.max(0, Math.min(255, opacity * 2.55)));
        return (int) Math.round(Math.max(0, Math.min(255, opacity)));
    }

    public static void drawHud(GuiGraphicsExtractor graphics, String text, int x, int y, float scale, int color) {
        wtf.tatp.meowtils.font.HudFont.draw(graphics, text, x, y, scale, color);
    }

    public static void drawHud(GuiGraphicsExtractor graphics, String text, int x, int y, float scale) {
        drawHud(graphics, text, x, y, scale, 0xFFFFFFFF);
    }

    public static Component legacy(String text) {
        MutableComponent root = Component.empty();
        Style style = Style.EMPTY;
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                flush(root, buffer, style);
                ChatFormatting formatting = ChatFormatting.getByCode(Character.toLowerCase(text.charAt(++i)));
                if (formatting == ChatFormatting.RESET) style = Style.EMPTY;
                else if (formatting != null && isFormat(formatting)) style = style.applyFormat(formatting);
                else if (formatting != null) style = Style.EMPTY.applyLegacyFormat(formatting);
            } else buffer.append(c);
        }
        flush(root, buffer, style);
        return root;
    }

    private static boolean isFormat(ChatFormatting formatting) {
        return formatting == ChatFormatting.BOLD || formatting == ChatFormatting.ITALIC || formatting == ChatFormatting.UNDERLINE
                || formatting == ChatFormatting.STRIKETHROUGH || formatting == ChatFormatting.OBFUSCATED;
    }

    private static void flush(MutableComponent root, StringBuilder buffer, Style style) {
        if (buffer.isEmpty()) return;
        root.append(Component.literal(buffer.toString()).withStyle(style));
        buffer.setLength(0);
    }

    public static String itemLabel(ItemStack stack) {
        return stack == null || stack.isEmpty() ? "" : ColorUtil.plainLower(stack.getHoverName().getString());
    }

    public static List<String> loreLines(ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        List<String> lines = new ArrayList<>();
        if (stack == null || stack.isEmpty() || mc.player == null) return lines;
        for (Component line : stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.Default.NORMAL)) {
            lines.add(ColorUtil.unformattedText(line.getString()));
        }
        return lines;
    }

    public static boolean isResource(ItemStack stack, String kind) {
        return switch (kind) {
            case "iron" -> ItemIds.is(stack, "iron_ingot");
            case "gold" -> ItemIds.is(stack, "gold_ingot");
            case "diamond" -> ItemIds.is(stack, "diamond") && !ItemIds.is(stack, "diamond_block", "diamond_sword", "diamond_pickaxe", "diamond_axe", "diamond_hoe", "diamond_shovel", "diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots");
            case "emerald" -> ItemIds.is(stack, "emerald") && !ItemIds.is(stack, "emerald_block", "emerald_ore");
            default -> false;
        };
    }

    public static String resourceKey(ItemStack stack) {
        if (isResource(stack, "iron")) return "iron";
        if (isResource(stack, "gold")) return "gold";
        if (isResource(stack, "diamond")) return "diamond";
        if (isResource(stack, "emerald")) return "emerald";
        return "";
    }

    public static ItemStack resourceIcon(String kind) {
        return switch (kind) {
            case "iron" -> new ItemStack(net.minecraft.world.item.Items.IRON_INGOT);
            case "gold" -> new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT);
            case "diamond" -> new ItemStack(net.minecraft.world.item.Items.DIAMOND);
            case "emerald" -> new ItemStack(net.minecraft.world.item.Items.EMERALD);
            default -> ItemStack.EMPTY;
        };
    }

    public static String resourceName(String kind) {
        return switch (kind) {
            case "iron" -> "§fIron";
            case "gold" -> "§6Gold";
            case "diamond" -> "§bDiamond";
            case "emerald" -> "§2Emerald";
            default -> "Unknown";
        };
    }

    public static String resourceColor(String kind) {
        return switch (kind) {
            case "iron" -> "§f";
            case "gold" -> "§6";
            case "diamond" -> "§b";
            case "emerald" -> "§2";
            default -> "§f";
        };
    }

    public static boolean shopScreen(String title) {
        String plain = ColorUtil.plainLower(title);
        for (String name : SHOP_TITLES) if (plain.equals(name) || plain.contains(name)) return true;
        return false;
    }

    public static boolean upgradeShop(String title) {
        return ColorUtil.plainLower(title).contains("upgrades") && ColorUtil.plainLower(title).contains("trap");
    }

    public static String screenTitle() {
        Minecraft mc = Minecraft.getInstance();
        return mc.gui.screen() == null ? "" : mc.gui.screen().getTitle().getString();
    }

    public static boolean isBed(net.minecraft.world.level.block.state.BlockState state) {
        return state != null && (state.getBlock() instanceof net.minecraft.world.level.block.BedBlock || ItemIds.isBlock(state, "bed"));
    }

    public static boolean isObsidian(net.minecraft.world.level.block.state.BlockState state) {
        return ItemIds.isBlock(state, "obsidian") && !ItemIds.isBlock(state, "crying");
    }

    public static boolean isWool(net.minecraft.world.level.block.state.BlockState state) {
        return ItemIds.isBlock(state, "wool") && !ItemIds.isBlock(state, "carpet");
    }

    public static final String[] SHOP_TITLES = {
            "quick buy", "blocks", "melee", "armor", "tools", "ranged", "potions", "utility", "rotating items", "upgrades & traps"
    };

    public enum Sound { PING, PING_MEDIUM, MEOW, LEVEL, ERROR, ANVIL }
}
