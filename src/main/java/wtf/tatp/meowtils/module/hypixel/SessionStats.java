package wtf.tatp.meowtils.module.hypixel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.ReceivePacketEvent;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.GuiUtil;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ButtonValue;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.util.DelayedTask;
import wtf.tatp.meowtils.util.HypixelUtil;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.Util;

public final class SessionStats extends Module {
    private static final String SEPARATOR = ChatFormatting.GRAY + " | " + ChatFormatting.RESET;
    @wtf.tatp.meowtils.config.Config public int posX = 1, posY = 1;
    private static boolean activated;
    private static int bedwarsKills, bedwarsFinalKills, bedwarsFinalDeaths, bedwarsExp;
    private static float bedwarsFkdr;
    private static int bedwarsKillsTemp, bedwarsFinalKillsTemp, bedwarsExpTemp;
    private static int skywarsKills, skywarsExp, skywarsKillsTemp, skywarsExpTemp;

    public SessionStats() {
        super("SessionStats", Category.Hypixel);
        tag(ModuleTag.LEGIT);
        tooltip("Tracks your own stats for this game session.");
        addButton(new ButtonValue("Reset", 5.0f, () -> {
            resetNormal();
            resetTemp();
            if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) Meowtils.addMessage("Reset session stats!");
            if (Notifications.getMode() != Notifications.Mode.CHAT) {
                NotificationManager.show("SessionStats", "Reset stats!", NotificationManager.Type.INFO, 1500L);
            }
        }));
    }

    @EventTarget(priority = EventPriority.HIGHEST)
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.isOverlay()) return;
        if (Server.HYPIXEL.isNotActive() && Server.UNIVERSAL.isNotActive()) return;
        String msg = event.getText();
        if (Bedwars.GAME.isActive()) {
            if (msg.equals("You have been eliminated!")) {
                bedwarsFinalDeaths++;
                bedwarsFkdr = bedwarsFinalDeaths == 0 ? bedwarsFinalKills : (float) bedwarsFinalKills / bedwarsFinalDeaths;
            }
            if (msg.contains("Bed Wars XP") && msg.contains("+") && !msg.contains("Quest")) {
                try {
                    String value = msg.replaceAll("[^0-9]", "");
                    if (!value.isEmpty()) {
                        int parsed = Integer.parseInt(value);
                        bedwarsExp += parsed;
                        bedwarsExpTemp += parsed;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        if (Skywars.GAME.isActive() || Skywars.MINI.isActive()) {
            if (msg.startsWith("+") && msg.contains("SkyWars Experience") && msg.endsWith("Kill") && !msg.contains(":")) {
                skywarsKills++;
                skywarsKillsTemp++;
            }
            if (msg.contains("SkyWars Experience") && msg.contains("+") && !msg.contains("Quest")) {
                try {
                    String value = msg.replaceAll("[^0-9]", "");
                    if (!value.isEmpty()) {
                        int parsed = Integer.parseInt(value);
                        skywarsExp += parsed;
                        skywarsExpTemp += parsed;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        if (!Settings.bool(this, "recap", true) || activated || !HypixelUtil.isGameEnd(msg)) return;
        if (Bedwars.GAME.isActive()) {
            new DelayedTask(() -> {
                Meowtils.addMessage(ChatFormatting.LIGHT_PURPLE + "Last game: " + ChatFormatting.WHITE + "Kills: "
                        + ChatFormatting.BLUE + bedwarsKillsTemp + SEPARATOR + "Finals: " + ChatFormatting.BLUE
                        + bedwarsFinalKillsTemp + SEPARATOR + "EXP: " + ChatFormatting.BLUE + bedwarsExpTemp);
                resetTemp();
            }, 40);
        } else if (Skywars.GAME.isActive() || Skywars.MINI.isActive()) {
            new DelayedTask(() -> {
                Meowtils.addMessage(ChatFormatting.LIGHT_PURPLE + "Last game: " + ChatFormatting.WHITE + "Kills: "
                        + ChatFormatting.BLUE + skywarsKillsTemp + SEPARATOR + "EXP: " + ChatFormatting.BLUE + skywarsExpTemp);
                resetTemp();
            }, 40);
        }
        activated = true;
    }

    @EventTarget
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!(event.getPacket() instanceof ClientboundTabListPacket packet)) return;
        if ((Server.HYPIXEL.isNotActive() && Server.UNIVERSAL.isNotActive()) || Bedwars.GAME.isNotActive()) return;
        if (packet.footer() == null) return;
        String footer = packet.footer().getString();
        if (footer == null || footer.isEmpty()) return;
        int killsIndex = footer.indexOf("Kills: ");
        int finalKillsIndex = footer.indexOf("Final Kills: ");
        if (killsIndex == -1 || finalKillsIndex == -1) return;
        int parsedKills = Util.parseIntFromString(footer, killsIndex + 7);
        int parsedFinalKills = Util.parseIntFromString(footer, finalKillsIndex + 13);
        bedwarsKills = parsedKills;
        bedwarsKillsTemp = parsedKills;
        bedwarsFinalKills = parsedFinalKills;
        bedwarsFinalKillsTemp = parsedFinalKills;
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (mc.gui.screen() != null && !GuiUtil.inEditor()) return;
        if (Server.HYPIXEL.isNotActive() && Server.UNIVERSAL.isNotActive() && !GuiUtil.inEditor()) return;
        int x = posX;
        int y = posY;
        int color = new Color(Settings.integer(this, "red", 255), Settings.integer(this, "green", 255), Settings.integer(this, "blue", 255)).getRGB();
        float scale = (float) Settings.number(this, "scale", 0.65);
        boolean reset = Settings.bool(this, "reset", true);
        boolean vertical = "Vertical".equals(Settings.text(this, "mode", "Horizontal"));
        var g = event.getGraphics();
        List<String> counterStrings = new ArrayList<>();
        if ((Settings.bool(this, "bedwars", true) && Bedwars.ALL.isActive()) || GuiUtil.inEditor()) {
            ChatFormatting killsColor = reset
                    ? (bedwarsKills < 5 ? ChatFormatting.GREEN : bedwarsKills < 10 ? ChatFormatting.YELLOW : bedwarsKills < 15 ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE)
                    : (bedwarsKills < 50 ? ChatFormatting.GREEN : bedwarsKills < 100 ? ChatFormatting.YELLOW : bedwarsKills < 200 ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE);
            ChatFormatting finalsColor = reset
                    ? (bedwarsFinalKills < 4 ? ChatFormatting.GREEN : bedwarsFinalKills < 6 ? ChatFormatting.YELLOW : bedwarsFinalKills < 8 ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE)
                    : (bedwarsFinalKills < 30 ? ChatFormatting.GREEN : bedwarsFinalKills < 60 ? ChatFormatting.YELLOW : bedwarsFinalKills < 100 ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE);
            ChatFormatting fkdrColor = reset
                    ? (bedwarsFkdr < 2.0f ? ChatFormatting.GREEN : bedwarsFinalKills < 5 ? ChatFormatting.YELLOW : bedwarsFinalKills < 8 ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE)
                    : (bedwarsFinalKills < 20 ? ChatFormatting.GREEN : bedwarsFinalKills < 40 ? ChatFormatting.YELLOW : bedwarsFinalKills < 60 ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE);
            ChatFormatting expColor = reset
                    ? (bedwarsExp < 300 ? ChatFormatting.GREEN : bedwarsExp < 500 ? ChatFormatting.YELLOW : bedwarsExp < 750 ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE)
                    : (bedwarsExp < 2000 ? ChatFormatting.GREEN : bedwarsExp < 4000 ? ChatFormatting.YELLOW : bedwarsExp < 6000 ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE);
            if (Settings.bool(this, "bedwarsSessionKills", true)) counterStrings.add("Kills: " + killsColor + bedwarsKills);
            if (Settings.bool(this, "bedwarsSessionFinals", true)) counterStrings.add("Finals: " + finalsColor + bedwarsFinalKills);
            if (Settings.bool(this, "bedwarsSessionFkdr", true)) {
                counterStrings.add("FKDR: " + fkdrColor + String.format("%.1f", bedwarsFkdr).replace(",", "."));
            }
            if (vertical ? Settings.bool(this, "bedwarsSessionFinals", true) : Settings.bool(this, "bedwarsSessionExp", true)) {
                counterStrings.add("EXP: " + expColor + bedwarsExp);
            }
            drawLines(g, counterStrings, x, y, scale, color, vertical);
            if (vertical) y += counterStrings.size() * wtf.tatp.meowtils.font.HudFont.lineOffset(scale);
        }
        if (Settings.bool(this, "skywars", true) && Skywars.ALL.isActive() && !GuiUtil.inEditor()) {
            ChatFormatting killsColor = reset
                    ? (skywarsKills < 2 ? ChatFormatting.GREEN : skywarsKills < 5 ? ChatFormatting.YELLOW : skywarsKills < 10 ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE)
                    : (skywarsKills < 20 ? ChatFormatting.GREEN : skywarsKills < 50 ? ChatFormatting.YELLOW : skywarsKills < 100 ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE);
            ChatFormatting expColor = reset
                    ? (skywarsExp < 3 ? ChatFormatting.GREEN : skywarsExp < 7 ? ChatFormatting.YELLOW : skywarsExp < 15 ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE)
                    : (skywarsExp < 50 ? ChatFormatting.GREEN : skywarsExp < 100 ? ChatFormatting.YELLOW : skywarsExp < 200 ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE);
            List<String> sky = new ArrayList<>();
            if (Settings.bool(this, "skywarsSessionKills", true)) sky.add("Kills: " + killsColor + skywarsKills);
            if (Settings.bool(this, "skywarsSessionExp", true)) sky.add("EXP: " + expColor + skywarsExp);
            drawLines(g, sky, x, y, scale, color, vertical);
        }
    }

    private void drawLines(net.minecraft.client.gui.GuiGraphicsExtractor g, List<String> lines, int x, int y, float scale, int color, boolean vertical) {
        if (lines.isEmpty()) return;
        if (vertical) {
            for (String text : lines) {
                wtf.tatp.meowtils.font.HudFont.draw(g, text, x, y, scale, color);
                y += wtf.tatp.meowtils.font.HudFont.lineOffset(scale);
            }
        } else {
            wtf.tatp.meowtils.font.HudFont.draw(g, String.join(SEPARATOR, lines), x, y, scale, color);
        }
    }

    private static void resetNormal() {
        bedwarsKills = bedwarsFinalKills = bedwarsExp = skywarsKills = skywarsExp = bedwarsFinalDeaths = 0;
        bedwarsFkdr = 0.0f;
    }

    private static void resetTemp() {
        bedwarsKillsTemp = bedwarsFinalKillsTemp = bedwarsExpTemp = skywarsKillsTemp = skywarsExpTemp = 0;
    }

    @Override
    public List<wtf.tatp.meowtils.gui.hudeditor.HudEntry> hudEditor() {
        float scale = (float) Settings.number(this, "scale", 0.65);
        return List.of(new wtf.tatp.meowtils.gui.hudeditor.HudEntry(null, this, "posX", "posY",
                () -> GuiUtil.getHudBounds("Kills: 0 Finals: 0 FKDR: 0 EXP: 9999", 1, scale)));
    }

    @Override
    public void onReset() {
        if (Settings.bool(this, "reset", true)) resetNormal();
        resetTemp();
        activated = false;
    }
}
