package wtf.tatp.meowtils.module.render;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.AABB;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.RenderWorldLastEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.OpacityValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.util.Settings;

/** Highlights arrows, fireballs, and ender pearls. */
public final class Indicators extends Module {
    @Config public float opacity = 50;
    @Config public String mode = "3D", render = "Full";
    @Config public boolean arrow = true, fireball = true, pearl = true;
    @Config public float arrowExpand, fireballExpand, pearlExpand;

    public Indicators() {
        super("Indicators", Category.Render);
        addMode(new ModeValue("Mode", List.of("3D", "2D"), "mode", this));
        addMode(new ModeValue("Render", List.of("Full", "Outline"), "render", this));
        addOpacity(new OpacityValue("Opacity", "opacity", this));
        addCheck(new CheckValue("Arrows", "arrow", this));
        addSlider(new SliderValue("Arrow expand", 0.0, 1.0, 0.1, "x", "arrowExpand", this, Float.class));
        addCheck(new CheckValue("§cFireballs", "fireball", this));
        addSlider(new SliderValue("Fireball expand", 0.0, 1.0, 0.1, "x", "fireballExpand", this, Float.class));
        addCheck(new CheckValue("§5Ender Pearls", "pearl", this));
        addSlider(new SliderValue("Pearl expand", 0.0, 1.0, 0.1, "x", "pearlExpand", this, Float.class));
        tag(ModuleTag.LEGIT);
        tooltip("Highlights dangerous projectiles.");
    }

    @EventTarget
    public void onWorld(RenderWorldLastEvent event) {
        if (mc.player == null || mc.level == null || !"3D".equals(Settings.text(this, "mode", "3D"))) return;
        boolean fill = WorldOverlay.wantsFill(this, "render", "Full");
        float partial = WorldOverlay.partialTick();
        for (Entity entity : mc.level.entitiesForRendering()) {
            int color = colorOf(entity);
            if (color == 0) continue;
            AABB box = WorldOverlay.interpolated(entity, partial).inflate(expandOf(entity));
            if (fill) WorldOverlay.filledBox(event, box, color);
            else WorldOverlay.outline(event, box, color, 1.5f);
        }
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.player == null || mc.level == null || !"2D".equals(Settings.text(this, "mode", "3D"))) return;
        var graphics = event.getGraphics();
        float[] min = new float[2];
        float[] max = new float[2];
        float partial = event.getDelta().getGameTimeDeltaPartialTick(false);
        for (Entity entity : mc.level.entitiesForRendering()) {
            int color = colorOf(entity);
            if (color == 0) continue;
            AABB box = WorldOverlay.interpolated(entity, partial).inflate(expandOf(entity));
            if (!WorldOverlay.projectBox(box, graphics.guiWidth(), graphics.guiHeight(), min, max)) continue;
            int x = Math.round(min[0]), y = Math.round(min[1]), w = Math.max(2, Math.round(max[0] - min[0])), h = Math.max(2, Math.round(max[1] - min[1]));
            WorldOverlay.hudBox(graphics, x, y, w, h, color, WorldOverlay.wantsFill(this, "render", "Full"));
        }
    }

    private int colorOf(Entity entity) {
        float alpha = WorldOverlay.opacity01(this, "opacity", 50);
        if (entity instanceof AbstractArrow && Settings.bool(this, "arrow", true)) return WorldOverlay.withAlpha(0xFFFFFF, alpha);
        if ((entity instanceof Fireball || entity instanceof DragonFireball) && Settings.bool(this, "fireball", true)) return WorldOverlay.withAlpha(0xAA0000, alpha);
        if (entity instanceof ThrownEnderpearl && Settings.bool(this, "pearl", true)) return WorldOverlay.withAlpha(0xAA00AA, alpha);
        return 0;
    }

    private double expandOf(Entity entity) {
        if (entity instanceof AbstractArrow) return Settings.number(this, "arrowExpand", 0);
        if (entity instanceof Fireball || entity instanceof DragonFireball) return Settings.number(this, "fireballExpand", 0);
        if (entity instanceof ThrownEnderpearl) return Settings.number(this, "pearlExpand", 0);
        return 0;
    }

}
