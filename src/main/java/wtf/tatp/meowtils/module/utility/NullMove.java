package wtf.tatp.meowtils.module.utility;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.gui.Module;

/** Movement-key compatibility module; its hook is intentionally isolated for the 26.2 input pipeline. */
public final class NullMove extends Module {
    private static long left, right, forward, back;
    public NullMove() { super("NullMove", Category.Utility); tooltip("Prevents opposing movement keys from cancelling each other."); tag(ModuleTag.LEGIT); }
    private static InputConstants.Key key(net.minecraft.client.KeyMapping mapping) { return ((wtf.tatp.meowtils.mixin.KeyMappingAccessor)(Object) mapping).meowtils$getKey(); }
    public static boolean shouldOverride() { var c=Minecraft.getInstance(); var m=get(NullMove.class); return m!=null&&m.getState()&&c.gui.screen()==null&&c.player!=null&&c.level!=null; }
    public static boolean isMovementKey(InputConstants.Key k) { var o=Minecraft.getInstance().options; return k.equals(key(o.keyLeft))||k.equals(key(o.keyRight))||k.equals(key(o.keyUp))||k.equals(key(o.keyDown)); }
    public static boolean accept(InputConstants.Key k, boolean pressed) { if(!pressed)return true; var o=Minecraft.getInstance().options; if(k.equals(key(o.keyLeft)))return right==0||right<=left; if(k.equals(key(o.keyRight)))return left==0||left<=right; if(k.equals(key(o.keyUp)))return back==0||back<=forward; if(k.equals(key(o.keyDown)))return forward==0||forward<=back; return true; }
    public static void update(InputConstants.Key k, boolean pressed) { long t=pressed?System.nanoTime():0; var o=Minecraft.getInstance().options; if(k.equals(key(o.keyLeft)))left=t; else if(k.equals(key(o.keyRight)))right=t; else if(k.equals(key(o.keyUp)))forward=t; else if(k.equals(key(o.keyDown)))back=t; }
}
