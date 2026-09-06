package wtf.tatp.meowtils.module.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.RenderWorldLastEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.OpacityValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Duels;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.util.Settings;

/**
 * Original 2.0.1 ItemESP: grouped drops, world-space 3D/2D boxes, nametag labels.
 * HUD projection is not used — auto-scale is a world-space multiplier so perspective
 * keeps distant text readable instead of blowing it up on the HUD.
 */
public final class ItemESP extends Module {
    @Config public float textScale = 0.65f, opacity = 25;
    @Config public String mode = "3D", display = "Both", render = "Full", font = "Normal";
    @Config public boolean compactText = true, autoScale = true, stackSize = true, showDistance = true, dynamicColor, bedwarsOnly;
    @Config public boolean iron = true, gold = true, diamond = true, emerald = true;

    public ItemESP() {
        super("ItemESP", Category.Render);
        addMode(new ModeValue("Mode", List.of("3D", "2D"), "mode", this));
        addMode(new ModeValue("Render", List.of("Full", "Outline"), "render", this));
        addMode(new ModeValue("Display", List.of("Both", "Box", "Text"), "display", this));
        addMode(new ModeValue("Font", List.of("Normal", "Smooth"), "font", this));
        addSlider(new SliderValue("Text scale", 0.5, 1.5, 0.05, null, "textScale", this, Float.class));
        addOpacity(new OpacityValue("Box opacity", "opacity", this));
        addToggle(new ToggleValue("Auto-scale", "autoScale", this));
        addToggle(new ToggleValue("Show stack size", "stackSize", this));
        addToggle(new ToggleValue("Show distance", "showDistance", this));
        addToggle(new ToggleValue("Compact text", "compactText", this));
        addToggle(new ToggleValue("Dynamic text color", "dynamicColor", this));
        addToggle(new ToggleValue("Bedwars only", "bedwarsOnly", this));
        addCheck(new CheckValue("Iron §7Items", "iron", this));
        addCheck(new CheckValue("§6Gold §7Items", "gold", this));
        addCheck(new CheckValue("§bDiamond §7Items", "diamond", this));
        addCheck(new CheckValue("§2Emerald §7Items", "emerald", this));
        tag(ModuleTag.SAFE);
        tooltip("Highlights important dropped items.");
    }

    @EventTarget
    public void onWorld(RenderWorldLastEvent event) {
        if (!ready()) return;
        boolean showBox = showBox();
        boolean showText = showText();
        if (!showBox && !showText) return;
        boolean threeD = "3D".equals(Settings.text(this, "mode", "3D"));
        boolean fill = "Full".equals(Settings.text(this, "render", "Full"));
        float partial = WorldOverlay.partialTick();
        for (var entry : grouped().entrySet()) {
            ItemEntity item = entry.getValue().item;
            int merged = entry.getValue().count;
            Vec3 pos = item.getPosition(partial);
            double distance = mc.player.position().distanceTo(pos);
            float expand = Settings.bool(this, "autoScale", true) ? (float) Math.max(0.1, distance / 100.0) : 0.1f;
            AABB box = WorldOverlay.interpolated(item, partial).inflate(expand);
            int color = colorOf(item.getItem());
            if (showBox) {
                if (threeD) WorldOverlay.drawBox(event, box, color, fill);
                else {
                    if (fill) {
                        int fillColor = WorldOverlay.rgba((color >> 16) & 255, (color >> 8) & 255, color & 255, Math.max(1, ((color >>> 24) & 255) / 2));
                        WorldOverlay.billboard(event, box, fillColor, true);
                    }
                    WorldOverlay.billboard(event, box, WorldOverlay.withAlpha(color, 1f), false);
                }
            }
            if (showText) {
                float scale = (float) Settings.number(this, "textScale", 0.65);
                if (Settings.bool(this, "autoScale", true)) {
                    scale *= (float) Math.max(1.0, distance / (Settings.bool(this, "compactText", true) ? 6.0 : 10.0));
                }
                int textColor = Settings.bool(this, "dynamicColor", false) ? colorOf(item.getItem()) | 0xFF000000 : 0xFFFFFFFF;
                WorldOverlay.worldLabel(event, pos.x, pos.y + 0.4, pos.z, label(item.getItem(), merged, distance), scale, textColor,
                        Settings.bool(this, "compactText", true));
            }
        }
    }

    private boolean ready() {
        if (mc.level == null || mc.player == null) return false;
        return !Settings.bool(this, "bedwarsOnly", false) || Bedwars.GAME.isActive();
    }

    private boolean showBox() {
        String display = Settings.text(this, "display", "Both");
        return "Box".equals(display) || "Both".equals(display);
    }

    private boolean showText() {
        String display = Settings.text(this, "display", "Both");
        return "Text".equals(display) || "Both".equals(display);
    }

    private Map<ItemGroup, Grouped> grouped() {
        Map<ItemGroup, Grouped> render = new HashMap<>();
        boolean hypixelDelay = Server.HYPIXEL.isActive() && (Bedwars.GAME.isActive() || Duels.BEDWARS.isActive());
        for (var entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity item) || (hypixelDelay && item.getAge() < 10)) continue;
            ItemStack stack = item.getItem();
            if (!shouldHighlight(stack)) continue;
            ItemGroup group = new ItemGroup(item);
            Grouped existing = render.get(group);
            if (existing == null) {
                render.put(group, new Grouped(item, stack.getCount()));
            } else if (priority(stack) > priority(existing.item.getItem())) {
                render.put(group, new Grouped(item, stack.getCount()));
            } else if (priority(stack) == priority(existing.item.getItem())) {
                existing.count += stack.getCount();
            }
        }
        return render;
    }

    private String label(ItemStack stack, int merged, double distance) {
        if (Settings.bool(this, "compactText", true)) return Integer.toString(merged);
        String text = stack.getHoverName().getString();
        if (Settings.bool(this, "stackSize", true) && merged > 1) text += "§7 x" + merged;
        if (Settings.bool(this, "showDistance", true)) text += "§7 [" + ((int) distance) + "m]";
        return text;
    }

    private int colorOf(ItemStack stack) {
        int alpha = Math.round(WorldOverlay.opacity01(this, "opacity", 25) * 255);
        if (ItemEspItems.emerald(stack)) return WorldOverlay.rgba(0, 170, 0, alpha);
        if (ItemEspItems.diamond(stack)) return WorldOverlay.rgba(85, 255, 255, alpha);
        if (ItemEspItems.gold(stack)) return WorldOverlay.rgba(255, 170, 0, alpha);
        return WorldOverlay.rgba(255, 255, 255, alpha);
    }

    private boolean shouldHighlight(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (Settings.bool(this, "iron", true) && ItemEspItems.iron(stack)) return true;
        if (Settings.bool(this, "gold", true) && ItemEspItems.gold(stack)) return true;
        if (Settings.bool(this, "diamond", true) && ItemEspItems.diamond(stack)) return true;
        return Settings.bool(this, "emerald", true) && ItemEspItems.emerald(stack);
    }

    private int priority(ItemStack stack) {
        if (ItemEspItems.emerald(stack)) return 4;
        if (ItemEspItems.diamond(stack)) return 3;
        if (ItemEspItems.gold(stack)) return 2;
        return ItemEspItems.iron(stack) ? 1 : 0;
    }

    private static final class Grouped {
        final ItemEntity item;
        int count;
        Grouped(ItemEntity item, int count) { this.item = item; this.count = count; }
    }

    private static final class ItemGroup {
        final int x, y, z;
        ItemGroup(ItemEntity entity) {
            this.x = (int) entity.getX();
            this.y = (int) entity.getY();
            this.z = (int) entity.getZ();
        }
        @Override public boolean equals(Object object) {
            return object instanceof ItemGroup other && x == other.x && y == other.y && z == other.z;
        }
        @Override public int hashCode() { return 31 * (31 * x + y) + z; }
    }
}
