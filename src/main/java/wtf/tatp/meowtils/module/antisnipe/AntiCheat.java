package wtf.tatp.meowtils.module.antisnipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import wtf.tatp.meowtils.CommandManager;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.NameUtil;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;
import wtf.tatp.meowtils.util.Util;
import wtf.tatp.meowtils.util.anticheat.AntiCheatData;

public final class AntiCheat extends Module {
    @Config public boolean enabled;
    @Config public int key;
    @Config public int violationLevel;
    @Config public boolean flagSound = true;
    @Config public boolean wdrButton = true;
    @Config public boolean autoBlock = true;
    @Config public boolean noSlow = true;
    @Config public boolean killaura = true;
    @Config public boolean legitScaffold = true;
    @Config public String componentColor = "RED";
    @Config public String buttonColor = "AQUA";
    @Config public String bracketColor = "GRAY";
    private static final Map<UUID, AntiCheatData> ANTICHEAT_DATA = new HashMap<>();
    private static final Map<String, Map<String, Integer>> VIOLATION_LEVELS = new HashMap<>();

    public AntiCheat() {
        super("AntiCheat", Category.Antisnipe);
        tag(ModuleTag.LEGIT);
        tooltip("Detects suspicious behaviour of players around you. May not be 100% accurate.\n§d/anticheat §f- Set anticheat message colors");
        addSlider(new SliderValue("Violation level", 0, 10, 1, null, "violationLevel", this, Integer.TYPE));
        addToggle(new ToggleValue("Flag sound", "flagSound", this));
        addToggle(new ToggleValue("WDR Button", "wdrButton", this));
        addCheck(new CheckValue("AutoBlock", "autoBlock", this));
        addCheck(new CheckValue("NoSlow", "noSlow", this));
        addCheck(new CheckValue("Killaura", "killaura", this));
        addCheck(new CheckValue("Legit Scaffold", "legitScaffold", this));
        CommandManager.register("anticheat", root -> root.executes(c -> { openColorPicker(); return 1; }),
                "anticheatcolor", "accolor", "acc", "flagcolor");
        CommandManager.register(com.mojang.brigadier.builder.LiteralArgumentBuilder.<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>literal("setflagmessagecolor")
                .then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource, String>argument("target", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource, String>argument("color", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .executes(c -> { setColor(com.mojang.brigadier.arguments.StringArgumentType.getString(c, "target"), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "color")); return 1; }))));
    }

    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null || event.getPhase() != ClientTickEvent.Phase.POST) return;
        AutoBlacklist auto = get(AutoBlacklist.class);
        for (Player player : mc.level.players()) {
            if (player == mc.player || player.getScoreboardName() == null || TeamUtil.isTeam(player)
                    || TeamUtil.ignoreFriends(player.getUUID().toString()) || TeamUtil.ignoreFriends(player.getScoreboardName())) continue;
            AntiCheatData data = ANTICHEAT_DATA.computeIfAbsent(player.getUUID(), key -> new AntiCheatData());
            data.anticheatCheck(player);
            String name = player.getScoreboardName();
            if (data.failedAutoBlock() && incrementViolation(name, "AutoBlock")) {
                sendFlagMessage(name, "AutoBlock");
                data.autoBlockCheck.reset();
                blacklist(name, "autoblock", auto != null && Settings.bool(auto, "flagAutoblock", true));
            }
            if (data.failedNoSlow() && incrementViolation(name, "NoSlow")) {
                sendFlagMessage(name, "NoSlow");
                data.noSlowCheck.reset();
                blacklist(name, "noslow", auto != null && Settings.bool(auto, "flagNoslow", true));
            }
            if (data.failedLegitScaffold() && incrementViolation(name, "Legit Scaffold")) {
                sendFlagMessage(name, "Legit Scaffold");
                data.legitScaffoldCheck.reset(player.getUUID());
                blacklist(name, "legit scaffold", auto != null && Settings.bool(auto, "flagLegitScaffold", true));
            }
            if (data.failedKillauraB() && incrementViolation(name, "KillAura")) {
                sendFlagMessage(name, "Killaura");
                data.killauraCheck.reset();
                blacklist(name, "killaura", auto != null && Settings.bool(auto, "flagKillaura", true));
            }
        }
    }

    private boolean incrementViolation(String playerName, String checkType) {
        Map<String, Integer> playerViolations = VIOLATION_LEVELS.computeIfAbsent(playerName, key -> new HashMap<>());
        int level = playerViolations.getOrDefault(checkType, 0) + 1;
        playerViolations.put(checkType, level);
        if (level >= Settings.integer(this, "violationLevel", violationLevel)) {
            playerViolations.put(checkType, 0);
            return true;
        }
        return false;
    }

    private static void blacklist(String playerName, String reason, boolean shouldBlacklist) {
        AutoBlacklist auto = get(AutoBlacklist.class);
        if (auto != null && auto.getState() && Settings.bool(auto, "forFlags", true) && shouldBlacklist && Server.HYPIXEL_REPLAY.isNotActive()) {
            AutoBlacklist.blacklistPlayer(playerName, reason);
        }
    }

    private void sendFlagMessage(String playerName, String checkType) {
        ChatFormatting reason = ColorUtil.formattingFromName(Settings.text(this, "componentColor", componentColor));
        String msg = NameUtil.getTabDisplayName(playerName) + "§7 failed " + reason + checkType;
        if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) {
            if (Settings.bool(this, "wdrButton", wdrButton) && mc.player != null) {
                ChatFormatting button = ColorUtil.getColorFromString(Settings.text(this, "buttonColor", buttonColor));
                ChatFormatting bracket = ColorUtil.getColorFromString(Settings.text(this, "bracketColor", bracketColor));
                MutableComponent message = Meowtils.prefixed(msg + " ").copy();
                MutableComponent wdr = Component.literal("[").withStyle(bracket)
                        .append(Component.literal("WDR").withStyle(button))
                        .append(Component.literal("]").withStyle(bracket));
                wdr.setStyle(wdr.getStyle()
                        .withClickEvent(new ClickEvent.RunCommand("/wdr " + playerName))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal(ChatFormatting.DARK_AQUA + "Click to report this player."))));
                Meowtils.addChat(message.append(wdr));
            } else Meowtils.addMessage(msg);
        }
        if (Notifications.getMode() != Notifications.Mode.CHAT) {
            NotificationManager.show("AntiCheat", msg, NotificationManager.Type.WARNING, 1500L);
        }
        if (Settings.bool(this, "flagSound", flagSound)) Util.playSound(Util.Sound.PING, 100);
        wtf.tatp.meowtils.module.hypixel.PartyNotifier.antiCheat(playerName, checkType);
    }

    public static void openColorPicker() {
        AntiCheat module = get(AntiCheat.class);
        if (module != null) module.openColors();
    }

    public static void applyClickedColor(String target, String color) {
        AntiCheat module = get(AntiCheat.class);
        if (module != null) module.setColor(target, color);
    }

    private void openColors() {
        if (mc.player == null) return;
        ChatFormatting[] colors = {
                ChatFormatting.BLACK, ChatFormatting.DARK_GRAY, ChatFormatting.GRAY, ChatFormatting.WHITE,
                ChatFormatting.DARK_RED, ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW,
                ChatFormatting.DARK_GREEN, ChatFormatting.GREEN, ChatFormatting.DARK_AQUA, ChatFormatting.AQUA,
                ChatFormatting.DARK_BLUE, ChatFormatting.BLUE, ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE
        };
        for (String target : List.of("Reason", "WDR", "Bracket")) {
            MutableComponent line = Meowtils.prefixed(ChatFormatting.WHITE + target.toUpperCase() + ": ").copy();
            for (ChatFormatting color : colors) {
                if (!ColorUtil.isColor(color)) continue;
                MutableComponent swatch = Component.literal("⬛").withStyle(color);
                swatch.setStyle(swatch.getStyle()
                        .withClickEvent(new ClickEvent.RunCommand("/setflagmessagecolor " + target + " " + color.name()))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to set " + target.toUpperCase() + " to " + color + color.name()))));
                line.append(swatch);
            }
            Meowtils.addChat(line);
        }
    }

    private void setColor(String target, String colorName) {
        try {
            ChatFormatting.valueOf(colorName.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            Meowtils.addMessage("§cInvalid color name.");
            return;
        }
        switch (target.toLowerCase(java.util.Locale.ROOT)) {
            case "reason" -> componentColor = colorName.toUpperCase(java.util.Locale.ROOT);
            case "wdr" -> buttonColor = colorName.toUpperCase(java.util.Locale.ROOT);
            case "bracket" -> bracketColor = colorName.toUpperCase(java.util.Locale.ROOT);
            default -> { Meowtils.addMessage("§cInvalid letter."); return; }
        }
        settingsStorage().put(target.equalsIgnoreCase("reason") ? "componentColor" : target.equalsIgnoreCase("wdr") ? "buttonColor" : "bracketColor",
                target.equalsIgnoreCase("reason") ? componentColor : target.equalsIgnoreCase("wdr") ? buttonColor : bracketColor);
        ConfigManager.save();
        Meowtils.addMessage("§aSet color for '" + target.toUpperCase(java.util.Locale.ROOT) + "' to " + colorName.toUpperCase(java.util.Locale.ROOT));
    }

    @Override
    public void onReset() {
        ANTICHEAT_DATA.clear();
        VIOLATION_LEVELS.clear();
    }
}
