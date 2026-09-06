package wtf.tatp.meowtils.module.render;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.OpacityValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.mixin.MultiPlayerGameModeAccessor;
import wtf.tatp.meowtils.util.Settings;

/** Block-break countdown projected from the looked-at block. */
public final class BreakProgress extends Module {
    @Config public String mode = "Percentage";
    @Config public int red = 255, green = 255, blue = 255;
    @Config public float scale = 0.65f;
    @Config public float opacity = 100;
    @Config public boolean dynamicColor;
    private float progress;
    private BlockPos block;
    private String progressStr = "";

    public BreakProgress() {
        super("BreakProgress", Category.Render);
        addMode(new ModeValue("Mode", List.of("Percentage", "Time"), "mode", this));
        ColorLink color = new ColorLink("red", "green", "blue", this);
        addColor(new ColorValue("Text color", color));
        addSaturation(new SaturationValue(color));
        addBrightness(new BrightnessValue(color));
        addOpacity(new OpacityValue("Text opacity", "opacity", this));
        addSlider(new SliderValue("Scale", 0.5, 1.5, 0.05, null, "scale", this, Float.TYPE));
        addToggle(new ToggleValue("Dynamic color", "dynamicColor", this));
        tag(ModuleTag.LEGIT);
        tooltip("Displays a countdown until block is broken.");
    }

    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            resetProgress();
            return;
        }
        float current = ((MultiPlayerGameModeAccessor) mc.gameMode).meowtils$destroyProgress();
        if (current <= 0.0f || !((MultiPlayerGameModeAccessor) mc.gameMode).meowtils$isDestroying()) {
            resetProgress();
            return;
        }
        progress = current;
        block = hit.getBlockPos();
        updateProgressString();
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.player == null || mc.level == null || progress <= 0.0f || block == null || progressStr.isEmpty()) return;
        float[] screen = new float[2];
        if (!WorldOverlay.project(block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5, event.getGraphics().guiWidth(), event.getGraphics().guiHeight(), screen)) return;
        int alpha = Math.round(WorldOverlay.opacity01(this, "opacity", 100) * 255);
        int color = WorldOverlay.withAlpha(dynamicColor(), alpha / 255.0f);
        String label = progressStr.replace(',', '.');
        float textScale = (float) Settings.number(this, "scale", 0.65);
        wtf.tatp.meowtils.font.HudFont.draw(event.getGraphics(), label, screen[0] - wtf.tatp.meowtils.font.HudFont.width(label, textScale) / 2f, screen[1], textScale, color);
    }

    private int dynamicColor() {
        if (!Settings.bool(this, "dynamicColor", false)) return WorldOverlay.color(this);
        if (progress > 0.9f) return 0xFFAA0000;
        if (progress > 0.8f) return 0xFFFF5555;
        if (progress > 0.7f) return 0xFFFFAA00;
        if (progress > 0.6f) return 0xFFFFFF55;
        return WorldOverlay.color(this);
    }

    private void updateProgressString() {
        if ("Percentage".equals(Settings.text(this, "mode", "Percentage"))) {
            progressStr = ((int) (progress * 100.0f)) + "%";
            return;
        }
        if (block == null || mc.level == null || mc.player == null) return;
        BlockState state = mc.level.getBlockState(block);
        float increment = state.getDestroyProgress(mc.player, mc.level, block);
        if (increment <= 0.0f) return;
        int ticks = (int) Math.ceil((1.0f - progress) / increment);
        progressStr = String.format("%.1f", ticks / 20.0).replace(',', '.') + "s";
    }

    private void resetProgress() {
        progress = 0;
        block = null;
        progressStr = "";
    }
}
