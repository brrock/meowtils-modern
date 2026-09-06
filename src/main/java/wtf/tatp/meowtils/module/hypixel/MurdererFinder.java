package wtf.tatp.meowtils.module.hypixel;

import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.RenderWorldLastEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.session.MurderMystery;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.module.render.WorldOverlay;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;
import wtf.tatp.meowtils.util.Util;

public final class MurdererFinder extends Module {
    private static final int MURDERER_COLOR = 0x50AA0000;
    private static final int DETECTIVE_COLOR = 0x50FFAA00;
    private static final ArrayList<UUID> MURDERERS = new ArrayList<>();
    private static final ArrayList<UUID> DETECTIVES = new ArrayList<>();

    public MurdererFinder() {
        super("MurdererFinder", Category.Hypixel);
        tag(ModuleTag.SAFE);
        tooltip("Highlights the murderer and alerts for other relevant roles in Murder Mystery.");
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null || event.getPhase() != ClientTickEvent.Phase.POST
                || Server.HYPIXEL.isNotActive() || MurderMystery.ALL.isNotActive()) {
            return;
        }
        boolean findMurderers = Settings.bool(this, "findMurderers", true);
        boolean findDetectives = Settings.bool(this, "findDetectives", true);
        for (Player player : mc.level.players()) {
            if (player == null || player == mc.player || TeamUtil.isBot(player)) continue;
            ItemStack held = player.getMainHandItem();
            if (held == null || held.isEmpty()) continue;
            UUID uuid = player.getUUID();
            int distanceToEntity = (int) player.distanceTo(mc.player);
            String distance = ChatFormatting.GRAY + " (" + ChatFormatting.AQUA + distanceToEntity + "m" + ChatFormatting.GRAY + ")";
            if (findMurderers && !MURDERERS.contains(uuid) && !DETECTIVES.contains(uuid) && isMurdererItem(held)) {
                MURDERERS.add(uuid);
                alert(ChatFormatting.DARK_RED.toString() + ChatFormatting.BOLD + "Murderer " + ChatFormatting.GRAY + "found: "
                        + ChatFormatting.LIGHT_PURPLE + player.getGameProfile().name() + distance);
            }
            if (findDetectives && !MURDERERS.contains(uuid) && !DETECTIVES.contains(uuid) && "bow".equals(ItemIds.id(held))) {
                DETECTIVES.add(uuid);
                alert(ChatFormatting.GOLD.toString() + ChatFormatting.BOLD + "Detective " + ChatFormatting.GRAY + "found: "
                        + ChatFormatting.LIGHT_PURPLE + player.getGameProfile().name() + distance);
            }
        }
    }

    @EventTarget
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!ready() || "None".equals(Settings.text(this, "render", "Full"))) return;
        if (!"3D".equals(Settings.text(this, "mode", "3D"))) return;
        boolean fill = WorldOverlay.wantsFill(this, "render", "Full");
        float partial = WorldOverlay.partialTick();
        for (Player player : mc.level.players()) {
            int color = colorOf(player);
            if (color == 0) continue;
            var box = WorldOverlay.interpolated(player, partial).inflate(0.1);
            if (fill) WorldOverlay.filledBox(event, box, color);
            else WorldOverlay.outline(event, box, color, 1.5f);
        }
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (!ready() || "None".equals(Settings.text(this, "render", "Full"))) return;
        if (!"2D".equals(Settings.text(this, "mode", "3D"))) return;
        boolean fill = WorldOverlay.wantsFill(this, "render", "Full");
        var graphics = event.getGraphics();
        float[] min = new float[2], max = new float[2];
        float partial = event.getDelta().getGameTimeDeltaPartialTick(false);
        for (Player player : mc.level.players()) {
            int color = colorOf(player);
            if (color == 0) continue;
            var box = WorldOverlay.interpolated(player, partial).inflate(0.1);
            if (!WorldOverlay.projectBox(box, graphics.guiWidth(), graphics.guiHeight(), min, max)) continue;
            int x = Math.round(min[0]), y = Math.round(min[1]);
            int w = Math.max(2, Math.round(max[0] - min[0])), h = Math.max(2, Math.round(max[1] - min[1]));
            WorldOverlay.hudBox(graphics, x, y, w, h, color, fill);
        }
    }

    private boolean ready() {
        return mc.player != null && mc.level != null && Server.HYPIXEL.isActive() && MurderMystery.ALL.isActive();
    }

    private int colorOf(Player player) {
        if (player == null || player == mc.player || TeamUtil.isBot(player)) return 0;
        UUID uuid = player.getUUID();
        return MURDERERS.contains(uuid) ? MURDERER_COLOR : DETECTIVES.contains(uuid) ? DETECTIVE_COLOR : 0;
    }

    private void alert(String msg) {
        if (Settings.bool(this, "chatAlerts", true)) Meowtils.addMessage(msg);
        if (Settings.bool(this, "sound", true)) Util.playSound(Util.Sound.PING_MEDIUM, 100);
    }

    private static boolean isMurdererItem(ItemStack stack) {
        return ItemIds.is(stack,
                "iron_sword", "chest", "ender_chest", "stone_sword", "stick", "wooden_axe", "wooden_sword",
                "dead_bush", "sugar_cane", "stone_shovel", "blaze_rod", "diamond_hoe", "quartz", "pumpkin_pie",
                "golden_hoe", "leather", "name_tag", "coal", "flint", "bone", "carrot", "golden_carrot", "cookie",
                "diamond_axe", "sunflower", "lilac", "rose_bush", "peony", "prismarine_shard", "cooked_beef",
                "nether_brick", "cooked_chicken", "music_disc", "golden_sword", "diamond_sword", "shears", "cod",
                "bread", "oak_boat", "glistering_melon", "book", "sapling", "golden_axe", "diamond_pickaxe",
                "golden_shovel", "golden_pickaxe", "ink_sac");
    }

    public static boolean isMurderer(UUID uuid) { return MURDERERS.contains(uuid); }
    public static boolean isDetective(UUID uuid) { return DETECTIVES.contains(uuid); }

    @Override
    public void onReset() {
        MURDERERS.clear();
        DETECTIVES.clear();
    }
}
