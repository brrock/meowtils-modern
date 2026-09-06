package wtf.tatp.meowtils.extension.render;
public final class Mouse {
 public static int wheel;
 public static int getDWheel(){int value=wheel;wheel=0;return value;}
 public static boolean isButtonDown(int button){return org.lwjgl.glfw.GLFW.glfwGetMouseButton(net.minecraft.client.Minecraft.getInstance().getWindow().handle(),button)==1;}
}
