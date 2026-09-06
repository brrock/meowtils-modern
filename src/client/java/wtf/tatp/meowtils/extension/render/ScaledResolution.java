package wtf.tatp.meowtils.extension.render;
import net.minecraft.client.Minecraft;
public final class ScaledResolution {
 public ScaledResolution(Minecraft mc){}
 public int func_78326_a(){return Minecraft.getInstance().getWindow().getGuiScaledWidth();}
 public int func_78328_b(){return Minecraft.getInstance().getWindow().getGuiScaledHeight();}
 public int func_78325_e(){return (int)Minecraft.getInstance().getWindow().getGuiScale();}
}
