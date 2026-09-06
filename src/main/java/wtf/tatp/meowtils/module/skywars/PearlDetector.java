package wtf.tatp.meowtils.module.skywars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.phys.AABB;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.PlayerInteractEvent;
import wtf.tatp.meowtils.event.ReceivePacketEvent;
import wtf.tatp.meowtils.event.RenderWorldLastEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.hudeditor.HudEntry;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.module.render.WorldOverlay;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.NameUtil;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;
import wtf.tatp.meowtils.util.Util;

public final class PearlDetector extends Module {
    private static final String WARNING_MESSAGE = "§4§l !!!";
    @Config public boolean enabled;
    @Config public int key;
    @Config public int red = wtf.tatp.meowtils.module.meowtils.GUI.BLUE_DEFAULT;
    @Config public int green = wtf.tatp.meowtils.module.meowtils.GUI.BLUE_DEFAULT;
    @Config public int blue = wtf.tatp.meowtils.module.meowtils.GUI.BLUE_DEFAULT;
    @Config public float scale = 0.65f;
    @Config public boolean timerSelf = true;
    @Config public boolean positionOthers = true;
    @Config public boolean thrownAlert = true;
    @Config public boolean sound = true;
    @Config public boolean ender = true;
    @Config public boolean corrupt = true;
    @Config public boolean warp = true;
    @Config public int posX = 1;
    @Config public int posY = 1;
    @Config public String timerStartMode = "On message";
    private static final Map<UUID, PearlType> LAST_HELD_PEARL = new HashMap<>();
    private static final List<BoxLocation> ACTIVE_BOXES = new ArrayList<>();
    private static float cooldown;
    private static int tickCounter;
    private static boolean thrown;

    private enum PearlType { NONE, NORMAL, CORRUPT, TIME_WARP }

    public PearlDetector() {
        super("PearlDetector", Category.Skywars);
        tag(ModuleTag.LEGIT);
        tooltip("Detects thrown pearls & displays various information such as warp location\nfor other players and warp timer for yourself.");
        ColorLink color = new ColorLink("red", "green", "blue", this);
        addColor(new ColorValue("Text color", color));
        addSaturation(new SaturationValue(color));
        addBrightness(new BrightnessValue(color));
        addMode(new ModeValue("Timer", List.of("On message", "On click"), "timerStartMode", this));
        addSlider(new SliderValue("Text scale", 0.5, 1.5, 0.05, null, "scale", this, Float.TYPE));
        addToggle(new ToggleValue("Ping sound", "sound", this));
        addToggle(new ToggleValue("Timer for self", "timerSelf", this));
        addToggle(new ToggleValue("Position for others", "positionOthers", this));
        addToggle(new ToggleValue("Thrown pearl alert", "thrownAlert", this));
        addCheck(new CheckValue("§5Ender §fPearl", "ender", this));
        addCheck(new CheckValue("§3Corrupt §fPearl", "corrupt", this));
        addCheck(new CheckValue("§dTime §dWarp §fPearl", "warp", this));
    }

    @Override
    public List<HudEntry> hudEditor() {
        if (!Settings.bool(this, "timerSelf", timerSelf)) return List.of();
        return List.of(new HudEntry(null, this, "posX", "posY", () -> wtf.tatp.meowtils.gui.GuiUtil.getHudBounds("Time Warp: 5 ", 1, (float) Settings.number(this, "scale", scale))));
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.level == null || mc.player == null) return;
        if (mc.gui.screen() != null && !wtf.tatp.meowtils.gui.GuiUtil.inEditor()) return;
        if ((!Skywars.GAME.isActive() && !wtf.tatp.meowtils.gui.GuiUtil.inEditor()) || !Settings.bool(this, "timerSelf", timerSelf)) return;
        if (!thrown && !wtf.tatp.meowtils.gui.GuiUtil.inEditor()) return;
        int color = ColorUtil.rgb(Settings.integer(this, "red", red), Settings.integer(this, "green", green), Settings.integer(this, "blue", blue));
        float drawScale = (float) Settings.number(this, "scale", scale);
        String value = wtf.tatp.meowtils.gui.GuiUtil.inEditor() ? "5.0" : String.format(java.util.Locale.ROOT, "%.1f", cooldown);
        int status = cooldownColor(wtf.tatp.meowtils.gui.GuiUtil.inEditor() ? 5f : cooldown);
        var g = event.getGraphics();
        wtf.tatp.meowtils.font.HudFont.draw(g, "Time Warp: ", posX, posY, drawScale, color);
        wtf.tatp.meowtils.font.HudFont.draw(g, value, posX + wtf.tatp.meowtils.font.HudFont.width("Time Warp: ", drawScale), posY, drawScale, status);
    }

    @EventTarget
    public void onChat(ChatReceivedEvent event) {
        if (Skywars.GAME.isNotActive() || event.isOverlay() || Settings.text(this, "timerStartMode", timerStartMode).equals("On click")) return;
        if (event.getText().equals("You will be warped back in 3 seconds!")) {
            thrown = true;
            cooldown = 3f;
            tickCounter = 0;
        }
    }

    @EventTarget
    public void onUse(PlayerInteractEvent event) {
        if (mc.player == null || Skywars.GAME.isNotActive() || Settings.text(this, "timerStartMode", timerStartMode).equals("On message") || thrown) return;
        if (event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_AIR && event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        ItemStack held = mc.player.getMainHandItem();
        if (ItemIds.is(held, "ender_pearl") && loreContains(held, "Teleport back")) {
            thrown = true;
            cooldown = 3f;
            tickCounter = 0;
        }
    }

    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null || event.getPhase() != ClientTickEvent.Phase.POST) return;
        if (Skywars.GAME.isNotActive() && Skywars.MINI.isNotActive()) return;
        if (thrown && cooldown > 0) {
            tickCounter++;
            if (tickCounter >= 2) {
                cooldown -= 0.1f;
                tickCounter = 0;
                if (cooldown <= 0) { cooldown = 0; thrown = false; }
            }
        }
        for (Player player : mc.level.players()) {
            if (player == mc.player || skipFriend(player)) continue;
            ItemStack held = player.getMainHandItem();
            if (!ItemIds.is(held, "ender_pearl")) continue;
            PearlType type = held.hasFoil() || !held.getEnchantments().isEmpty()
                    ? (loreContains(held, "Teleport back") ? PearlType.TIME_WARP : PearlType.CORRUPT)
                    : PearlType.NORMAL;
            LAST_HELD_PEARL.put(player.getUUID(), type);
        }
    }

    @EventTarget
    public void onPacket(ReceivePacketEvent event) {
        if (!(event.getPacket() instanceof ClientboundAddEntityPacket packet) || packet.getType() != EntityTypes.ENDER_PEARL) return;
        if ((Skywars.GAME.isNotActive() && Skywars.MINI.isNotActive()) || !Settings.bool(this, "positionOthers", positionOthers) || mc.level == null) return;
        Player closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (Player player : mc.level.players()) {
            if (player == mc.player || skipFriend(player)) continue;
            double distance = player.distanceToSqr(packet.getX(), packet.getY(), packet.getZ());
            if (distance < 4 && distance < closestDistance) {
                closestDistance = distance;
                closest = player;
            }
        }
        if (closest != null) alert(closest, LAST_HELD_PEARL.getOrDefault(closest.getUUID(), PearlType.NONE));
    }

    @EventTarget
    public void onWorld(RenderWorldLastEvent event) {
        if (Skywars.GAME.isNotActive() || !Settings.bool(this, "positionOthers", positionOthers) || mc.player == null) return;
        long now = System.currentTimeMillis();
        ACTIVE_BOXES.removeIf(box -> now - box.spawnTime > 4000);
        int color = ColorUtil.rgba(Settings.integer(this, "red", red), Settings.integer(this, "green", green), Settings.integer(this, "blue", blue), 150);
        for (BoxLocation pos : ACTIVE_BOXES) {
            AABB box = new AABB(pos.x - 0.3, pos.y, pos.z - 0.3, pos.x + 0.3, pos.y + 1.8, pos.z + 0.3);
            WorldOverlay.filledBox(event, box, color);
        }
    }

    private void alert(Player player, PearlType type) {
        if (skipFriend(player)) return;
        String name = NameUtil.getTabDisplayName(player.getScoreboardName());
        if (Settings.bool(this, "positionOthers", positionOthers) && type == PearlType.TIME_WARP) {
            ACTIVE_BOXES.add(new BoxLocation(player.getX(), player.getY(), player.getZ(), System.currentTimeMillis()));
        }
        if (!Settings.bool(this, "thrownAlert", thrownAlert)) return;
        String message;
        if (type == PearlType.TIME_WARP && Settings.bool(this, "warp", warp)) message = name + "§7 threw a §dTime Warp Pearl" + WARNING_MESSAGE;
        else if (type == PearlType.CORRUPT && Settings.bool(this, "corrupt", corrupt)) message = name + "§7 threw a §3Corrupt Pearl" + WARNING_MESSAGE;
        else if (type == PearlType.NORMAL && Settings.bool(this, "ender", ender)) message = name + "§7 threw an §5Ender Pearl" + WARNING_MESSAGE;
        else return;
        Meowtils.addMessage(message);
        if (Settings.bool(this, "sound", sound)) Util.playSound(Util.Sound.MEOW, 100);
    }

    private static boolean skipFriend(Player player) {
        return TeamUtil.ignoreFriends(player.getUUID().toString()) || TeamUtil.ignoreFriends(player.getScoreboardName());
    }

    private static boolean loreContains(ItemStack stack, String needle) {
        if (stack == null || stack.isEmpty()) return false;
        String hover = stack.getHoverName().getString();
        if (hover.contains(needle) || ColorUtil.unformattedText(hover).contains(needle)) return true;
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore != null) {
            for (var line : lore.lines()) {
                if (line.getString().contains(needle) || ColorUtil.unformattedText(line.getString()).contains(needle)) return true;
            }
        }
        for (String line : wtf.tatp.meowtils.module.bedwars.BedwarsSupport.loreLines(stack)) {
            if (line.contains(needle) || ColorUtil.unformattedText(line).contains(needle)) return true;
        }
        return false;
    }

    private static int cooldownColor(float value) {
        if (value >= 2.5f) return 0xFF00AA00;
        if (value >= 2f) return 0xFF55FF55;
        if (value >= 1.5f) return 0xFFFFFF55;
        if (value >= 1f) return 0xFFFFAA00;
        if (value >= 0.5f) return 0xFFFF5555;
        return 0xFFAA0000;
    }

    private record BoxLocation(double x, double y, double z, long spawnTime) {}

    @Override
    public void onReset() {
        LAST_HELD_PEARL.clear();
        ACTIVE_BOXES.clear();
        cooldown = 0;
        tickCounter = 0;
        thrown = false;
    }
}
