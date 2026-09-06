package wtf.tatp.meowtils.module.bedwars;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.TeamColor;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;

/** Alerts when an enemy team first shows Sharpness or Protection. */
public final class UpgradeAlerts extends Module {
    private static final Map<String, Set<String>> TEAM_UPGRADES = new HashMap<>();
    private static int tickCounter;

    public UpgradeAlerts() {
        super("UpgradeAlerts", Category.Bedwars);
        tag(ModuleTag.SAFE);
        tooltip("Alerts when a team buys a team upgrade.");
        addMode(new ModeValue("Alert", List.of("Chat", "Notification", "All"), "alertType", this));
        addToggle(new ToggleValue("Ping sound", "sound", this));
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        if (!BedwarsSupport.inMatch()) return;
        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;
        for (Player player : mc.level.players()) checkForEnchantments(player);
    }

    private void checkForEnchantments(Player player) {
        if (player == null || BedwarsSupport.skipPlayer(player, false) || skipFriend(player)) return;
        ItemStack held = player.getMainHandItem();
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!held.isEmpty() && ItemIds.isSword(held) && held.isEnchanted()) notifyUpgrade(player, "Sharpened Swords");
        if (!chestplate.isEmpty() && ItemIds.is(chestplate, "chestplate") && chestplate.isEnchanted()) notifyUpgrade(player, "Reinforced Armor");
    }

    private static boolean skipFriend(Player player) {
        return TeamUtil.ignoreFriends(player.getUUID().toString()) || TeamUtil.ignoreFriends(player.getGameProfile().name());
    }

    private void notifyUpgrade(Player player, String upgrade) {
        String name = player.getGameProfile().name();
        if ("NONE".equals(BedwarsSupport.displayName(name))) return;
        TeamColor color = BedwarsSupport.teamColor(name);
        String teamName = BedwarsSupport.formattedTeamName(color);
        if (teamName == null) return;
        Set<String> teamUpgrades = TEAM_UPGRADES.computeIfAbsent(teamName, ignored -> new HashSet<>());
        if (teamUpgrades.contains(upgrade)) return;
        String text = teamName + "§7 purchased §3" + upgrade;
        BedwarsSupport.alert(this, text, "UpgradeAlerts", text, Settings.text(this, "alertType", "Chat"));
        teamUpgrades.add(upgrade);
        if (Settings.bool(this, "sound", true)) BedwarsSupport.play(BedwarsSupport.Sound.PING);
    }

    @Override
    public void onReset() {
        TEAM_UPGRADES.clear();
    }
}
