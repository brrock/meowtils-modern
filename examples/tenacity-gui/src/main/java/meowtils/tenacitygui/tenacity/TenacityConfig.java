package meowtils.tenacitygui.tenacity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.gui.Module.Category;

public final class TenacityConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final long SAVE_COOLDOWN = 1000L;
   private static long lastSave;
   private static String lastSignature;
   private static boolean loaded;

   private TenacityConfig() {
   }

   public static File directory() {
      return new File(Minecraft.getInstance().gameDirectory, "meowtils/TenacityGUI");
   }

   public static File file() {
      return new File(directory(), "config.json");
   }

   public static void load() {
      if (!loaded) {
         loaded = true;
         TenacityGuiModule module = TenacityGuiModule.get();
         if (module != null) {
            File file = file();
            if (!file.exists()) {
               if (module.enabled) {
                  setState(module, true);
               }
            } else {
               FileReader reader = null;

               try {
                  reader = new FileReader(file);
                  JsonObject root = (JsonObject)GSON.fromJson(reader, JsonObject.class);
                  if (root != null) {
                     setState(module, bool(root, "enabled", bool(root, "override", module.getState())));
                     module.theme = string(root, "theme", module.theme);
                     module.transparent = bool(root, "transparent", module.transparent);
                     module.outlineAccent = bool(root, "outlineAccent", module.outlineAccent);
                     module.scrollMode = string(root, "scrollMode", module.scrollMode);
                     module.clickHeight = (float)number(root, "clickHeight", module.clickHeight);
                     module.color1R = (int)number(root, "color1R", module.color1R);
                     module.color1G = (int)number(root, "color1G", module.color1G);
                     module.color1B = (int)number(root, "color1B", module.color1B);
                     module.color2R = (int)number(root, "color2R", module.color2R);
                     module.color2G = (int)number(root, "color2G", module.color2G);
                     module.color2B = (int)number(root, "color2B", module.color2B);
                     JsonObject panels = root.has("panels") ? root.getAsJsonObject("panels") : null;
                     if (panels != null) {
                        Category[] categories = Category.values();
                        module.panelX = new int[categories.length];
                        module.panelY = new int[categories.length];

                        for (int i = 0; i < categories.length; i++) {
                           String key = categories[i].name();
                           module.panelX[i] = Integer.MIN_VALUE;
                           module.panelY[i] = Integer.MIN_VALUE;
                           if (panels.has(key)) {
                              JsonObject panel = panels.getAsJsonObject(key);
                              module.panelX[i] = (int)number(panel, "x", -2.1474836E9F);
                              module.panelY[i] = (int)number(panel, "y", -2.1474836E9F);
                           }
                        }
                     }

                     return;
                  }
               } catch (Exception var12) {
                  Meowtils.error("Tenacity GUI could not read " + file.getName());
                  var12.printStackTrace();
                  return;
               } finally {
                  close(reader);
               }
            }
         }
      }
   }

   public static void save() {
      TenacityGuiModule module = TenacityGuiModule.get();
      if (module != null) {
         String signature = signature(module);
         if (!signature.equals(lastSignature)) {
            if (System.currentTimeMillis() - lastSave >= 1000L) {
               forceSave();
            }
         }
      }
   }

   public static void forceSave() {
      TenacityGuiModule module = TenacityGuiModule.get();
      if (module != null) {
         FileWriter writer = null;

         try {
            File directory = directory();
            if (directory.exists() || directory.mkdirs()) {
               JsonObject root = new JsonObject();
               root.addProperty("enabled", module.getState());
               root.addProperty("theme", module.theme);
               root.addProperty("transparent", module.transparent);
               root.addProperty("outlineAccent", module.outlineAccent);
               root.addProperty("scrollMode", module.scrollMode);
               root.addProperty("clickHeight", module.clickHeight);
               root.addProperty("color1R", module.color1R);
               root.addProperty("color1G", module.color1G);
               root.addProperty("color1B", module.color1B);
               root.addProperty("color2R", module.color2R);
               root.addProperty("color2G", module.color2G);
               root.addProperty("color2B", module.color2B);
               JsonObject panels = new JsonObject();
               Category[] categories = Category.values();

               for (int i = 0; i < categories.length && i < module.panelX.length && i < module.panelY.length; i++) {
                  if (module.panelX[i] != Integer.MIN_VALUE) {
                     JsonObject panel = new JsonObject();
                     panel.addProperty("x", module.panelX[i]);
                     panel.addProperty("y", module.panelY[i]);
                     panels.add(categories[i].name(), panel);
                  }
               }

               root.add("panels", panels);
               writer = new FileWriter(file());
               GSON.toJson(root, writer);
               lastSave = System.currentTimeMillis();
               lastSignature = signature(module);
               return;
            }

            Meowtils.error("Tenacity GUI could not create " + directory.getPath());
         } catch (Exception var11) {
            Meowtils.error("Tenacity GUI could not write its config");
            var11.printStackTrace();
            return;
         } finally {
            close(writer);
         }
      }
   }

   private static String signature(TenacityGuiModule m) {
      StringBuilder builder = new StringBuilder();
      builder.append(m.getState())
         .append('|')
         .append(m.theme)
         .append('|')
         .append(m.transparent)
         .append('|')
         .append(m.outlineAccent)
         .append('|')
         .append(m.scrollMode)
         .append('|')
         .append(m.clickHeight)
         .append('|')
         .append(m.color1R)
         .append(',')
         .append(m.color1G)
         .append(',')
         .append(m.color1B)
         .append('|')
         .append(m.color2R)
         .append(',')
         .append(m.color2G)
         .append(',')
         .append(m.color2B)
         .append('|');

      for (int i = 0; i < m.panelX.length && i < m.panelY.length; i++) {
         builder.append(m.panelX[i]).append(',').append(m.panelY[i]).append(';');
      }

      return builder.toString();
   }

   private static void setState(TenacityGuiModule module, boolean state) {
      if (module.getState() != state) {
         module.setState(state);
      }
   }

   private static boolean bool(JsonObject o, String key, boolean fallback) {
      return o.has(key) ? o.get(key).getAsBoolean() : fallback;
   }

   private static String string(JsonObject o, String key, String fallback) {
      return o.has(key) ? o.get(key).getAsString() : fallback;
   }

   private static double number(JsonObject o, String key, double fallback) {
      return o.has(key) ? o.get(key).getAsDouble() : fallback;
   }

   private static void close(Closeable c) {
      if (c != null) {
         try {
            c.close();
         } catch (Exception var2) {
         }
      }
   }
}
