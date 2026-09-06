package meowtils.notifications.tenacity.render;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.extension.render.ScaledResolution;
import wtf.tatp.meowtils.extension.render.GlStateManager;
import wtf.tatp.meowtils.extension.render.GL11;

public class RenderUtil {
   private static final Minecraft mc = Minecraft.getInstance();

   public static void drawRect2(double x, double y, double width, double height, int color) {
      GL11.glDisable(3553); color(color); GL11.glBegin(7);
      GL11.glVertex2d(x,y);GL11.glVertex2d(x,y+height);GL11.glVertex2d(x+width,y+height);GL11.glVertex2d(x+width,y);GL11.glEnd();GL11.glEnable(3553);
   }

   public static void drawRect2(double x, double y, double width, double height, Color color) {
      drawRect2(x, y, width, height, color.getRGB());
   }

   public static void drawRect(double left, double top, double right, double bottom, int color) {
      drawRect2(Math.min(left,right),Math.min(top,bottom),Math.abs(right-left),Math.abs(bottom-top),color);
   }

   public static void scaleStart(float x, float y, float scale) {
      GL11.glPushMatrix();
      GL11.glTranslatef(x, y, 0.0F);
      GL11.glScalef(scale, scale, 1.0F);
      GL11.glTranslatef(-x, -y, 0.0F);
   }

   public static void scaleEnd() {
      GL11.glPopMatrix();
   }

   public static void rotateStart(float x, float y, float width, float height, float rotation) {
      GL11.glPushMatrix();
      x += width / 2.0F;
      y += height / 3.0F;
      GL11.glTranslatef(x, y, 0.0F);
      GL11.glRotatef(rotation, 0.0F, 0.0F, 1.0F);
      GL11.glTranslatef(-x, -y, 0.0F);
   }

   public static void rotateStartReal(float x, float y, float width, float height, float rotation) {
      GL11.glPushMatrix();
      GL11.glTranslatef(x, y, 0.0F);
      GL11.glRotatef(rotation, 0.0F, 0.0F, 1.0F);
      GL11.glTranslatef(-x, -y, 0.0F);
   }

   public static void rotateEnd() {
      GL11.glPopMatrix();
   }

   public static void scissorStart(double x, double y, double width, double height) {
      wtf.tatp.meowtils.extension.render.Draw.g().enableScissor((int)x,(int)y,(int)Math.ceil(x+width),(int)Math.ceil(y+height));
   }

   public static void scissorEnd() {
      wtf.tatp.meowtils.extension.render.Draw.g().disableScissor();
   }

   public static void setAlphaLimit(float limit) {
      GlStateManager.func_179141_d();
      GlStateManager.func_179092_a(516, (float)(limit * 0.01));
   }

   public static void color(int color, float alpha) {
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      GlStateManager.func_179131_c(r, g, b, alpha);
   }

   public static void color(int color) {
      color(color, (color >> 24 & 0xFF) / 255.0F);
   }

   public static void resetColor() {
      GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public static boolean isHovered(float mouseX, float mouseY, float x, float y, float width, float height) {
      return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
   }

   public static void drawGradientRect(double left, double top, double right, double bottom, int startColor, int endColor) {
      GLUtil.setup2DRendering();
      GL11.glEnable(2848);
      GL11.glShadeModel(7425);
      GL11.glPushMatrix();
      GL11.glBegin(7);
      color(startColor);
      GL11.glVertex2d(left, top);
      GL11.glVertex2d(left, bottom);
      color(endColor);
      GL11.glVertex2d(right, bottom);
      GL11.glVertex2d(right, top);
      GL11.glEnd();
      GL11.glPopMatrix();
      GL11.glDisable(2848);
      GLUtil.end2DRendering();
      resetColor();
   }

   public static void drawGoodCircle(double x, double y, float radius, int color) {
      color(color);
      GLUtil.setup2DRendering();
      GL11.glEnable(2832);
      GL11.glHint(3153, 4354);
      GL11.glDisable(2884);
      GL11.glBegin(6);

      for (double i = 0.0; i <= 360.0; i++) {
         double angle = i * (Math.PI * 2) / 360.0;
         GL11.glVertex2d(x + radius * Math.cos(angle), y + radius * Math.sin(angle));
      }

      GL11.glEnd();
      GL11.glEnable(2884);
      GLUtil.end2DRendering();
      resetColor();
   }

   public static void drawBorderedRect(float x, float y, float width, float height, float outlineThickness, int rectColor, int outlineColor) {
      drawRect2(x, y, width, height, rectColor);
      GL11.glEnable(2848);
      color(outlineColor);
      GLUtil.setup2DRendering();
      GL11.glLineWidth(outlineThickness);
      float cornerValue = (float)(outlineThickness * 0.19);
      GL11.glBegin(1);
      GL11.glVertex2d(x, y - cornerValue);
      GL11.glVertex2d(x, y + height + cornerValue);
      GL11.glVertex2d(x + width, y + height + cornerValue);
      GL11.glVertex2d(x + width, y - cornerValue);
      GL11.glVertex2d(x, y);
      GL11.glVertex2d(x + width, y);
      GL11.glVertex2d(x, y + height);
      GL11.glVertex2d(x + width, y + height);
      GL11.glEnd();
      GLUtil.end2DRendering();
      GL11.glDisable(2848);
   }
}
