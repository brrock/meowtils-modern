package wtf.tatp.meowtils.module.render;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.PlayerInteractEvent;
import wtf.tatp.meowtils.event.RenderWorldLastEvent;
import wtf.tatp.meowtils.event.WorldEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ButtonValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.util.Settings;

/** Chest outlines, with opened-chest tracking from right-clicks. */
public final class ChestESP extends Module {
    @Config public int red = 255, green = 255, blue = 255, redOpen = 255, greenOpen = 0, blueOpen = 0;
    @Config public String mode = "Normal", render = "Full";
    @Config public boolean skywarsOnly;
    private static final List<BlockPos> OPENED = new CopyOnWriteArrayList<>();

    public ChestESP() {
        super("ChestESP", Category.Render);
        addMode(new ModeValue("Mode", List.of("Opened", "Normal", "Both"), "mode", this));
        addMode(new ModeValue("Render", List.of("Full", "Outline"), "render", this));
        ColorLink color = new ColorLink("red", "green", "blue", this);
        ColorLink opened = new ColorLink("redOpen", "greenOpen", "blueOpen", this);
        addColor(new ColorValue("Chest color", color));
        addSaturation(new wtf.tatp.meowtils.gui.values.SaturationValue(color));
        addBrightness(new wtf.tatp.meowtils.gui.values.BrightnessValue(color));
        addColor(new ColorValue("Opened color", opened));
        addToggle(new ToggleValue("Skywars only", "skywarsOnly", this));
        addButton(new ButtonValue("Reset opened", 5.0f, ChestESP::resetOpened));
        tag(ModuleTag.SAFE);
        tooltip("Highlights chests");
    }

    @EventTarget
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || event.getPlayer() != mc.player || mc.level == null) return;
        BlockPos pos = event.getPos();
        if (mc.level.getBlockEntity(pos) instanceof ChestBlockEntity && !OPENED.contains(pos)) OPENED.add(pos.immutable());
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        if (event.getType() == WorldEvent.Type.UNLOAD) OPENED.clear();
    }

    @EventTarget
    public void onRender(RenderWorldLastEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (Settings.bool(this, "skywarsOnly", false) && Skywars.GAME.isNotActive() && Skywars.MINI.isNotActive()) return;
        String mode = Settings.text(this, "mode", "Normal");
        boolean fill = WorldOverlay.wantsFill(this, "render", "Full");
        Set<BlockEntity> tiles = mc.level.getGloballyRenderedBlockEntities();
        for (BlockEntity tile : tiles) {
            if (!(tile instanceof ChestBlockEntity)) continue;
            BlockPos pos = tile.getBlockPos();
            boolean opened = OPENED.contains(pos);
            if ("Opened".equals(mode) && !opened) continue;
            int color = "Both".equals(mode) && opened
                    ? WorldOverlay.rgb(Settings.integer(this, "redOpen", 255), Settings.integer(this, "greenOpen", 0), Settings.integer(this, "blueOpen", 0))
                    : WorldOverlay.color(this);
            var shape = tile.getBlockState().getShape(mc.level, pos);
            if (shape.isEmpty()) continue;
            AABB box = shape.bounds().move(pos).inflate(0.06);
            if (fill) WorldOverlay.filledBox(event, box, WorldOverlay.withAlpha(color, 120 / 255.0f));
            else WorldOverlay.outline(event, box, color, 1.5f);
        }
    }

    public static int tesrColor(BlockPos pos) {
        ChestESP module = get(ChestESP.class);
        if (module == null || !module.getState() || module.mc.level == null) return 0;
        if (WorldOverlay.wantsFill(module, "render", "Full")) return 0;
        if (Settings.bool(module, "skywarsOnly", false) && Skywars.GAME.isNotActive() && Skywars.MINI.isNotActive()) return 0;
        String mode = Settings.text(module, "mode", "Normal");
        boolean opened = OPENED.contains(pos);
        if ("Opened".equals(mode) && !opened) return 0;
        return "Both".equals(mode) && opened
                ? WorldOverlay.rgb(Settings.integer(module, "redOpen", 255), Settings.integer(module, "greenOpen", 0), Settings.integer(module, "blueOpen", 0))
                : WorldOverlay.color(module);
    }

    private static void resetOpened() {
        OPENED.clear();
        if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) Meowtils.addMessage("Reset opened chest positions.");
        if (Notifications.getMode() != Notifications.Mode.CHAT) NotificationManager.show("ChestESP", "Reset positions.", NotificationManager.Type.INFO, 1000L);
    }
}
