package wtf.tatp.meowtils.module.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.AttackEntityEvent;
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
import wtf.tatp.meowtils.util.Settings;

/** Floating damage numbers after a successful hit. */
public final class DamageTags extends Module {
    @Config public int red = 255, green = 255, blue = 255;
    @Config public float scale = 0.65f, expireTime = 2.0f;
    @Config public boolean dynamicColor = true, fadeOut, suffix = true;
    private final Map<Player, Float> health = new HashMap<>();
    private final List<Indicator> indicators = new ArrayList<>();
    private Player pending;
    private float preHealth;
    private int wait = -1;

    public DamageTags() {
        super("DamageTags", Category.Render);
        ColorLink color = new ColorLink("red", "green", "blue", this);
        addColor(new ColorValue("Text color", color));
        addSaturation(new SaturationValue(color));
        addBrightness(new BrightnessValue(color));
        addSlider(new SliderValue("Scale", 0.5, 1.5, 0.05, null, "scale", this, Float.TYPE));
        addSlider(new SliderValue("Expire", 0.5, 5.0, 0.1, "s", "expireTime", this, Float.TYPE));
        addToggle(new ToggleValue("Dynamic color", "dynamicColor", this));
        addToggle(new ToggleValue("Fade out", "fadeOut", this));
        addToggle(new ToggleValue("Show suffix", "suffix", this));
        tag(ModuleTag.LEGIT);
        tooltip("Displays damage dealt after hitting someone.");
    }

    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        health.keySet().removeIf(player -> !mc.level.players().contains(player));
        for (Player player : mc.level.players()) {
            if (player != mc.player) health.putIfAbsent(player, player.getHealth());
        }
        if (pending != null && --wait <= 0) {
            float post = pending.getHealth();
            float damage = preHealth - post;
            if (damage > 0.0f) {
                indicators.add(new Indicator(pending.getX(), pending.getY() + pending.getEyeHeight() - 0.3, pending.getZ(), damage, System.currentTimeMillis()));
                health.put(pending, post);
            }
            pending = null;
        }
    }

    @EventTarget
    public void onAttack(AttackEntityEvent event) {
        if (!(event.getTarget() instanceof Player target) || pending != null) return;
        pending = target;
        preHealth = health.getOrDefault(target, target.getHealth());
        wait = 1;
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.player == null || mc.level == null) return;
        long now = System.currentTimeMillis();
        float lifespan = (float) Settings.number(this, "expireTime", 2.0) * 1000.0f;
        Iterator<Indicator> iterator = indicators.iterator();
        var graphics = event.getGraphics();
        float[] screen = new float[2];
        while (iterator.hasNext()) {
            Indicator indicator = iterator.next();
            float elapsed = now - indicator.time;
            if (elapsed > lifespan) {
                iterator.remove();
                continue;
            }
            if (!WorldOverlay.project(indicator.x, indicator.y, indicator.z, graphics.guiWidth(), graphics.guiHeight(), screen)) continue;
            int alpha = 255;
            if (Settings.bool(this, "fadeOut", false)) alpha = Math.max(0, Math.round(150.0f * (1.0f - elapsed / lifespan)));
            String text = String.format("%.1f", indicator.damage).replace(',', '.') + (Settings.bool(this, "suffix", true) ? "❤" : "");
            int color = Settings.bool(this, "dynamicColor", true) ? WorldOverlay.withAlpha(damageColor(indicator.damage), alpha / 255.0f)
                    : WorldOverlay.rgba(Settings.integer(this, "red", 255), Settings.integer(this, "green", 255), Settings.integer(this, "blue", 255), alpha);
            float tagScale = (float) Settings.number(this, "scale", 0.65);
            wtf.tatp.meowtils.font.HudFont.draw(graphics, text, screen[0] - wtf.tatp.meowtils.font.HudFont.width(text, tagScale) / 2f, screen[1], tagScale, color);
        }
    }

    private static int damageColor(float damage) {
        if (damage < 1.0f) return 0xFF55FF55;
        if (damage < 2.0f) return 0xFF00AA00;
        if (damage < 4.0f) return 0xFFFFFF55;
        if (damage < 6.0f) return 0xFFFF5555;
        return 0xFFAA0000;
    }

    @Override
    public void onReset() {
        health.clear();
        indicators.clear();
        pending = null;
        wait = -1;
    }

    private record Indicator(double x, double y, double z, float damage, long time) {}
}
