package wtf.tatp.meowtils.module.render;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.OpacityValue;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;

/**
 * Reveals invisible players with the original opacity slider.
 * 26.2 has no GL11 color; we force the spectator translucent path and replace
 * the hardcoded 0x26FFFFFF tint with the slider via {@link #tint}.
 */
public final class AntiInvis extends Module {
    public static final RenderStateDataKey<Float> OPACITY = RenderStateDataKey.create(() -> "meowtils:anti_invis_opacity");

    @Config public float opacity = 50;

    public AntiInvis() {
        super("Anti-Invis", Category.Render);
        addOpacity(new OpacityValue("Opacity", "opacity", this));
        tag(ModuleTag.SAFE);
        tooltip("Renders invisible players semi-transparent instead.");
    }

    public static boolean shouldReveal(Player player) {
        AntiInvis module = get(AntiInvis.class);
        if (module == null || !module.getState() || player == null || module.mc.player == null) return false;
        var gui = get(wtf.tatp.meowtils.module.meowtils.GUI.class);
        if (gui != null && Settings.bool(gui, "debugMode", false)) return player != module.mc.player;
        return player != module.mc.player && player.isInvisible() && !TeamUtil.isBot(player);
    }

    public static float opacity() {
        AntiInvis module = get(AntiInvis.class);
        return module == null ? 0.5f : WorldOverlay.opacity01(module, "opacity", 50);
    }

    public static void applyReveal(LivingEntityRenderState state) {
        state.isInvisible = false;
        // LivingEntityRenderer.submit uses isInvisibleToPlayer to pick entityTranslucent + a tint.
        state.isInvisibleToPlayer = true;
        state.setData(OPACITY, opacity());
    }

    public static boolean revealed(LivingEntityRenderState state) {
        return state.getData(OPACITY) != null;
    }

    /** Replaces the spectator 0x26FFFFFF multiply with the module slider. */
    public static int tint(LivingEntityRenderState state, int color, int modelTint) {
        Float alpha = state.getData(OPACITY);
        if (alpha == null) return ARGB.multiply(color, modelTint);
        return ARGB.multiply(ARGB.white(alpha), modelTint);
    }
}
