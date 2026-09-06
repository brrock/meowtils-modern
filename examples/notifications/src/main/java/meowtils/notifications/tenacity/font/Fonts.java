package meowtils.notifications.tenacity.font;

public final class Fonts {
   public static CustomFont tenacityFont18;
   public static CustomFont tenacityFont22;
   public static CustomFont tenacityBoldFont18;
   public static CustomFont tenacityBoldFont22;
   public static CustomFont iconFont35;
   public static CustomFont proxima14;
   public static CustomFont proximaBold14;
   private static boolean bound;

   private Fonts() {
   }

   public static void init() {
      if (!bound) {
         FontUtil.setupFonts();
         bound = true;
         FontUtil.FontType t = FontUtil.FontType.TENACITY;
         tenacityFont18 = t.size(18);
         tenacityFont22 = t.size(22);
         tenacityBoldFont18 = t.boldSize(18);
         tenacityBoldFont22 = t.boldSize(22);
         FontUtil.FontType proxima = FontUtil.FontType.PROXIMA;
         proxima14 = proxima.size(14);
         proximaBold14 = proxima.boldSize(14);
         iconFont35 = FontUtil.FontType.ICON.size(35);
         if (tenacityFont18 != null) {
            tenacityFont18.setSmooth(true);
         }
         if (tenacityFont22 != null) {
            tenacityFont22.setSmooth(true);
         }
         if (tenacityBoldFont18 != null) {
            tenacityBoldFont18.setSmooth(true);
         }
         if (tenacityBoldFont22 != null) {
            tenacityBoldFont22.setSmooth(true);
         }
         if (iconFont35 != null) {
            iconFont35.setSmooth(true);
         }
         if (proxima14 != null) {
            proxima14.setSmooth(true);
         }
         if (proximaBold14 != null) {
            proximaBold14.setSmooth(true);
         }
      }
   }

   public static boolean ready() {
      return bound && tenacityFont18 != null && tenacityBoldFont22 != null && iconFont35 != null && proxima14 != null && proximaBold14 != null;
   }
}
