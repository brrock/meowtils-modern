package wtf.tatp.meowtils.module.render;

import java.util.List;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.hudeditor.HudEntry;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.Settings;

/** HUD countdown while eating/drinking short-use items. */
public final class ConsumeTimer extends Module {
    @Config public int red = 255, green = 255, blue = 255, posX = 1, posY = 1;
    @Config public float scale = 0.65f;
    @Config public String mode = "Seconds";
    @Config public boolean dynamicColor;
    private boolean usingItem;
    private int useTicksLeft;

    public ConsumeTimer() {
        super("ConsumeTimer", Category.Render);
        ColorLink color = new ColorLink("red", "green", "blue", this);
        addColor(new ColorValue("Text color", color));
        addSaturation(new SaturationValue(color));
        addBrightness(new BrightnessValue(color));
        addSlider(new SliderValue("Scale", 0.5, 1.5, 0.05, null, "scale", this, Float.TYPE));
        addMode(new ModeValue("Mode", List.of("Ticks", "Seconds"), "mode", this));
        addToggle(new ToggleValue("Dynamic color", "dynamicColor", this));
        tag(ModuleTag.LEGIT);
        tooltip("Displays a countdown of how much time left to consume an item.");
    }

    @Override
    public List<HudEntry> hudEditor() {
        return List.of(new HudEntry(null, this, "posX", "posY", () -> wtf.tatp.meowtils.gui.GuiUtil.getHudBounds("30", 1, (float) Settings.number(this, "scale", 0.65))));
    }

    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        if (!mc.player.isUsingItem() || mc.player.getUseItem().isEmpty()) {
            usingItem = false;
            useTicksLeft = 0;
            return;
        }
        int max = mc.player.getUseItem().getUseDuration(mc.player);
        if (max <= 0 || max > 32) {
            usingItem = false;
            useTicksLeft = 0;
            return;
        }
        usingItem = true;
        useTicksLeft = Math.max(0, mc.player.getUseItemRemainingTicks());
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.player == null || mc.level == null) return;
        boolean preview = wtf.tatp.meowtils.gui.GuiUtil.inEditor();
        if ((!usingItem || useTicksLeft <= 0) && !preview) return;
        int ticks = preview && useTicksLeft <= 0 ? 16 : useTicksLeft;
        String text = "Ticks".equals(Settings.text(this, "mode", "Seconds")) ? Integer.toString(ticks) : String.format("%.1f", ticks / 20.0).replace(',', '.');
        int color = timerColor(ticks);
        float drawScale = (float) Settings.number(this, "scale", 0.65);
        wtf.tatp.meowtils.font.HudFont.draw(event.getGraphics(), text, posX, posY, drawScale, color);
    }

    private int timerColor(int ticks) {
        if (!Settings.bool(this, "dynamicColor", false)) return WorldOverlay.color(this);
        if (ticks < 6) return 0xFFAA0000;
        if (ticks < 12) return 0xFFFF5555;
        if (ticks < 18) return 0xFFFFAA00;
        if (ticks < 24) return 0xFFFFFF55;
        return WorldOverlay.color(this);
    }
}
