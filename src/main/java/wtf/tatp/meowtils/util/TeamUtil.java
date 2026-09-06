package wtf.tatp.meowtils.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.scores.PlayerTeam;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.lists.FriendlistManager;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Server;

public final class TeamUtil {
    private static final Map<UUID, Long> TAB_JOIN_TIMES = new HashMap<>();
    private static final Set<UUID> CURRENT_TAB_LIST = new HashSet<>();
    private static final Set<UUID> CACHED_TEAMMATES = new HashSet<>();

    @EventTarget public void onTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST) return;
        var teams = Module.get(wtf.tatp.meowtils.module.meowtils.Teams.class);
        if (teams != null && "Universal".equals(Settings.text(teams, "ignoreBotMode", "Dynamic"))) updateTabList();
    }

    private static void updateTabList() {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;
        Set<UUID> next = new HashSet<>();
        for (PlayerInfo info : connection.getListedOnlinePlayers()) next.add(info.getProfile().id());
        long now = System.currentTimeMillis();
        for (UUID id : next) TAB_JOIN_TIMES.putIfAbsent(id, now);
        TAB_JOIN_TIMES.keySet().removeIf(id -> !next.contains(id));
        CURRENT_TAB_LIST.clear();
        CURRENT_TAB_LIST.addAll(next);
    }

    public static boolean isBot(Player player) {
        var teams = Module.get(wtf.tatp.meowtils.module.meowtils.Teams.class);
        if (player == null) return true;
        String mode = teams == null ? "Dynamic" : Settings.text(teams, "ignoreBotMode", "Dynamic");
        if ("None".equals(mode) || player == Minecraft.getInstance().player) return false;
        UUID id = player.getUUID();
        boolean useTab = "Universal".equals(mode) || ("Dynamic".equals(mode) && Server.HYPIXEL.isNotActive());
        boolean useUuid = "Hypixel".equals(mode) || ("Dynamic".equals(mode) && Server.HYPIXEL.isActive());
        if (useTab) {
            Long joined = TAB_JOIN_TIMES.get(id);
            if (!CURRENT_TAB_LIST.contains(id) || joined == null) return true;
            return System.currentTimeMillis() - joined < 10000;
        }
        if (useUuid) return id.version() != 1 && id.version() != 4;
        return true;
    }

    /**
     * Original 2.0.1 order: flying skip, cache, vanilla same-team, Bedwars leather color,
     * then the most frequent {@code §} color on the formatted name.
     */
    public static boolean isTeam(Player player) {
        var mc = Minecraft.getInstance();
        var teams = Module.get(wtf.tatp.meowtils.module.meowtils.Teams.class);
        if (mc.player == null || mc.level == null || player == null || player == mc.player) return false;
        if (teams != null && !Settings.bool(teams, "ignoreTeam", true)) return false;
        if (player.getAbilities().flying) return true;
        if (CACHED_TEAMMATES.contains(player.getUUID())) return true;
        if (player.isAlliedTo(mc.player) || sameScoreboardTeam(mc.player, player)) {
            CACHED_TEAMMATES.add(player.getUUID());
            return true;
        }
        if (Bedwars.ALL.isActive() && sameArmorColor(mc.player, player)) {
            CACHED_TEAMMATES.add(player.getUUID());
            return true;
        }
        String ours = TeamColors.mostFrequent(formattedName(mc.player));
        String theirs = TeamColors.mostFrequent(formattedName(player));
        if (ours != null && ours.equals(theirs)) {
            CACHED_TEAMMATES.add(player.getUUID());
            return true;
        }
        return false;
    }

    private static boolean sameScoreboardTeam(Player self, Player other) {
        var a = self.getTeam();
        var b = other.getTeam();
        return a != null && a == b;
    }

    private static String formattedName(Player player) {
        String name = player.getScoreboardName();
        if (player.getTeam() instanceof PlayerTeam team) {
            return team.getPlayerPrefix().getString() + name + team.getPlayerSuffix().getString();
        }
        return name;
    }

    static Integer armorColor(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty() || !chest.is(Items.LEATHER_CHESTPLATE)) return null;
        return DyedItemColor.getOrDefault(chest, DyedItemColor.LEATHER_COLOR);
    }

    static boolean sameArmorColor(Player a, Player b) {
        Integer left = armorColor(a);
        Integer right = armorColor(b);
        return left != null && left.equals(right);
    }

    public static boolean ignoreFriends(String uuidOrName) {
        var teams = Module.get(wtf.tatp.meowtils.module.meowtils.Teams.class);
        if (teams == null || !Settings.bool(teams, "ignoreFriends", false)) return false;
        return FriendlistManager.isFriendlisted(uuidOrName);
    }

    public static void reset() {
        CURRENT_TAB_LIST.clear();
        TAB_JOIN_TIMES.clear();
        CACHED_TEAMMATES.clear();
    }
}
