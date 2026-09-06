package meowtils.tenacitygui.tenacity.font;

public final class Fonts {
   public static CustomFont tenacityFont14;
   public static CustomFont tenacityFont16;
   public static CustomFont tenacityFont18;
   public static CustomFont tenacityFont20;
   public static CustomFont tenacityFont22;
   public static CustomFont tenacityFont24;
   public static CustomFont tenacityFont26;
   public static CustomFont tenacityBoldFont14;
   public static CustomFont tenacityBoldFont16;
   public static CustomFont tenacityBoldFont18;
   public static CustomFont tenacityBoldFont20;
   public static CustomFont tenacityBoldFont22;
   public static CustomFont tenacityBoldFont24;
   public static CustomFont tenacityBoldFont26;
   public static CustomFont iconFont16;
   public static CustomFont iconFont20;
   private static boolean bound;

   private Fonts() {
   }

   public static void init() {
      if (!bound) {
         FontUtil.setupFonts();
         bound = true;
         FontUtil.FontType t = FontUtil.FontType.TENACITY;
         tenacityFont14 = t.size(14);
         tenacityFont16 = t.size(16);
         tenacityFont18 = t.size(18);
         tenacityFont20 = t.size(20);
         tenacityFont22 = t.size(22);
         tenacityFont24 = t.size(24);
         tenacityFont26 = t.size(26);
         tenacityBoldFont14 = t.boldSize(14);
         tenacityBoldFont16 = t.boldSize(16);
         tenacityBoldFont18 = t.boldSize(18);
         tenacityBoldFont20 = t.boldSize(20);
         tenacityBoldFont22 = t.boldSize(22);
         tenacityBoldFont24 = t.boldSize(24);
         tenacityBoldFont26 = t.boldSize(26);
         iconFont16 = FontUtil.FontType.ICON.size(16);
         iconFont20 = FontUtil.FontType.ICON.size(20);
         if (iconFont16 != null) {
            iconFont16.setSmooth(true);
         }

         if (iconFont20 != null) {
            iconFont20.setSmooth(true);
         }
      }
   }
}
