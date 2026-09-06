package wtf.tatp.meowtils.module.bedwars;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.ScoreboardUtil;
import wtf.tatp.meowtils.util.Settings;

/** HUD countdown for diamond/emerald tiers, bed gone, sudden death, and emerald spawns. */
public final class EventTimers extends Module {
    @wtf.tatp.meowtils.config.Config public int eventPosX, eventPosY, emeraldPosX = 1, emeraldPosY = 1;
    @wtf.tatp.meowtils.config.Config public float scale = 0.65f;
    private static final int EMERALD_II_TIME = 720;
    private static final int EMERALD_III_TIME = 1440;
    private static final int FIRST_EMERALD_SPAWN_TIME = 31;
    private static final EmeraldEntry EIGHT_TEAMS_MODE_DATA = new EmeraldEntry(65, 50, 35, 4);
    private static final EmeraldEntry FOUR_TEAMS_MODE_DATA = new EmeraldEntry(55, 40, 27, 2);
    private static long gameStartTime;
    private static boolean gameStarted;

    private enum EventType { DIAMOND, EMERALD, BED_GONE, SUDDEN_DEATH, GAME_END }

    public EventTimers() {
        super("EventTimers", Category.Bedwars);
        tag(ModuleTag.LEGIT);
        tooltip("Displays event timers.");
        addSlider(new SliderValue("Scale", 0.5, 1.5, 0.05, null, "scale", this, Float.TYPE));
        addExpand(new ExpandValue("Events", expand -> {
            expand.addToggle(new ToggleValue("Enabled", "eventTime", this));
            expand.addToggle(new ToggleValue("Show next events only", "onlyNext", this));
            expand.addToggle(new ToggleValue("Roman numerals", "romanNumerals", this));
            expand.addToggle(new ToggleValue("Dynamic color", "eventDynamicColor", this));
            expand.addCheck(new CheckValue("§bDiamond §7Timer", "diamondTimer", this));
            expand.addCheck(new CheckValue("§2Emerald §7Timer", "emeraldTimer", this));
            expand.addCheck(new CheckValue("§6Bed Gone §7Timer", "bedGoneTimer", this));
            expand.addCheck(new CheckValue("§5Sudden Death §7Timer", "suddenDeathTimer", this));
            expand.addCheck(new CheckValue("§cGame End §7Timer", "gameEndTimer", this));
        }, this));
        addExpand(new ExpandValue("Emeralds", expand -> {
            expand.addToggle(new ToggleValue("Enabled", "emeraldTime", this));
            expand.addToggle(new ToggleValue("Dynamic color", "emeraldDynamicColor", this));
        }, this));
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (mc.gui.screen() != null && !BedwarsSupport.inEditor()) return;
        if (!BedwarsSupport.inEditor() && !inLiveMatch()) {
            gameStarted = false;
            gameStartTime = 0L;
            return;
        }
        if (!gameStarted) {
            gameStartTime = System.currentTimeMillis();
            gameStarted = true;
        }
        int elapsedSeconds = (int) Math.max(0L, (System.currentTimeMillis() - gameStartTime) / 1000);
        Integer fromBoard = EventTimerSync.elapsedFromLines(ScoreboardUtil.getSidebarLines());
        if (fromBoard != null) {
            elapsedSeconds = fromBoard;
            gameStartTime = System.currentTimeMillis() - fromBoard * 1000L;
        }
        float hudScale = (float) Settings.number(this, "scale", scale);
        if (Settings.bool(this, "eventTime", true)) {
            boolean diamondShown = false;
            boolean emeraldShown = false;
            int shown = 0;
            boolean roman = Settings.bool(this, "romanNumerals", false);
            boolean dynamic = Settings.bool(this, "eventDynamicColor", false);
            String diamond2 = (dynamic ? "§b" : "§f") + "Diamond §f" + (roman ? "II" : "2");
            String diamond3 = (dynamic ? "§b" : "§f") + "Diamond §f" + (roman ? "III" : "3");
            String emerald2 = (dynamic ? "§2" : "§f") + "Emerald §f" + (roman ? "II" : "2");
            String emerald3 = (dynamic ? "§2" : "§f") + "Emerald §f" + (roman ? "III" : "3");
            String bedGone = (dynamic ? "§6" : "§f") + "Bed Gone";
            String suddenDeath = (dynamic ? "§5" : "§f") + "Sudden Death";
            String gameEnd = (dynamic ? "§c" : "§f") + "Game End";
            EventEntry[] schedule = {
                    new EventEntry(new ItemStack(Items.DIAMOND), diamond2, EventTimerSync.DIAMOND_II, EventType.DIAMOND),
                    new EventEntry(new ItemStack(Items.EMERALD), emerald2, EventTimerSync.EMERALD_II, EventType.EMERALD),
                    new EventEntry(new ItemStack(Items.DIAMOND), diamond3, EventTimerSync.DIAMOND_III, EventType.DIAMOND),
                    new EventEntry(new ItemStack(Items.EMERALD), emerald3, EventTimerSync.EMERALD_III, EventType.EMERALD),
                    new EventEntry(new ItemStack(Items.BEDROCK), bedGone, EventTimerSync.BED_GONE, EventType.BED_GONE),
                    new EventEntry(new ItemStack(Blocks.BEACON), suddenDeath, EventTimerSync.SUDDEN_DEATH, EventType.SUDDEN_DEATH),
                    new EventEntry(new ItemStack(Blocks.BEDROCK), gameEnd, EventTimerSync.GAME_END, EventType.GAME_END)
            };
            int x = eventPosX;
            int y = eventPosY;
            int row = Math.max((int) ((mc.font.lineHeight * 2 + 4) * hudScale), (int) (16f * hudScale)) + (int) (4f * hudScale);
            for (EventEntry entry : schedule) {
                if (!shouldShow(entry.type)) continue;
                int remaining = entry.targetSeconds - elapsedSeconds;
                if (remaining <= 0) continue;
                if (entry.type == EventType.DIAMOND) {
                    if (diamondShown) continue;
                    diamondShown = true;
                }
                if (entry.type == EventType.EMERALD) {
                    if (emeraldShown) continue;
                    emeraldShown = true;
                }
                renderEventHud(event, entry.icon, entry.title, BedwarsSupport.formatTime(remaining), x, y, hudScale);
                y += row;
                if (Settings.bool(this, "onlyNext", false) && ++shown >= 2) break;
            }
        }
        if (!Settings.bool(this, "emeraldTime", true)) return;
        EmeraldEntry modeData = getModeData();
        int nextSpawnTime = getNextSpawnTime(elapsedSeconds, modeData);
        renderEmeraldHud(event, emeraldPosX, emeraldPosY, Math.max(0, nextSpawnTime - elapsedSeconds), getTotalEmeralds(elapsedSeconds, modeData), hudScale);
    }

    @EventTarget
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.isOverlay() || !BedwarsSupport.inMatch()) return;
        String plain = ColorUtil.plainLower(event.getText());
        if (plain.contains(":")) return;
        if (plain.contains("the game starts in 1 second") && !gameStarted) {
            gameStartTime = System.currentTimeMillis() + 1000;
            gameStarted = true;
        }
    }

    private static boolean inLiveMatch() {
        return Bedwars.GAME.isActive() && Bedwars.PRE_GAME.isNotActive();
    }

    private static boolean scoreboardShowsMatch() {
        return ScoreboardUtil.lineContains("diamond ii") || ScoreboardUtil.lineContains("diamond 2")
                || ScoreboardUtil.lineContains("emerald ii") || ScoreboardUtil.lineContains("emerald 2")
                || ScoreboardUtil.lineContains("bed gone") || ScoreboardUtil.lineContains("sudden death");
    }

    private void renderEventHud(HudRenderEvent event, ItemStack icon, String title, String time, int x, int y, float hudScale) {
        var g = event.getGraphics();
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(hudScale, hudScale);
        g.item(icon, 0, 0);
        wtf.tatp.meowtils.font.HudFont.draw(g, title, 18, 0, 1, 0xFFFFFFFF);
        wtf.tatp.meowtils.font.HudFont.draw(g, "§7" + time, 18, mc.font.lineHeight + 2, 1, 0xFFFFFFFF);
        g.pose().popMatrix();
    }

    private void renderEmeraldHud(HudRenderEvent event, int x, int y, int secondsUntilSpawn, int totalEmeralds, float hudScale) {
        String timeColor = !Settings.bool(this, "emeraldDynamicColor", true) ? "§7"
                : secondsUntilSpawn < 3 ? "§4" : secondsUntilSpawn < 5 ? "§c" : secondsUntilSpawn < 9 ? "§6" : secondsUntilSpawn < 12 ? "§e" : "§2";
        var g = event.getGraphics();
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(hudScale, hudScale);
        g.item(new ItemStack(Items.EMERALD), 0, Math.max(0, ((mc.font.lineHeight * 2 + 2) - 16) / 2));
        wtf.tatp.meowtils.font.HudFont.draw(g, "Next Emerald: " + timeColor + secondsUntilSpawn + "§7 s", 18, 0, 1, 0xFFFFFFFF);
        wtf.tatp.meowtils.font.HudFont.draw(g, "§fTotal: §2" + totalEmeralds, 18, mc.font.lineHeight + 2, 1, 0xFFFFFFFF);
        g.pose().popMatrix();
    }

    private boolean shouldShow(EventType type) {
        return switch (type) {
            case DIAMOND -> Settings.bool(this, "diamondTimer", true);
            case EMERALD -> Settings.bool(this, "emeraldTimer", true);
            case BED_GONE -> Settings.bool(this, "bedGoneTimer", true);
            case SUDDEN_DEATH -> Settings.bool(this, "suddenDeathTimer", true);
            case GAME_END -> Settings.bool(this, "gameEndTimer", true);
        };
    }

    private static int getNextSpawnTime(int elapsedSeconds, EmeraldEntry modeData) {
        int next = FIRST_EMERALD_SPAWN_TIME;
        while (elapsedSeconds >= next) next += modeData.getSpawnInterval(next);
        return next;
    }

    private static int getTotalEmeralds(int elapsedSeconds, EmeraldEntry modeData) {
        int total = 0;
        int spawn = FIRST_EMERALD_SPAWN_TIME;
        while (elapsedSeconds >= spawn) {
            total += modeData.emeraldsPerSpawn;
            spawn += modeData.getSpawnInterval(spawn);
        }
        return total;
    }

    private static EmeraldEntry getModeData() {
        if (Bedwars.THREES.isActive() || Bedwars.FOURS.isActive()) return FOUR_TEAMS_MODE_DATA;
        if (Bedwars.SOLOS.isActive() || Bedwars.DOUBLES.isActive()) return EIGHT_TEAMS_MODE_DATA;
        return FOUR_TEAMS_MODE_DATA;
    }

    private record EventEntry(ItemStack icon, String title, int targetSeconds, EventType type) {}

    private static final class EmeraldEntry {
        final int tierOneInterval, tierTwoInterval, tierThreeInterval, emeraldsPerSpawn;
        EmeraldEntry(int tierOneInterval, int tierTwoInterval, int tierThreeInterval, int emeraldsPerSpawn) {
            this.tierOneInterval = tierOneInterval;
            this.tierTwoInterval = tierTwoInterval;
            this.tierThreeInterval = tierThreeInterval;
            this.emeraldsPerSpawn = emeraldsPerSpawn;
        }
        int getSpawnInterval(int elapsedSeconds) {
            if (elapsedSeconds >= EMERALD_III_TIME) return tierThreeInterval;
            if (elapsedSeconds >= EMERALD_II_TIME) return tierTwoInterval;
            return tierOneInterval;
        }
    }

    @Override
    public List<wtf.tatp.meowtils.gui.hudeditor.HudEntry> hudEditor() {
        float hudScale = (float) Settings.number(this, "scale", scale);
        return List.of(
                new wtf.tatp.meowtils.gui.hudeditor.HudEntry("EventTimers (Events)", this, "eventPosX", "eventPosY",
                        () -> wtf.tatp.meowtils.gui.GuiUtil.getHudBounds("XXX Sudden Death", 11, hudScale)),
                new wtf.tatp.meowtils.gui.hudeditor.HudEntry("EventTimers (Emeralds)", this, "emeraldPosX", "emeraldPosY",
                        () -> wtf.tatp.meowtils.gui.GuiUtil.getHudBounds("XXX Next Emerald: 99", 2, hudScale))
        );
    }

    @Override
    public void onReset() {
        gameStarted = false;
        gameStartTime = 0L;
    }
}
