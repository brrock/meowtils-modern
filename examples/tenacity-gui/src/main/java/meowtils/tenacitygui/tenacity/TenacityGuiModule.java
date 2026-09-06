package meowtils.tenacitygui.tenacity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import meowtils.tenacitygui.tenacity.render.Theme;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.extension.Extension;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.module.meowtils.GUI;

public class TenacityGuiModule extends Extension {
   private static TenacityGuiModule instance;
   @Config
   public boolean enabled = true;
   @Config
   public int key = 0;
   @Config
   public String theme = "Tenacity";
   @Config
   public boolean transparent = false;
   @Config
   public boolean outlineAccent = false;
   @Config
   public String scrollMode = "Screen";
   @Config
   public float clickHeight = 200.0F;
   @Config
   public boolean rescale = false;
   @Config
   public int color1R = 97;
   @Config
   public int color1G = 194;
   @Config
   public int color1B = 162;
   @Config
   public int color2R = 65;
   @Config
   public int color2G = 130;
   @Config
   public int color2B = 108;
   public int[] panelX = new int[0];
   public int[] panelY = new int[0];
   private ColorLink color1Link;
   private ColorLink color2Link;

   public TenacityGuiModule() {
      super("Tenacity GUI", "Mega");
      instance = this;
      this.info("Replaces Meowtils ClickGUI with Tenacity.");
      this.color1Link = this.linkColor("color1R", "color1G", "color1B");
      this.color2Link = this.linkColor("color2R", "color2G", "color2B");
      Module host = this.host();
      removeStaleSettings(host);
      this.addAppearanceSettings(host);
   }

   private static void removeStaleSettings(Module host) {
      java.util.List<wtf.tatp.meowtils.gui.values.Value<?>> keep=new java.util.ArrayList<>();
      for(Object value:host.getOrderedValues()){Object owner=ownerOf(value);if(owner==null || !owner.getClass().getName().equals(TenacityGuiModule.class.getName()))keep.add((wtf.tatp.meowtils.gui.values.Value<?>)value);}
      host.replaceSettings(keep);
   }

   private static void stripOwned(List<?> values) {
      if (values != null) {
         Iterator<?> iterator = values.iterator();

         while (iterator.hasNext()) {
            Object owner = ownerOf(iterator.next());
            if (owner != null && owner.getClass().getName().equals(TenacityGuiModule.class.getName())) {
               iterator.remove();
            }
         }
      }
   }

   private static Object ownerOf(Object value) {
      if (value == null) {
         return null;
      } else {
         Class<?> type = value.getClass();

         while (type != null) {
            try {
               Field field = type.getDeclaredField("owner");
               field.setAccessible(true);
               return field.get(value);
            } catch (NoSuchFieldException var3) {
               type = type.getSuperclass();
            } catch (Throwable var4) {
               return null;
            }
         }

         return null;
      }
   }

   private Module host() {
      try {
         Module module=Module.get(GUI.class);return module==null?this:module;
      } catch (Throwable var2) {
         return this;
      }
   }

   private void addAppearanceSettings(Module host) {
      host.addMode(new ModeValue("Theme", Theme.names(), "theme", this));
      host.addExpand(new ExpandValue("Custom theme", e -> {
         e.addColor(new ColorValue("Primary hue", this.color1Link));
         e.addSaturation(new SaturationValue(this.color1Link));
         e.addBrightness(new BrightnessValue(this.color1Link));
         e.addColor(new ColorValue("Secondary hue", this.color2Link));
         e.addSaturation(new SaturationValue(this.color2Link));
         e.addBrightness(new BrightnessValue(this.color2Link));
      }, this));
      host.addToggle(new ToggleValue("Transparent", "transparent", this));
      host.addToggle(new ToggleValue("Outline accent", "outlineAccent", this));
      host.addToggle(new ToggleValue("Rescale GUI", "rescale", this));
      host.addMode(new ModeValue("Scroll mode", Arrays.asList("Screen", "Value"), "scrollMode", this));
      host.addSlider(new SliderValue("Panel height", 100.0, 500.0, 5.0, "px", "clickHeight", this, float.class));
   }

   public static void removeAppearanceSettings() {
      if(instance!=null)removeStaleSettings(instance.host());
   }

   public static TenacityGuiModule get() {
      return instance;
   }

   public ColorLink getColor1Link() {
      return this.color1Link;
   }

   public ColorLink getColor2Link() {
      return this.color2Link;
   }
}
