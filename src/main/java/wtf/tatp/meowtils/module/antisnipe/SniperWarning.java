package wtf.tatp.meowtils.module.antisnipe;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.ReceivePacketEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.extension.HttpJson;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.module.hypixel.Stats;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.NameUtil;
import wtf.tatp.meowtils.util.PlayerUtil;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;
import wtf.tatp.meowtils.util.Util;

public final class SniperWarning extends Module {
    @Config public boolean enabled;
    @Config public int key;
    @Config public boolean sound = true;
    @Config public boolean checkGear = true;
    @Config public boolean checkName = true;
    @Config public boolean checkStats;
    @Config public boolean fetchStats;
    private static final Map<String, Boolean> SNIPER_ALERTED = new HashMap<>();
    private static int tickCounter;
    private static final String[] SNIPER_NAMES = {"mcalt_", "mcalts_", "hassalt_", "dogalt_", "mal_", "bym_", "jy6_", "lf_", "wg_", "ggnekito", "dahai_", "tzi", "nicegen", "opalalts", "msmc", "myau", "vape", "snipe", "nicealts", "rave", "alt", "client", "hack", "hax", "fernan", "watchdog", "anticheat"};

    public SniperWarning() {
        super("SniperWarning", Category.Antisnipe);
        tag(ModuleTag.LEGIT);
        tooltip("Warns you of certain players that may be snipers.");
        addToggle(new ToggleValue("Fetch stats", "fetchStats", this));
        addToggle(new ToggleValue("Ping sound", "sound", this));
        addCheck(new CheckValue("Check gear", "checkGear", this));
        addCheck(new CheckValue("Check name", "checkName", this));
        addCheck(new CheckValue("Check stats", "checkStats", this));
    }

    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null || event.getPhase() != ClientTickEvent.Phase.POST) return;
        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;
        if (Settings.bool(this, "checkGear", checkGear) && Bedwars.GAME.isActive()) {
            for (Player player : mc.level.players()) checkGear(player);
        }
        if (Settings.bool(this, "checkName", checkName)) {
            for (Player player : mc.level.players()) checkName(player);
        }
    }

    @EventTarget
    public void onPacket(ReceivePacketEvent event) {
        if (!(event.getPacket() instanceof ClientboundAddEntityPacket packet) || packet.getType() != EntityTypes.PLAYER) return;
        Stats statsModule = get(Stats.class);
        if (statsModule == null || !statsModule.getState() || Bedwars.GAME.isNotActive() || !Settings.bool(this, "checkStats", checkStats)) return;
        if (mc.player != null && packet.getUUID().equals(mc.player.getUUID())) return;
        java.util.UUID uuid = packet.getUUID();
        new wtf.tatp.meowtils.util.DelayedTask(() -> {
            Player player = mc.level == null ? null : mc.level.getPlayerByUUID(uuid);
            String playerName = player == null ? null : player.getScoreboardName();
            if (playerName == null || SNIPER_ALERTED.containsKey(playerName)) return;
            if (PlayerUtil.isNicked(PlayerUtil.getProfile(playerName)) || TeamUtil.isTeam(player)
                    || TeamUtil.ignoreFriends(player.getUUID().toString()) || TeamUtil.ignoreFriends(playerName)) {
                SNIPER_ALERTED.put(playerName, true);
                return;
            }
            fetchBedwars(playerName, (fkdr, finals, clutch) -> {
                if (clutch >= 0.5) alert(playerName, "Clutch Ratio", Settings.bool(this, "sound", sound));
                else if (fkdr >= finals * 0.1) alert(playerName, "Stats", Settings.bool(this, "sound", sound));
            });
            SNIPER_ALERTED.put(playerName, true);
        }, 1);
    }

    private void checkGear(Player player) {
        if (player == null || player == mc.player || TeamUtil.isTeam(player)
                || TeamUtil.ignoreFriends(player.getUUID().toString()) || TeamUtil.ignoreFriends(player.getScoreboardName())
                || SNIPER_ALERTED.containsKey(player.getScoreboardName())) return;
        boolean hasChain = false;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = player.getItemBySlot(slot);
            if (!armor.isEmpty() && ItemIds.is(armor, "chainmail")) { hasChain = true; break; }
        }
        boolean hasIronSword = ItemIds.isSword(player.getMainHandItem()) && ItemIds.is(player.getMainHandItem(), "iron_sword");
        if (hasChain && hasIronSword) {
            alert(player.getScoreboardName(), "Iron Sword + Chainmail Armor", Settings.bool(this, "sound", sound));
            SNIPER_ALERTED.put(player.getScoreboardName(), true);
        }
    }

    private void checkName(Player player) {
        if (player == null || TeamUtil.isTeam(player) || TeamUtil.ignoreFriends(player.getUUID().toString())
                || TeamUtil.ignoreFriends(player.getScoreboardName()) || SNIPER_ALERTED.containsKey(player.getScoreboardName())) return;
        String lower = player.getScoreboardName().toLowerCase(java.util.Locale.ROOT);
        for (String sniper : SNIPER_NAMES) {
            if (lower.contains(sniper)) {
                alert(player.getScoreboardName(), "Name", Settings.bool(this, "sound", sound));
                SNIPER_ALERTED.put(player.getScoreboardName(), true);
                return;
            }
        }
    }

    private void alert(String name, String reason, boolean playSound) {
        Meowtils.addMessage("§cWarning: §r" + NameUtil.getTabDisplayName(name) + "§7 might be a sniper! §8(§f" + reason + "§8)");
        if (Settings.bool(this, "fetchStats", fetchStats)) {
            Stats stats = get(Stats.class);
            if (stats != null && (Bedwars.GAME.isActive() || Skywars.GAME.isActive())) stats.request(name);
        }
        if (playSound) Util.playSound(Util.Sound.PING_DEEP, 100);
    }

    private void fetchBedwars(String name, StatsCallback callback) {
        Stats stats = get(Stats.class);
        String key = stats == null ? "" : Settings.text(stats, "apiKey", "");
        if (key.isBlank()) return;
        HttpJson.get(URI.create("https://api.mojang.com/users/profiles/minecraft/" + java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8)), Map.of())
                .thenCompose(profile -> {
                    String uuid = profile.has("id") ? profile.get("id").getAsString() : "";
                    if (uuid.isEmpty()) throw new IllegalStateException("missing uuid");
                    return HttpJson.get(URI.create("https://api.hypixel.net/v2/player?uuid=" + uuid), Map.of("API-Key", key));
                })
                .thenAccept(json -> {
                    if (!json.has("player") || !json.getAsJsonObject("player").has("stats")) return;
                    var bedwars = json.getAsJsonObject("player").getAsJsonObject("stats").getAsJsonObject("Bedwars");
                    if (bedwars == null) return;
                    int finals = bedwars.has("final_kills_bedwars") ? bedwars.get("final_kills_bedwars").getAsInt() : 0;
                    int finalDeaths = bedwars.has("final_deaths_bedwars") ? bedwars.get("final_deaths_bedwars").getAsInt() : 0;
                    int bedsLost = bedwars.has("beds_lost_bedwars") ? bedwars.get("beds_lost_bedwars").getAsInt() : 0;
                    double fkdr = finalDeaths == 0 ? finals : (double) finals / finalDeaths;
                    double clutch = bedsLost == 0 ? 0 : 1.0 - ((double) finalDeaths / bedsLost);
                    net.minecraft.client.Minecraft.getInstance().execute(() -> callback.accept(fkdr, finals, clutch));
                })
                .exceptionally(error -> null);
    }

    private interface StatsCallback { void accept(double fkdr, int finals, double clutch); }

    @Override
    public void onReset() { SNIPER_ALERTED.clear(); }
}
