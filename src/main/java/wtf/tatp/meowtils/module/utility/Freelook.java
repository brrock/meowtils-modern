package wtf.tatp.meowtils.module.utility;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;
import wtf.tatp.meowtils.event.GuiOpenEvent;
import wtf.tatp.meowtils.event.RenderTickEvent;
import wtf.tatp.meowtils.event.WorldEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.BindValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.Settings;

/** Third-person camera look that does not change the player's server-side yaw/pitch. */
public final class Freelook extends Module {
    private static boolean perspectiveToggled;
    private static boolean previousState;
    private static boolean keyToggled;
    private static float cameraYaw;
    private static float cameraPitch;
    private static int previousFov = 70;
    private static CameraType previousPerspective = CameraType.FIRST_PERSON;

    public Freelook() {
        super("Freelook", Category.Utility);
        tag(ModuleTag.LEGIT);
        tooltip("Allows you to look around in third person without changing your serverside view.");
        addMode(new ModeValue("Mode", List.of("Hold", "Toggle"), "mode", this));
        addMode(new ModeValue("Start Position", List.of("Forward", "Backwards"), "startingPos", this));
        addToggle(new ToggleValue("Custom fov", "customFov", this));
        addSlider(new SliderValue("Fov", 30, 110, 5, null, "fov", this, Integer.class));
        addBind(new BindValue("Bind", "freelookKey", this));
    }

    @EventTarget
    public void onRenderTick(RenderTickEvent event) {
        if (event.getPhase() != RenderTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        if (mc.gui.screen() != null) {
            if (perspectiveToggled) resetPerspective();
            return;
        }
        int key = lookKey();
        boolean down = key != 0 && InputConstants.isKeyDown(mc.getWindow(), key);
        if (down == previousState) return;
        if ("Hold".equals(Settings.text(this, "mode", "Hold"))) {
            onPressed(down);
        } else if (down) {
            keyToggled = !keyToggled;
            onPressed(keyToggled);
        }
        previousState = down;
    }

    @EventTarget
    public void onWorldLoad(WorldEvent event) {
        if (event.getType() == WorldEvent.Type.LOAD && perspectiveToggled) resetPerspective();
    }

    @EventTarget
    public void onGuiOpen(GuiOpenEvent event) {
        if (mc.player != null && mc.level != null && event.getGui() != null && perspectiveToggled) resetPerspective();
    }

    private void onPressed(boolean down) {
        if (mc.player == null) return;
        if (down) {
            cameraYaw = mc.player.getYRot();
            cameraPitch = mc.player.getXRot();
            if (perspectiveToggled) resetPerspective();
            else enterPerspective();
            return;
        }
        resetPerspective();
    }

    private void enterPerspective() {
        perspectiveToggled = true;
        previousFov = mc.options.fov().get();
        previousPerspective = mc.options.getCameraType();
        if (Settings.bool(this, "customFov", false)) {
            mc.options.fov().set(Settings.integer(this, "fov", 70));
        }
        if ("Backwards".equals(Settings.text(this, "startingPos", "Forward"))) {
            mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
        } else {
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
    }

    private void resetPerspective() {
        perspectiveToggled = false;
        keyToggled = false;
        mc.options.fov().set(previousFov);
        mc.options.setCameraType(previousPerspective);
    }

    /** Applies the same 26.2 {@code Entity.turn} scale to the detached camera. */
    public static void applyDelta(double yaw, double pitch) {
        if (!perspectiveToggled) return;
        cameraYaw += (float) yaw * 0.15f;
        cameraPitch += (float) pitch * 0.15f;
        cameraPitch = Mth.clamp(cameraPitch, -90.0f, 90.0f);
    }

    public static boolean isActive() {
        return perspectiveToggled;
    }

    public static float getYaw() {
        return cameraYaw;
    }

    public static float getPitch() {
        return cameraPitch;
    }

    private int lookKey() {
        int bind = Settings.integer(this, "freelookKey", 0);
        return bind != 0 ? bind : getKey();
    }

    @Override
    public void onDisable() {
        if (perspectiveToggled) resetPerspective();
    }
}
