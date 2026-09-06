package wtf.tatp.meowtils.extension.render;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
public final class Keyboard {
 private static final int[] KEYS={0,256,49,50,51,52,53,54,55,56,57,48,45,61,259,258,81,87,69,82,84,89,85,73,79,80,91,93,257,341,65,83,68,70,71,72,74,75,76,59,39,96,340,92,90,88,67,86,66,78,77,44,46,47,344,332,342,32,280,290,291,292,293,294,295,296,297,298,299};
 public static int modern(int code){if(code>=0&&code<KEYS.length)return KEYS[code];return switch(code){case 199->268;case 200->265;case 201->266;case 203->263;case 205->262;case 207->269;case 208->264;case 209->267;case 211->261;case 157->345;case 184->346;case 219->343;case 220->347;default->code;};}
 public static int legacy(int code){for(int i=1;i<KEYS.length;i++)if(KEYS[i]==code)return i;for(int i:new int[]{199,200,201,203,205,207,208,209,211,157,184,219,220})if(modern(i)==code)return i;return code;}
 public static boolean isKeyDown(int code){int key=modern(code);return key>0&&GLFW.glfwGetKey(Minecraft.getInstance().getWindow().handle(),key)==GLFW.GLFW_PRESS;}
 public static String getKeyName(int code){return code==0?"NONE":com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(modern(code)).getDisplayName().getString();}
 public static void enableRepeatEvents(boolean enabled){} // Screen receives GLFW repeat events.
}
