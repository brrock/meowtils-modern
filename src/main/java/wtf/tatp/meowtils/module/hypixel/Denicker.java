package wtf.tatp.meowtils.module.hypixel;

import com.mojang.authlib.GameProfile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.MegaWalls;
import wtf.tatp.meowtils.manager.session.MurderMystery;
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.PlayerUtil;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;

public final class Denicker extends Module {
    private static final Set<String> PARSED = new HashSet<>();
    static final HashSet<String> nicks = loadNicks();

    public Denicker() {
        super("Denicker", Category.Hypixel);
        tag(ModuleTag.LEGIT);
        tooltip("Tells you the name of a nicked player if they are using their real skin.");
    }

    private static HashSet<String> loadNicks() {
        HashSet<String> set = new HashSet<>();
        try (var input = Denicker.class.getResourceAsStream("/meowtils-denicker-nicks.txt")) {
            if (input == null) return set;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) set.add(line.trim());
                }
            }
        } catch (Exception ignored) {}
        return set;
    }

    public static String getIcon() {
        Denicker d = get(Denicker.class);
        String mode = d == null ? "Normal" : Settings.text(d, "iconMode", "Normal");
        return switch (mode) {
            case "Normal" -> getColor().toString() + ChatFormatting.BOLD + " ✧";
            case "Asterisk" -> getColor().toString() + ChatFormatting.BOLD + " *";
            case "Text" -> getColor() + " [NICKED]";
            default -> "";
        };
    }

    private static ChatFormatting getColor() {
        Denicker d = get(Denicker.class);
        String mode = d == null ? "§5Dark Purple" : Settings.text(d, "iconColor", "§5Dark Purple");
        if (mode.contains("Dark Purple")) return ChatFormatting.DARK_PURPLE;
        if (mode.contains("Dark Red")) return ChatFormatting.DARK_RED;
        if (mode.contains("Dark Blue")) return ChatFormatting.DARK_BLUE;
        if (mode.contains("Dark Aqua")) return ChatFormatting.DARK_AQUA;
        return ChatFormatting.WHITE;
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (mc.player == null || event.getPhase() != ClientTickEvent.Phase.POST
                || MegaWalls.GAME.isActive() || MurderMystery.ALL.isActive()) {
            return;
        }
        var connection = mc.getConnection();
        if (connection == null) return;
        boolean ignoreTeam = Settings.bool(this, "ignoreTeam", false);
        for (PlayerInfo info : connection.getOnlinePlayers()) {
            GameProfile profile = info.getProfile();
            String name = profile.name();
            if (PARSED.contains(name)) continue;
            if (!PlayerUtil.isNicked(profile)) continue;
            String displayName = info.getTabListDisplayName() != null ? info.getTabListDisplayName().getString() : name;
            Player player = mc.level == null ? null : mc.level.getPlayerByUUID(profile.id());
            if (ignoreTeam && player != null && TeamUtil.isTeam(player)) continue;
            DenickerSkin.Data skinData = DenickerSkin.fromPlayerInfo(info);
            if (skinData == null) continue;
            if (!shouldMarkParsed(skinData)) continue;
            PARSED.add(name);
            alert(skinData.profileName(), ColorUtil.unformattedText(displayName), skinData.hash());
        }
    }

    private static boolean shouldMarkParsed(DenickerSkin.Data skinData) {
        if (nicks.contains(skinData.hash())) return true;
        return skinData.profileName() != null && !skinData.profileName().isEmpty();
    }

    private static void alert(String name, String displayName, String hash) {
        Denicker d = get(Denicker.class);
        if (d == null) return;
        String localPlayer = Minecraft.getInstance().player == null ? "" : Minecraft.getInstance().player.getGameProfile().name();
        if (nicks.contains(hash)) {
            if (Settings.bool(d, "message", true)) {
                Meowtils.addMessage(ChatFormatting.RED + displayName + ChatFormatting.DARK_PURPLE + " is nicked.");
                PartyNotifier.denicker(displayName, "", false);
            }
            return;
        }
        if (!Settings.bool(d, "message", true) || name.isEmpty() || name.equals(localPlayer)) return;
        Meowtils.addMessage(ChatFormatting.GOLD + name + ChatFormatting.DARK_PURPLE + " is nicked as "
                + ChatFormatting.RED + displayName + ChatFormatting.DARK_PURPLE + ".");
        Stats stats = get(Stats.class);
        if (stats != null && stats.getState() && Settings.bool(stats, "autoCheck", false)) {
            if (Bedwars.GAME.isActive()) {
                stats.request(name);
            } else if (Skywars.GAME.isActive()) {
                stats.request(name);
            }
        }
        PartyNotifier.denicker(displayName, name, true);
    }

    @Override
    public void onReset() { PARSED.clear(); }
}
