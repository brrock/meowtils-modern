package meowtils.tenacitygui.tenacity.render;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import meowtils.tenacitygui.tenacity.util.ExtensionResources;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.extension.render.GlStateManager;
import wtf.tatp.meowtils.extension.render.OpenGlHelper;
import wtf.tatp.meowtils.extension.render.DynamicTexture;
import net.minecraft.resources.Identifier;
import wtf.tatp.meowtils.extension.render.GL11;
import wtf.tatp.meowtils.Meowtils;

public class TextureUtil {
   public static final String COLOR_PICKER = "colorpicker2";
   public static final String HUE = "hue";
   private static final String TEXTURE_DIR = "/meowtils/tenacitygui/textures/";
   private static final Map<String, DynamicTexture> cache = new HashMap<>();

   public static int get(String name) {
      DynamicTexture texture = cache.get(name);
      if (texture == null) {
         InputStream is = null;

         byte e;
         try {
            is = ExtensionResources.open("/meowtils/tenacitygui/textures/" + name + ".png");
            if (is != null) {
               BufferedImage image = ImageIO.read(is);
               texture = new DynamicTexture(image);
               cache.put(name, texture);
               return texture.func_110552_b();
            }

            Meowtils.error("Tenacity GUI could not load texture " + name + ".png");
            e = -1;
         } catch (Exception var15) {
            var15.printStackTrace();
            return -1;
         } finally {
            if (is != null) {
               try {
                  is.close();
               } catch (Exception var14) {
               }
            }
         }

         return e;
      } else {
         return texture.func_110552_b();
      }
   }

   public static void bind(String name) {
      int id = get(name);
      if (id != -1) {
         GlStateManager.func_179138_g(OpenGlHelper.field_77478_a);
         GlStateManager.func_179098_w();
         GlStateManager.func_179144_i(id);
      }
   }

   public static void drawImage(String name, float x, float y, float imgWidth, float imgHeight) {
      int id = get(name);
      if (id != -1) {
         GLUtil.startBlend();
         GlStateManager.func_179138_g(OpenGlHelper.field_77478_a);
         GlStateManager.func_179098_w();
         GlStateManager.func_179144_i(id);
         GL11.glBegin(7);
         GL11.glTexCoord2f(0.0F, 0.0F);
         GL11.glVertex2f(x, y);
         GL11.glTexCoord2f(0.0F, 1.0F);
         GL11.glVertex2f(x, y + imgHeight);
         GL11.glTexCoord2f(1.0F, 1.0F);
         GL11.glVertex2f(x + imgWidth, y + imgHeight);
         GL11.glTexCoord2f(1.0F, 0.0F);
         GL11.glVertex2f(x + imgWidth, y);
         GL11.glEnd();
         GlStateManager.func_179144_i(0);
         GLUtil.endBlend();
      }
   }

   public static void drawResource(Identifier location, float x, float y, float width, float height) {
      GLUtil.startBlend();
      GlStateManager.func_179138_g(OpenGlHelper.field_77478_a);
      GlStateManager.func_179098_w();
      wtf.tatp.meowtils.extension.render.GL11.bind(location);
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      GL11.glBegin(7);
      GL11.glTexCoord2f(0.0F, 0.0F);
      GL11.glVertex2f(x, y);
      GL11.glTexCoord2f(0.0F, 1.0F);
      GL11.glVertex2f(x, y + height);
      GL11.glTexCoord2f(1.0F, 1.0F);
      GL11.glVertex2f(x + width, y + height);
      GL11.glTexCoord2f(1.0F, 0.0F);
      GL11.glVertex2f(x + width, y);
      GL11.glEnd();
      GLUtil.endBlend();
   }
}
