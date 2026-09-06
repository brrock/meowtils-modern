package meowtils.tenacitygui.tenacity.font;

import java.awt.Font;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import meowtils.tenacitygui.tenacity.util.ExtensionResources;
import wtf.tatp.meowtils.Meowtils;

public class FontUtil {
   public static final String BUG = "a";
   public static final String LIST = "b";
   public static final String BOMB = "c";
   public static final String EYE = "d";
   public static final String PERSON = "e";
   public static final String WHEELCHAIR = "f";
   public static final String SCRIPT = "g";
   public static final String SKIP_LEFT = "h";
   public static final String PAUSE = "i";
   public static final String PLAY = "j";
   public static final String SKIP_RIGHT = "k";
   public static final String SHUFFLE = "l";
   public static final String INFO = "m";
   public static final String SETTINGS = "n";
   public static final String CHECKMARK = "o";
   public static final String XMARK = "p";
   public static final String TRASH = "q";
   public static final String WARNING = "r";
   public static final String FOLDER = "s";
   public static final String LOAD = "t";
   public static final String SAVE = "u";
   public static final String UPVOTE_OUTLINE = "v";
   public static final String UPVOTE = "w";
   public static final String DOWNVOTE_OUTLINE = "x";
   public static final String DOWNVOTE = "y";
   public static final String DROPDOWN_ARROW = "z";
   public static final String PIN = "s";
   public static final String EDIT = "A";
   public static final String SEARCH = "B";
   public static final String UPLOAD = "C";
   public static final String REFRESH = "D";
   public static final String ADD_FILE = "E";
   public static final String STAR_OUTLINE = "F";
   public static final String STAR = "G";
   private static final String FONT_DIR = "/meowtils/tenacitygui/fonts/";
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
      String path = "/meowtils/tenacitygui/fonts/" + name + ".ttf";
      InputStream is = null;

      Font e;
      try {
         is = ExtensionResources.open(path);
         if (is == null) {
            throw new IllegalStateException("resource not found: " + path);
         }

         e = Font.createFont(0, is);
      } catch (Exception var14) {
         Meowtils.error("Tenacity GUI could not load font " + name + ".ttf (" + var14.getMessage() + ")");
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
      TENACITY("tenacity", "tenacity-bold", 14, 16, 18, 20, 22, 24, 26),
      ICON("icon", 16, 20);

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
