package meowtils.tenacitygui.tenacity.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meowtils.tenacitygui.tenacity.TenacityGuiModule;
import meowtils.tenacitygui.tenacity.util.Pair;

public enum Theme {
   SPEARMINT("Spearmint", new Color(97, 194, 162), new Color(65, 130, 108)),
   JADE_GREEN("Jade Green", new Color(0, 168, 107), new Color(0, 105, 66)),
   GREEN_SPIRIT("Green Spirit", new Color(0, 135, 62), new Color(159, 226, 191), true),
   ROSY_PINK("Rosy Pink", new Color(255, 102, 204), new Color(191, 77, 153)),
   MAGENTA("Magenta", new Color(213, 63, 119), new Color(157, 68, 110)),
   HOT_PINK("Hot Pink", new Color(231, 84, 128), new Color(172, 79, 198), true),
   LAVENDER("Lavender", new Color(219, 166, 247), new Color(152, 115, 172)),
   AMETHYST("Amethyst", new Color(144, 99, 205), new Color(98, 67, 140)),
   PURPLE_FIRE("Purple Fire", new Color(104, 71, 141), new Color(177, 162, 202), true),
   SUNSET_PINK("Sunset Pink", new Color(255, 145, 20), new Color(245, 105, 231), true),
   BLAZE_ORANGE("Blaze Orange", new Color(255, 169, 77), new Color(255, 130, 0)),
   PINK_BLOOD("Pink Blood", new Color(228, 0, 70), new Color(255, 166, 201), true),
   PASTEL("Pastel", new Color(255, 109, 106), new Color(191, 82, 80)),
   NEON_RED("Neon Red", new Color(210, 39, 48), new Color(184, 25, 42)),
   RED_COFFEE("Red Coffee", Color.BLACK, new Color(225, 34, 59)),
   DEEP_OCEAN("Deep Ocean", new Color(60, 82, 145), new Color(0, 20, 64), true),
   CHAMBRAY_BLUE("Chambray Blue", new Color(33, 46, 182), new Color(60, 82, 145)),
   MINT_BLUE("Mint Blue", new Color(66, 158, 157), new Color(40, 94, 93)),
   PACIFIC_BLUE("Pacific Blue", new Color(5, 169, 199), new Color(4, 115, 135)),
   TROPICAL_ICE("Tropical Ice", new Color(102, 255, 209), new Color(6, 149, 255), true),
   TENACITY("Tenacity", new Color(236, 133, 209), new Color(28, 167, 222), true),
   CUSTOM_THEME("Custom Theme", new Color(97, 194, 162), new Color(65, 130, 108));

   private static final Map<String, Theme> themeMap = new HashMap<>();
   private final String name;
   private final Pair<Color, Color> colors;
   private final boolean gradient;

   private Theme(String name, Color color, Color colorAlt) {
      this(name, color, colorAlt, false);
   }

   private Theme(String name, Color color, Color colorAlt, boolean gradient) {
      this.name = name;
      this.colors = Pair.of(color, colorAlt);
      this.gradient = gradient;
   }

   public String getName() {
      return this.name;
   }

   public boolean isGradient() {
      return this.gradient;
   }

   public Pair<Color, Color> getColors() {
      if (this == CUSTOM_THEME) {
         TenacityGuiModule module = TenacityGuiModule.get();
         if (module != null) {
            return Pair.of(new Color(module.getColor1Link().getRGB()), new Color(module.getColor2Link().getRGB()));
         }
      }

      return this.colors;
   }

   public static Pair<Color, Color> getThemeColors(String name) {
      Theme theme = get(name);
      return theme == null ? SPEARMINT.getColors() : theme.getColors();
   }

   public static Theme get(String name) {
      return themeMap.get(name);
   }

   public static Theme getCurrentTheme() {
      TenacityGuiModule module = TenacityGuiModule.get();
      Theme theme = module == null ? null : get(module.theme);
      return theme == null ? TENACITY : theme;
   }

   public static List<String> names() {
      List<String> names = new ArrayList<>();

      for (Theme theme : values()) {
         names.add(theme.getName());
      }

      return names;
   }

   public static Theme[] all() {
      return Arrays.copyOf(values(), values().length);
   }

   static {
      for (Theme theme : values()) {
         themeMap.put(theme.getName(), theme);
      }
   }
}
