package wtf.tatp.meowtils.module.utility;

import wtf.tatp.meowtils.gui.Module;

/** Configuration/registration surface for third-person camera clipping. */
public final class ViewClip extends Module {
    public ViewClip() { super("ViewClip", Category.Utility); tooltip("Allows the third-person camera to clip through blocks."); tag(ModuleTag.SAFE); }
    public static boolean active() { var m=get(ViewClip.class); var c=net.minecraft.client.Minecraft.getInstance(); return m!=null&&m.getState()&&c.options.getCameraType()!=net.minecraft.client.CameraType.FIRST_PERSON; }
}
