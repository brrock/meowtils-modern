package meowtils.notifications.tenacity.render;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import meowtils.notifications.tenacity.util.ExtensionResources;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.extension.render.GlStateManager;
import wtf.tatp.meowtils.extension.render.OpenGlHelper;
import wtf.tatp.meowtils.extension.render.DynamicTexture;
import net.minecraft.resources.Identifier;
import wtf.tatp.meowtils.extension.render.GL11;
import wtf.tatp.meowtils.Meowtils;

public class TextureUtil {
   private static final String TEXTURE_DIR = "/meowtils/notifications/textures/";
   private static final Map<String, DynamicTexture> cache = new HashMap<>();
   private static final int GL_TEXTURE_2D = 3553;
   private static final int GL_TEXTURE_MIN_FILTER = 10241;
   private static final int GL_TEXTURE_MAG_FILTER = 10240;
   private static final int GL_LINEAR = 9729;

   private static void useLinearFiltering() {
      GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
      GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
   }

   public static int get(String name) {
      DynamicTexture texture = cache.get(name);
      if (texture == null) {
         InputStream is = null;

         byte e;
         try {
            is = ExtensionResources.open("/meowtils/notifications/textures/" + name + ".png");
            if (is != null) {
               BufferedImage image = ImageIO.read(is);
               if (image == null) {
                  Meowtils.error("Tenacity Notifications could not decode texture " + name + ".png");
                  return -1;
               }
               texture = new DynamicTexture(image);
               cache.put(name, texture);
               return texture.func_110552_b();
            }

            Meowtils.error("Tenacity Notifications could not load texture " + name + ".png");
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

   public static int getBorderRecolored(String name, int rgb, int lumaThreshold) {
      return getBorderRecolored(name, rgb, lumaThreshold, false);
   }

   public static int getBorderRecolored(String name, int rgb, int lumaThreshold, boolean borderOnly) {
      String key = name + "@border" + rgb + "/" + lumaThreshold + (borderOnly ? "/frame" : "");
      DynamicTexture texture = cache.get(key);
      if (texture != null) {
         return texture.func_110552_b();
      } else {
         InputStream is = null;

         byte e;
         try {
            is = ExtensionResources.open("/meowtils/notifications/textures/" + name + ".png");
            if (is != null) {
               BufferedImage source = ImageIO.read(is);
               if (source == null) {
                  Meowtils.error("Tenacity Notifications could not decode texture " + name + ".png");
                  return -1;
               }
               BufferedImage out = new BufferedImage(source.getWidth(), source.getHeight(), 2);

               for (int y = 0; y < source.getHeight(); y++) {
                  for (int x = 0; x < source.getWidth(); x++) {
                     int argb = source.getRGB(x, y);
                     int luma = argb >> 16 & 0xFF;
                     if (luma >= lumaThreshold) {
                        out.setRGB(x, y, argb & 0xFF000000 | rgb & 16777215);
                     } else {
                        out.setRGB(x, y, borderOnly ? argb & 16777215 : argb);
                     }
                  }
               }

               texture = new DynamicTexture(out);
               cache.put(key, texture);
               return texture.func_110552_b();
            }

            Meowtils.error("Tenacity Notifications could not load texture " + name + ".png");
            e = -1;
         } catch (Exception var22) {
            var22.printStackTrace();
            return -1;
         } finally {
            if (is != null) {
               try {
                  is.close();
               } catch (Exception var21) {
               }
            }
         }

         return e;
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

   public static void drawImage(String name, float x, float y, float imgWidth, float imgHeight, Color color) {
      RenderUtil.color(color.getRGB(), color.getAlpha() / 255.0F);
      drawImage(name, x, y, imgWidth, imgHeight);
      RenderUtil.resetColor();
   }

   public static void drawNineSlice(String name, float x, float y, float w, float h, int imgW, int imgH, int[] inset, float scale) {
      drawNineSlice(get(name), x, y, w, h, imgW, imgH, inset, scale);
   }

   public static void drawNineSlice(int id, float x, float y, float w, float h, int imgW, int imgH, int[] inset, float scale) {
      if (id != -1) {
         float sl = inset[0];
         float st = inset[1];
         float sr = inset[2];
         float sb = inset[3];
         float dl = sl * scale;
         float dt = st * scale;
         float dr = sr * scale;
         float db = sb * scale;
         if (dl + dr > w) {
            float k = w / (dl + dr);
            dl *= k;
            dr *= k;
         }

         if (dt + db > h) {
            float k = h / (dt + db);
            dt *= k;
            db *= k;
         }

         GLUtil.startBlend();
         RenderUtil.resetColor();
         GlStateManager.func_179138_g(OpenGlHelper.field_77478_a);
         GlStateManager.func_179098_w();
         GlStateManager.func_179144_i(id);
         useLinearFiltering();
         GL11.glTexParameteri(3553, 10242, 33071);
         GL11.glTexParameteri(3553, 10243, 33071);
         float[] dx = new float[]{x, x + dl, x + w - dr, x + w};
         float[] dy = new float[]{y, y + dt, y + h - db, y + h};
         float[] ux = new float[]{0.0F, sl / imgW, (imgW - sr) / imgW, 1.0F};
         float[] uy = new float[]{0.0F, st / imgH, (imgH - sb) / imgH, 1.0F};
         GL11.glBegin(7);

         for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
               quad(dx[col], dy[row], dx[col + 1], dy[row + 1], ux[col], uy[row], ux[col + 1], uy[row + 1]);
            }
         }

         GL11.glEnd();
         GlStateManager.func_179144_i(0);
         GLUtil.endBlend();
      }
   }

   private static void quad(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1) {
      GL11.glTexCoord2f(u0, v0);
      GL11.glVertex2f(x0, y0);
      GL11.glTexCoord2f(u0, v1);
      GL11.glVertex2f(x0, y1);
      GL11.glTexCoord2f(u1, v1);
      GL11.glVertex2f(x1, y1);
      GL11.glTexCoord2f(u1, v0);
      GL11.glVertex2f(x1, y0);
   }

   public static void drawImage(String name, float x, float y, float imgWidth, float imgHeight) {
      int id = get(name);
      if (id != -1) {
         GLUtil.startBlend();
         GlStateManager.func_179138_g(OpenGlHelper.field_77478_a);
         GlStateManager.func_179098_w();
         GlStateManager.func_179144_i(id);
         useLinearFiltering();
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
      useLinearFiltering();
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
