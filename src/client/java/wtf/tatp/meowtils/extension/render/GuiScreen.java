package wtf.tatp.meowtils.extension.render;
import net.minecraft.client.Minecraft;
public final class GuiScreen {
 public static boolean func_146271_m(){return Keyboard.isKeyDown(29)||Keyboard.isKeyDown(157)||Keyboard.isKeyDown(219)||Keyboard.isKeyDown(220);}
 public static boolean func_146272_n(){return Keyboard.isKeyDown(42)||Keyboard.isKeyDown(54);}
 public static boolean func_175278_g(int key){return key==30&&func_146271_m();}
 public static boolean func_175280_f(int key){return key==46&&func_146271_m();}
 public static boolean func_175279_e(int key){return key==47&&func_146271_m();}
 public static boolean func_175277_d(int key){return key==45&&func_146271_m();}
 public static String func_146277_j(){return Minecraft.getInstance().keyboardHandler.getClipboard();}
 public static void func_146275_d(String text){Minecraft.getInstance().keyboardHandler.setClipboard(text);}
}
