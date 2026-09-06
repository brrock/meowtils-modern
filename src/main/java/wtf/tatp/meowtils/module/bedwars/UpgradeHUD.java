package wtf.tatp.meowtils.module.bedwars;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.module.meowtils.GUI;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.Settings;

/** HUD of your team's sharpness, protection, traps, feather falling, heal pool, and forge. */
public final class UpgradeHUD extends Module {
    @wtf.tatp.meowtils.config.Config public int posX = 1, posY = 1, red = GUI.BLUE_DEFAULT, green = GUI.BLUE_DEFAULT, blue = GUI.BLUE_DEFAULT;
    @wtf.tatp.meowtils.config.Config public float scale = 0.65f;
    private static final Queue<String> TRAP_QUEUE = new ArrayDeque<>();
    private static final String FALSE_ICON = "§c✗";
    private static int sharpnessLevel, sharpnessLevelCached;
    private static int protectionLevel, protectionLevelCached;
    private static String trapName = "", trapNameCached = "";
    private static int featherFallingLevel, featherFallingLevelCached;
    private static boolean healPoolEnabled, healPoolEnabledCached;
    private static String forgeLevel = "", forgeLevelCached = "";

    public UpgradeHUD() {
        super("UpgradeHUD", Category.Bedwars);
        tag(ModuleTag.LEGIT);
        tooltip("Displays team upgrades on your screen. Only works with English language selected on Hypixel.");
        ColorLink color = new ColorLink("red", "green", "blue", this);
        addColor(new ColorValue("Text color", color));
        addSaturation(new SaturationValue(color));
        addBrightness(new BrightnessValue(color));
        addSlider(new SliderValue("Scale", 0.5, 1.5, 0.05, null, "scale", this, Float.TYPE));
        addToggle(new ToggleValue("Short names", "shortNames", this));
        addCheck(new CheckValue("Show §bSharpness", "showSharpness", this));
        addCheck(new CheckValue("Show §3Protection", "showProtection", this));
        addCheck(new CheckValue("Show §9Traps", "showTraps", this));
        addCheck(new CheckValue("Show §aFeather Falling", "showFeatherFalling", this));
        addCheck(new CheckValue("Show §dHeal Pool", "showHealPool", this));
        addCheck(new CheckValue("Show §7Forge", "showForge", this));
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.player == null || mc.level == null) return;
        boolean inGame = (Bedwars.GAME.isActive() && Bedwars.PRE_GAME.isNotActive()) || wtf.tatp.meowtils.manager.session.SessionManager.duelsBedwars;
        if (!BedwarsSupport.inEditor() && !(inGame && mc.gui.screen() == null)) return;
        float hudScale = (float) Settings.number(this, "scale", scale);
        int color = BedwarsSupport.rgb(Settings.integer(this, "red", red), Settings.integer(this, "green", green), Settings.integer(this, "blue", blue));
        List<String> lines = getDisplayString();
        lines.sort(Comparator.comparingDouble((String line) -> wtf.tatp.meowtils.font.HudFont.width(line, 1)).reversed());
        int y = posY;
        for (String line : lines) {
            BedwarsSupport.drawHud(event.getGraphics(), line, posX, y, hudScale, color);
            y += wtf.tatp.meowtils.font.HudFont.lineOffset(hudScale);
        }
    }

    private List<String> getDisplayString() {
        List<String> lines = new ArrayList<>();
        if (Settings.bool(this, "showSharpness", true)) {
            lines.add(formatUpgradeName("Sharpness: ") + (sharpnessLevel > 0 ? "§a" + sharpnessLevel : FALSE_ICON));
        }
        if (Settings.bool(this, "showProtection", true)) {
            lines.add(formatUpgradeName("Protection: ") + (protectionLevel > 0 ? "§a" + protectionLevel : FALSE_ICON));
        }
        if (Settings.bool(this, "showTraps", true)) {
            lines.add(formatUpgradeName("Trap: ") + (trapName.isEmpty() ? FALSE_ICON : "§a" + trapName));
        }
        if (Settings.bool(this, "showFeatherFalling", true)) {
            lines.add(formatUpgradeName("Feather Falling: ") + (featherFallingLevel > 0 ? "§a" + featherFallingLevel : FALSE_ICON));
        }
        if (Settings.bool(this, "showHealPool", true)) {
            lines.add(formatUpgradeName("Heal Pool: ") + (healPoolEnabled ? "§a✓" : FALSE_ICON));
        }
        if (Settings.bool(this, "showForge", true)) {
            lines.add(formatUpgradeName("Forge: ") + (forgeLevel.isEmpty() ? FALSE_ICON : getForgeLevel(forgeLevel)));
        }
        return lines;
    }

    @EventTarget
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.isOverlay() || !BedwarsSupport.inMatch()) return;
        String msg = event.getText();
        String plain = ColorUtil.unformattedText(msg);
        String lower = ColorUtil.plainLower(plain);
        if (lower.equals("you will respawn because you still have a bed!")) {
            sharpnessLevel = sharpnessLevelCached;
            protectionLevel = protectionLevelCached;
            trapName = trapNameCached;
            featherFallingLevel = featherFallingLevelCached;
            healPoolEnabled = healPoolEnabledCached;
            forgeLevel = forgeLevelCached;
        }
        if (lower.contains("purchased") && !plain.contains(":")) {
            if (lower.contains("sharpened swords")) {
                sharpnessLevel = lower.contains("ii") ? 2 : 1;
                sharpnessLevelCached = sharpnessLevel;
            }
            if (lower.contains("reinforced armor")) {
                if (lower.contains("iv")) protectionLevel = 4;
                else if (lower.contains("iii")) protectionLevel = 3;
                else if (lower.contains("ii")) protectionLevel = 2;
                else if (lower.contains("i")) protectionLevel = 1;
                protectionLevelCached = protectionLevel;
            }
            if (lower.contains("trap")) {
                if (lower.contains("miner fatigue")) addTrap("Miner Fatigue");
                else if (lower.contains("blindness")) addTrap("Blindness");
                else if (lower.contains("reveal")) addTrap("Reveal");
                else if (lower.contains("counter-offensive")) addTrap("Counter-Offensive");
            }
            if (lower.contains("cushioned boots")) {
                featherFallingLevel = lower.contains("ii") ? 2 : lower.contains("i") ? 1 : featherFallingLevel;
                featherFallingLevelCached = featherFallingLevel;
            }
            if (lower.contains("heal pool")) {
                healPoolEnabled = true;
                healPoolEnabledCached = true;
            }
            if (lower.contains("forge")) {
                if (lower.contains("iron")) forgeLevel = "Iron";
                else if (lower.contains("golden")) forgeLevel = "Golden";
                else if (lower.contains("emerald")) forgeLevel = "Emerald";
                else if (lower.contains("molten")) forgeLevel = "Molten";
                forgeLevelCached = forgeLevel;
            }
        }
        if (lower.contains("trap was set off!") || lower.contains("your bed was destroyed")) {
            trapName = TRAP_QUEUE.poll();
            trapNameCached = trapName;
            if (trapName == null) {
                trapName = "";
                trapNameCached = "";
            }
        }
    }

    private static String getForgeLevel(String value) {
        return switch (value) {
            case "Iron" -> "§7" + value;
            case "Golden" -> "§6" + value;
            case "Emerald" -> "§2" + value;
            case "Molten" -> "§4" + value;
            default -> value;
        };
    }

    private static void addTrap(String trap) {
        if (trapName.isEmpty()) {
            trapName = trap;
            trapNameCached = trap;
        } else {
            TRAP_QUEUE.offer(trap);
        }
    }

    private String formatUpgradeName(String upgradeName) {
        if (!Settings.bool(this, "shortNames", false)) return upgradeName;
        return switch (upgradeName) {
            case "Sharpness: " -> "Sharp: ";
            case "Protection: " -> "Prot: ";
            case "Feather Falling: " -> "Feather: ";
            case "Heal Pool: " -> "Heal: ";
            default -> upgradeName;
        };
    }

    @Override
    public List<wtf.tatp.meowtils.gui.hudeditor.HudEntry> hudEditor() {
        float hudScale = (float) Settings.number(this, "scale", scale);
        return List.of(new wtf.tatp.meowtils.gui.hudeditor.HudEntry(null, this, "posX", "posY",
                () -> wtf.tatp.meowtils.gui.GuiUtil.getHudBounds(Settings.bool(this, "shortNames", false) ? "Feather: X" : "Feather Falling: X", 6, hudScale)));
    }

    @Override
    public void onReset() {
        sharpnessLevel = 0;
        protectionLevel = 0;
        trapName = "";
        featherFallingLevel = 0;
        healPoolEnabled = false;
        forgeLevel = "";
        TRAP_QUEUE.clear();
    }
}
