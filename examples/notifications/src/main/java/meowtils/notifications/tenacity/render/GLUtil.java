package meowtils.notifications.tenacity.render;

import wtf.tatp.meowtils.extension.render.GlStateManager;
import wtf.tatp.meowtils.extension.render.GL11;

public class GLUtil {
   public static int[] enabledCaps = new int[32];

   public static void enableDepth() {
      GlStateManager.func_179126_j();
      
   }

   public static void disableDepth() {
      GlStateManager.func_179097_i();
      
   }

   public static void enableCaps(int... caps) {
      for (int cap : caps) {
         GL11.glEnable(cap);
      }

      enabledCaps = caps;
   }

   public static void disableCaps() {
      for (int cap : enabledCaps) {
         GL11.glDisable(cap);
      }
   }

   public static void startBlend() {
      GlStateManager.func_179147_l();
      GlStateManager.func_179112_b(770, 771);
   }

   public static void endBlend() {
      GlStateManager.func_179084_k();
   }

   public static void setup2DRendering(boolean blend) {
      if (blend) {
         startBlend();
      }

      GlStateManager.func_179090_x();
   }

   public static void setup2DRendering() {
      setup2DRendering(true);
   }

   public static void end2DRendering() {
      GlStateManager.func_179098_w();
      endBlend();
   }

   public static void startRotate(float x, float y, float rotate) {
      GlStateManager.func_179094_E();
      GlStateManager.func_179109_b(x, y, 0.0F);
      GL11.glRotatef(-rotate,0,0,1);
      GlStateManager.func_179109_b(-x, -y, 0.0F);
   }

   public static void endRotate() {
      GlStateManager.func_179121_F();
   }
}
