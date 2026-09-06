package meowtils.tenacitygui.tenacity.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.Identifier;
import meowtils.tenacitygui.tenacity.TenacityConfig;
import meowtils.tenacitygui.tenacity.TenacityGuiModule;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.render.RenderUtil;
import meowtils.tenacitygui.tenacity.render.TextureUtil;
import meowtils.tenacitygui.tenacity.util.Drag;
import meowtils.tenacitygui.tenacity.util.Scroll;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.extension.render.ScaledResolution;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.gui.Module.Category;


public class MCategory {
   private static List<MCategory> values;
   private static final int SPACING = 120;
   private static final int PANEL_WIDTH = 105;
   private static final int HEADER_HEIGHT = 15;
   public static final int UNSET = Integer.MIN_VALUE;
   private static final float TEXTURE_ICON_SIZE = 10.0F;
   public final Category category;
   public final String name;
   public final String icon;
   public final String texture;
   private boolean textureFailed;
   public final int posX;
   public final int posY;
   private final Drag drag;
   private final Scroll scroll = new Scroll();
   private static int builtAtWidth = -1;

   private MCategory(Category category, String icon, String texture, int index, int perRow) {
      this.category = category;
      this.name = category.name();
      this.icon = icon;
      this.texture = texture;
      this.posX = 20 + index % perRow * 120;
      this.posY = 20 + index / perRow * 20;
      float x = this.posX;
      float y = this.posY;
      TenacityGuiModule module = TenacityGuiModule.get();
      int ordinal = category.ordinal();
      if (module != null) {
         if (module.panelX.length > ordinal && module.panelX[ordinal] != Integer.MIN_VALUE) {
            x = module.panelX[ordinal];
         }

         if (module.panelY.length > ordinal && module.panelY[ordinal] != Integer.MIN_VALUE) {
            y = module.panelY[ordinal];
         }
      }

      ScaledResolution sr = new ScaledResolution(Minecraft.getInstance());
      x = Math.max(0.0F, Math.min(x, (float)(sr.func_78326_a() - 105)));
      y = Math.max(0.0F, Math.min(y, (float)(sr.func_78328_b() - 15)));
      this.drag = new Drag(x, y);
   }

   public Drag getDrag() {
      return this.drag;
   }

   public Scroll getScroll() {
      return this.scroll;
   }

   public static List<MCategory> values() {
      ScaledResolution sr = new ScaledResolution(Minecraft.getInstance());
      int scaledWidth = sr.func_78326_a();
      if (values != null && scaledWidth != builtAtWidth) {
         savePositions();
         values = null;
      }

      if (values == null) {
         builtAtWidth = scaledWidth;
         values = new ArrayList<>();
         Category[] categories = Category.values();
         int perRow = Math.max(1, (scaledWidth - 30) / 120);

         for (int i = 0; i < categories.length; i++) {
            values.add(new MCategory(categories[i], glyphFor(categories[i]), textureFor(categories[i]), i, perRow));
         }
      }

      return values;
   }

   public static void invalidate() {
      values = null;
      builtAtWidth = -1;
   }

   public static void savePositions() {
      TenacityGuiModule module = TenacityGuiModule.get();
      if (module != null && values != null) {
         int size = Category.values().length;
         if (module.panelX.length != size) {
            module.panelX = filled(size);
         }

         if (module.panelY.length != size) {
            module.panelY = filled(size);
         }

         boolean changed = false;

         for (MCategory c : values) {
            int ordinal = c.category.ordinal();
            int x = (int)c.getDrag().getX();
            int y = (int)c.getDrag().getY();
            if (module.panelX[ordinal] != x || module.panelY[ordinal] != y) {
               module.panelX[ordinal] = x;
               module.panelY[ordinal] = y;
               changed = true;
            }
         }

         if (changed) {
            TenacityConfig.save();
         }
      }
   }

   private static int[] filled(int size) {
      int[] array = new int[size];
      Arrays.fill(array, Integer.MIN_VALUE);
      return array;
   }

   public float iconWidth() {
      return this.texture != null && !this.textureFailed ? 10.0F : Fonts.iconFont20.getStringWidth(this.icon);
   }

   public void drawIcon(float centerX, float y, float headerHeight, int color) {
      RenderUtil.color(color);
      if (this.texture != null && !this.textureFailed) {
         try {
            float size = 10.0F;
            TextureUtil.drawResource(Identifier.fromNamespaceAndPath("meowtils",this.texture), centerX - size / 2.0F, y + headerHeight / 2.0F - size / 2.0F, size, size);
            RenderUtil.resetColor();
            return;
         } catch (Throwable var6) {
            this.textureFailed = true;
            Meowtils.error("Tenacity GUI could not load category icon " + this.texture);
         }
      }

      Fonts.iconFont20.drawCenteredString(this.icon, centerX, y + Fonts.iconFont20.getMiddleOfBox(headerHeight) + this.glyphNudge(), color);
   }

   private float glyphNudge() {
      if ("f".equals(this.icon) || "e".equals(this.icon) || "b".equals(this.icon)) {
         return 0.5F;
      } else {
         return !"d".equals(this.icon) && !"a".equals(this.icon) && !"g".equals(this.icon) ? 0.0F : 1.0F;
      }
   }

   private static String textureFor(Category category) {
      switch (category) {
         case Meowtils:
            return "textures/gui/icons/meowtils.png";
         case Hypixel:
            return "textures/gui/icons/hypixel.png";
         case Bedwars:
            return "textures/gui/icons/bedwars.png";
         default:
            return null;
      }
   }

   private static String glyphFor(Category category) {
      switch (category) {
         case Skywars:
            return "c";
         case Render:
            return "d";
         case Antisnipe:
            return "f";
         case Utility:
            return "n";
         case Advanced:
            return "a";
         case Extensions:
            return "g";
         default:
            return "b";
      }
   }
}
