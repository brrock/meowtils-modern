package meowtils.notifications.tenacity.font;

import java.awt.Font;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import meowtils.notifications.tenacity.util.ExtensionResources;
import wtf.tatp.meowtils.Meowtils;

public class FontUtil {
   public static final String INFO = "m";
   public static final String CHECKMARK = "o";
   public static final String XMARK = "p";
   public static final String WARNING = "r";
   private static final String FONT_DIR = "/meowtils/notifications/fonts/";
   private static final HashMap<FontUtil.FontType, Map<Integer, CustomFont>> customFontMap = new HashMap<>();
   private static boolean loaded;

   public static void setupFonts() {
      if (!loaded) {
         loaded = true;

         for (FontUtil.FontType type : FontUtil.FontType.values()) {
            type.setup();
            HashMap<Integer, CustomFont> fontSizes = new HashMap<>();
            if (type.hasBold()) {
               for (int size : type.getSizes()) {
                  CustomFont font = new CustomFont(type.fromSize(size));
                  font.setBoldFont(new CustomFont(type.fromBoldSize(size)));
                  fontSizes.put(size, font);
               }
            } else {
               for (int size : type.getSizes()) {
                  fontSizes.put(size, new CustomFont(type.fromSize(size)));
               }
            }

            customFontMap.put(type, fontSizes);
         }
      }
   }

   public static boolean isLoaded() {
      return loaded;
   }

   private static Font getFontData(String name) {
      String path = "/meowtils/notifications/fonts/" + name + ".ttf";
      InputStream is = null;

      Font e;
      try {
         is = ExtensionResources.open(path);
         if (is == null) {
            throw new IllegalStateException("resource not found: " + path);
         }

         e = Font.createFont(0, is);
      } catch (Exception var14) {
         Meowtils.error("Tenacity Notifications could not load font " + name + ".ttf (" + var14.getMessage() + ")");
         var14.printStackTrace();
         return new Font("default", 0, 10);
      } finally {
         if (is != null) {
            try {
               is.close();
            } catch (Exception var13) {
            }
         }
      }

      return e;
   }

   public static enum FontType {
      TENACITY("tenacity", "tenacity-bold", 18, 22),
      PROXIMA("proxima", "proximabd", 14),
      ICON("icon", 35);

      private final String fontName;
      private final String boldName;
      private Font font;
      private Font boldFont;
      private final int[] sizes;

      private FontType(String fontName, String boldName, int... sizes) {
         this.fontName = fontName;
         this.boldName = boldName;
         this.sizes = sizes;
      }

      private FontType(String fontName, int... sizes) {
         this.fontName = fontName;
         this.boldName = null;
         this.sizes = sizes;
      }

      public int[] getSizes() {
         return this.sizes;
      }

      public boolean hasBold() {
         return this.boldName != null;
      }

      public Font fromSize(int size) {
         return this.font.deriveFont(0, size);
      }

      private Font fromBoldSize(int size) {
         return this.boldFont.deriveFont(0, size);
      }

      public void setup() {
         this.font = FontUtil.getFontData(this.fontName);
         if (this.boldName != null) {
            this.boldFont = FontUtil.getFontData(this.boldName);
         }
      }

      public CustomFont size(int size) {
         Map<Integer, CustomFont> map = FontUtil.customFontMap.get(this);
         return map == null ? null : map.get(size);
      }

      public CustomFont boldSize(int size) {
         CustomFont regular = this.size(size);
         return regular == null ? null : regular.getBoldFont();
      }
   }
}
