package wtf.tatp.meowtils.manager.session;

import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.ReceivePacketEvent;
import wtf.tatp.meowtils.event.WorldEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.util.ScoreboardUtil;

/** Port of 2.0.1 session detection: scoreboard title/lines plus address fallback for ViaVersion. */
public final class SessionManager {
    private static long lastRefresh;
    public static boolean universalServer, mineplex, hypixel, hypixelReplay;
    public static boolean skywars, skywarsLobby, skywarsGame, skywarsGameMini;
    public static boolean bedwars, bedwarsLobby, bedwarsPractice, bedwarsGamePre, bedwarsGame;
    public static boolean bedwarsGameSolos, bedwarsGameDoubles, bedwarsGameThrees, bedwarsGameFours, bedwarsGameOneBlock, bedwarsGameFourFour;
    public static boolean duels, duelsLobby, duelsBedwars, megaWalls, megaWallsLobby, megaWallsGame, murderMystery;

    @EventTarget public void onPacket(ReceivePacketEvent event) { refreshThrottled(); }
    @EventTarget public void onTick(ClientTickEvent event) { if (event.getPhase() == ClientTickEvent.Phase.POST) refreshThrottled(); }
    @EventTarget public void onWorld(WorldEvent event) { if (event.getType() == WorldEvent.Type.UNLOAD) resetStates(); }

    private static void refreshThrottled() {
        long now = System.currentTimeMillis();
        if (now - lastRefresh < 500) return;
        lastRefresh = now;
        if (Minecraft.getInstance().level == null) { resetStates(); return; }
        try { updateStates(); } catch (Exception ignored) {}
    }

    private static void updateStates() {
        boolean addressHypixel = addressLooksLikeHypixel();
        universalServer = ScoreboardUtil.lineContains("fakepixel") || ScoreboardUtil.lineContains("blocksmc");
        mineplex = ScoreboardUtil.lineContains("mineplex");
        hypixel = addressHypixel || ScoreboardUtil.lineContains("hypixel") || ScoreboardUtil.titleContains("hypixel");
        hypixelReplay = hypixel && (ScoreboardUtil.titleContains("replay") || ScoreboardUtil.titleContains("atlas"));
        skywars = ScoreboardUtil.titleContains("skywars") || ScoreboardUtil.titleContains("sky wars");
        skywarsLobby = skywars && ScoreboardUtil.lineContains("your level:");
        skywarsGame = (skywars && ScoreboardUtil.lineContains("mode: normal")) || ScoreboardUtil.lineContains("mode: insane");
        skywarsGameMini = skywars && ScoreboardUtil.lineContains("mode: mini");
        bedwars = ScoreboardUtil.titleContains("bed wars") || ScoreboardUtil.titleContains("bedwars");
        bedwarsPractice = ScoreboardUtil.titleContains("bed wars practice");
        bedwarsLobby = !bedwarsPractice && ScoreboardUtil.titleContains("bed wars") && ScoreboardUtil.lineContains("level:");
        bedwarsGamePre = (!bedwarsPractice && ScoreboardUtil.titleContains("bed wars") && ScoreboardUtil.lineContains("waiting...")) || ScoreboardUtil.lineContains("starting in");
        bedwarsGame = bedwarsPractice || bedwarsLobby || (!ScoreboardUtil.titleContains("bed wars") && !ScoreboardUtil.titleContains("bedwars")) ? false : true;
        bedwarsGameSolos = bedwarsGame && ScoreboardUtil.lineContains("mode: solo");
        bedwarsGameDoubles = bedwarsGame && ScoreboardUtil.lineContains("mode: doubles");
        bedwarsGameThrees = bedwarsGame && ScoreboardUtil.lineContains("mode: 3v3v3v3");
        bedwarsGameFours = bedwarsGame && ScoreboardUtil.lineContains("mode: 4v4v4v4");
        bedwarsGameOneBlock = bedwarsGame && ScoreboardUtil.lineContains("mode: one block");
        bedwarsGameFourFour = bedwarsGame && ScoreboardUtil.lineContains("mode: 4v4");
        duels = ScoreboardUtil.titleContains("duels");
        duelsLobby = duels && ScoreboardUtil.lineContains("tokens:");
        duelsBedwars = ScoreboardUtil.titleContains("bed wars") && ScoreboardUtil.lineContains("mode: bed wars duel");
        megaWalls = ScoreboardUtil.titleContains("mega walls");
        megaWallsLobby = megaWalls;
        megaWallsGame = megaWalls;
        murderMystery = ScoreboardUtil.titleContains("murder mystery");
    }

    public static boolean addressLooksLikeHypixel() {
        var info = Minecraft.getInstance().getCurrentServer();
        if (info == null) return false;
        String ip = String.valueOf(info.ip).toLowerCase(java.util.Locale.ROOT);
        String name = String.valueOf(info.name).toLowerCase(java.util.Locale.ROOT);
        return ip.contains("hypixel") || name.contains("hypixel");
    }

    public static void resetStates() {
        hypixelReplay = hypixel = universalServer = mineplex = false;
        skywarsGameMini = skywarsGame = skywarsLobby = skywars = false;
        bedwarsGame = bedwarsGamePre = bedwarsPractice = bedwarsLobby = bedwars = false;
        bedwarsGameFours = bedwarsGameThrees = bedwarsGameDoubles = bedwarsGameSolos = false;
        bedwarsGameFourFour = bedwarsGameOneBlock = false;
        duelsBedwars = duelsLobby = duels = false;
        megaWallsGame = megaWallsLobby = megaWalls = murderMystery = false;
    }
}
