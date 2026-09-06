package wtf.tatp.meowtils.module.bedwars;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.module.meowtils.GUI;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;

/** Locates your bed, warns when enemies approach, and shows distance on HUD. */
public final class BedTracker extends Module {
    @wtf.tatp.meowtils.config.Config public int red = GUI.BLUE_DEFAULT, green = GUI.BLUE_DEFAULT, blue = GUI.BLUE_DEFAULT;
    @wtf.tatp.meowtils.config.Config public float scale = 0.65f;
    @wtf.tatp.meowtils.config.Config public int posX = 1, posY = 1;
    private static final String CROSS_ICON = "§c✗";
    private static final String CHECK_ICON = "§a✓";
    private static BlockPos bedPos;
    private static long bedScanTime;
    private static long startTime;
    private static boolean rangeAlert;
    private static final Map<UUID, Long> LAST_ALERT_TIME = new HashMap<>();

    public BedTracker() {
        super("BedTracker", Category.Bedwars);
        tag(ModuleTag.SAFE);
        tooltip("Alerts when enemy players are near your bed and shows distance to your bed on screen.");
        ColorLink color = new ColorLink("red", "green", "blue", this);
        addColor(new ColorValue("Text color", color));
        addSaturation(new SaturationValue(color));
        addBrightness(new BrightnessValue(color));
        addSlider(new SliderValue("Scale", 0.5, 1.5, 0.05, null, "scale", this, Float.TYPE));
        addSlider(new SliderValue("Alert frequency", 5, 30, 1, "s", "frequency", this, Integer.TYPE));
        addSlider(new SliderValue("Max distance", 10, 100, 5, "m", "distance", this, Integer.TYPE));
        addToggle(new ToggleValue("Ping sound", "sound", this));
        addToggle(new ToggleValue("Show HUD", "hud", this));
    }

    @EventTarget
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.isOverlay() || !BedwarsSupport.inMatch()) return;
        String msg = event.getText();
        String plain = ColorUtil.plainLower(msg);
        if (!plain.contains(":")) {
            if (plain.contains("the game starts in 1 second")) {
                bedPos = null;
                bedScanTime = System.currentTimeMillis() + 6000;
                startTime = System.currentTimeMillis() + 7000;
                Meowtils.debugMessage("§e[BedTracker]: §fLocating bed..");
            } else if (plain.equals("you will respawn in 6 seconds!")) {
                bedPos = null;
                bedScanTime = System.currentTimeMillis() + 9000;
                startTime = System.currentTimeMillis() + 10000;
                Meowtils.debugMessage("§e[BedTracker]: §fLocating bed..");
            }
        }
        if (plain.contains("bed destruction") && plain.contains("your bed") && !plain.contains(":")) {
            bedPos = null;
            Meowtils.addMessage("§4§l⚠ Your bed was destroyed!");
        }
        if (plain.contains("your team swapped and you are now:")) {
            bedPos = null;
            bedScanTime = System.currentTimeMillis() + 1000;
            startTime = System.currentTimeMillis() + 1000;
        }
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        if (!BedwarsSupport.inMatch()) return;
        if (bedPos == null && bedScanTime > 0 && System.currentTimeMillis() > bedScanTime) {
            bedPos = findNearbyBed(mc.level, mc.player.blockPosition(), 25);
            bedScanTime = 0L;
            if (bedPos != null) {
                Meowtils.addMessage("§a§l✓ §rWhitelisted your bed at§7 (§a" + bedPos.getX() + ", " + bedPos.getY() + ", " + bedPos.getZ() + "§7)");
            } else {
                Meowtils.addMessage("§c⚠ Error locating your bed.");
            }
        }
        if (bedPos != null && isBedOutOfRange() && !rangeAlert) {
            Meowtils.addMessage("§l§d⚠ Your bed is out of range!");
            rangeAlert = true;
        } else if (!isBedOutOfRange() && bedPos != null) {
            rangeAlert = false;
        }
        if (bedPos == null) return;
        long currentTime = System.currentTimeMillis();
        if (currentTime - startTime < 6000) return;
        int maxDistance = Settings.integer(this, "distance", 50);
        long frequency = (long) Settings.integer(this, "frequency", 10) * 1000;
        for (Player player : mc.level.players()) {
            if (BedwarsSupport.skipPlayer(player, true) || skipFriend(player) || player.getAbilities().flying || player.tickCount < 100) continue;
            int distanceToBed = (int) player.position().distanceTo(Vec3.atCenterOf(bedPos));
            if (distanceToBed > maxDistance) continue;
            long lastAlert = LAST_ALERT_TIME.getOrDefault(player.getUUID(), 0L);
            if (currentTime - lastAlert < frequency) continue;
            String distanceColor = distanceToBed <= 5 ? "§4" : distanceToBed <= 15 ? "§c" : distanceToBed <= 30 ? "§6" : distanceToBed <= 40 ? "§e" : "§a";
            Meowtils.addMessage(BedwarsSupport.displayName(player) + "§f is " + distanceColor + distanceToBed + "§f blocks from your bed!" + distanceColor + " ⚠");
            LAST_ALERT_TIME.put(player.getUUID(), currentTime);
            if (Settings.bool(this, "sound", true)) BedwarsSupport.play(BedwarsSupport.Sound.MEOW);
        }
        LAST_ALERT_TIME.keySet().removeIf(uuid -> mc.level.players().stream().noneMatch(player -> player.getUUID().equals(uuid)));
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (!BedwarsSupport.inMatch() && !BedwarsSupport.inEditor()) return;
        if (mc.gui.screen() != null && !BedwarsSupport.inEditor()) return;
        if (!Settings.bool(this, "hud", true)) return;
        String distanceColor = getDistanceToBed() < 70 ? "§a" : isBedOutOfRange() ? "§c" : "§e";
        String separator = bedPos != null ? "§7 | §r" : "";
        String bedState = bedPos == null ? CROSS_ICON : CHECK_ICON;
        String distance = bedPos == null ? "" : "Distance: " + distanceColor + getDistanceToBed();
        String warn = isBedOutOfRange() && bedPos != null ? "§c ⚠" : "";
        BedwarsSupport.drawHud(event.getGraphics(), "Bed: " + bedState + separator + distance + warn, posX, posY,
                (float) Settings.number(this, "scale", scale),
                BedwarsSupport.rgb(Settings.integer(this, "red", red), Settings.integer(this, "green", green), Settings.integer(this, "blue", blue)));
    }

    private static boolean skipFriend(Player player) {
        return TeamUtil.ignoreFriends(player.getUUID().toString()) || TeamUtil.ignoreFriends(player.getGameProfile().name());
    }

    private static BlockPos findNearbyBed(Level world, BlockPos center, int radius) {
        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int y = center.getY() - radius; y <= center.getY() + radius; y++) {
                for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (BedwarsSupport.isBed(world.getBlockState(pos))) return pos;
                }
            }
        }
        return null;
    }

    private int getDistanceToBed() {
        if (bedPos == null || mc.player == null) return 0;
        return (int) mc.player.position().distanceTo(new Vec3(bedPos.getX() + 0.5, bedPos.getY(), bedPos.getZ() + 0.5));
    }

    private boolean isBedOutOfRange() {
        if (bedPos == null || mc.player == null || mc.level == null) return true;
        boolean serverOutOfRange = !mc.level.isLoaded(bedPos);
        int renderDistanceBlocks = mc.options.getEffectiveRenderDistance() * 16;
        double dx = mc.player.getX() - bedPos.getX();
        double dz = mc.player.getZ() - bedPos.getZ();
        return serverOutOfRange || Math.sqrt(dx * dx + dz * dz) > renderDistanceBlocks;
    }

    @Override
    public List<wtf.tatp.meowtils.gui.hudeditor.HudEntry> hudEditor() {
        if (Settings.bool(this, "hud", true)) {
            return List.of(new wtf.tatp.meowtils.gui.hudeditor.HudEntry(null, this, "posX", "posY",
                    () -> wtf.tatp.meowtils.gui.GuiUtil.getHudBounds("Bed: X   Distance: 999 X", 1, (float) Settings.number(this, "scale", scale))));
        }
        return List.of();
    }

    @Override
    public void onReset() {
        bedPos = null;
        bedScanTime = 0L;
        startTime = 0L;
        LAST_ALERT_TIME.clear();
    }
}
