package meowtils.tenacitygui.tenacity.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import meowtils.tenacitygui.tenacity.panel.SettingComponent;
import meowtils.tenacitygui.tenacity.panel.settings.BooleanComponent;
import meowtils.tenacitygui.tenacity.panel.settings.ButtonComponent;
import meowtils.tenacitygui.tenacity.panel.settings.ColorComponent;
import meowtils.tenacitygui.tenacity.panel.settings.ExpandComponent;
import meowtils.tenacitygui.tenacity.panel.settings.KeybindComponent;
import meowtils.tenacitygui.tenacity.panel.settings.ModeComponent;
import meowtils.tenacitygui.tenacity.panel.settings.NumberComponent;
import meowtils.tenacitygui.tenacity.panel.settings.StringComponent;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.BindValue;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.ButtonValue;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.OpacityValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.TextValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.module.meowtils.GUI;

public final class ValueAdapter {
   private static final List<String> GUI_MODULE_ORDER = Arrays.asList(
      "Theme",
      "Custom theme",
      "Transparent",
      "Outline accent",
      "Rescale GUI",
      "GUI Scale",
      "Features",
      "Show tooltips",
      "Blur background",
      "Scroll speed",
      "Scroll mode",
      "Panel height",
      "Bind",
      "Reset GUI color"
   );

   private ValueAdapter() {
   }

   public static List<SettingComponent> build(List<Object> values) {
      return build(values, null);
   }

   public static List<SettingComponent> build(List<Object> values, Object owner) {
      boolean hideAccentColor = isMeowtilsGuiModule(owner);
      List<SettingComponent> components = new ArrayList<>();
      Map<ColorLink, ColorComponent> seenLinks = new IdentityHashMap<>();

      for (Object value : values) {
         if (value instanceof ToggleValue) {
            final ToggleValue v = (ToggleValue)value;
            components.add(new BooleanComponent(new BooleanComponent.BoolAccess() {
               @Override
               public String name() {
                  return v.getName();
               }

               @Override
               public boolean get() {
                  return v.getState();
               }

               @Override
               public void toggle() {
                  v.toggle();
               }
            }));
         } else if (value instanceof CheckValue) {
            final CheckValue v = (CheckValue)value;
            components.add(new BooleanComponent(new BooleanComponent.BoolAccess() {
               @Override
               public String name() {
                  return v.getName();
               }

               @Override
               public boolean get() {
                  return v.getState();
               }

               @Override
               public void toggle() {
                  v.toggle();
               }
            }));
         } else if (value instanceof SliderValue) {
            final SliderValue v = (SliderValue)value;
            components.add(new NumberComponent(new NumberComponent.NumAccess() {
               @Override
               public String name() {
                  return v.getName();
               }

               @Override
               public double get() {
                  return v.get();
               }

               @Override
               public void set(double value) {
                  v.set(value);
               }

               @Override
               public double min() {
                  return v.getMin();
               }

               @Override
               public double max() {
                  return v.getMax();
               }

               @Override
               public double increment() {
                  return v.getIncrement();
               }
            }));
         } else if (value instanceof OpacityValue) {
            final OpacityValue v = (OpacityValue)value;
            components.add(new NumberComponent(new NumberComponent.NumAccess() {
               @Override
               public String name() {
                  return v.getName();
               }

               @Override
               public double get() {
                  return v.get();
               }

               @Override
               public void set(double value) {
                  v.set(value);
               }

               @Override
               public double min() {
                  return v.getMin();
               }

               @Override
               public double max() {
                  return v.getMax();
               }

               @Override
               public double increment() {
                  return 5.0;
               }
            }));
         } else if (value instanceof ModeValue) {
            components.add(new ModeComponent((ModeValue)value));
         } else if (value instanceof TextValue) {
            components.add(new StringComponent((TextValue)value));
         } else if (value instanceof BindValue) {
            final BindValue v = (BindValue)value;
            components.add(new KeybindComponent(new KeybindComponent.BindAccess() {
               @Override
               public String name() {
                  return v.getName();
               }

               @Override
               public int get() {
                  return wtf.tatp.meowtils.extension.render.Keyboard.legacy(v.getBind());
               }

               @Override
               public void set(int code) {
                  v.setBind(wtf.tatp.meowtils.extension.render.Keyboard.modern(code));
               }
            }));
         } else if (value instanceof ButtonValue) {
            components.add(new ButtonComponent((ButtonValue)value));
         } else if (value instanceof ExpandValue) {
            components.add(new ExpandComponent((ExpandValue)value));
         } else if (value instanceof ColorValue) {
            if (!hideAccentColor) {
               ColorValue v = (ColorValue)value;
               if (!seenLinks.containsKey(v.getLink())) {
                  ColorComponent component = new ColorComponent(v.getName(), v.getLink());
                  seenLinks.put(v.getLink(), component);
                  components.add(component);
               }
            }
         } else if (value instanceof SaturationValue) {
            if (!hideAccentColor) {
               SaturationValue v = (SaturationValue)value;
               if (!seenLinks.containsKey(v.getLink())) {
                  ColorComponent component = new ColorComponent("Color", v.getLink());
                  seenLinks.put(v.getLink(), component);
                  components.add(component);
               }
            }
         } else if (value instanceof BrightnessValue && !hideAccentColor) {
            BrightnessValue v = (BrightnessValue)value;
            if (!seenLinks.containsKey(v.getLink())) {
               ColorComponent component = new ColorComponent("Color", v.getLink());
               seenLinks.put(v.getLink(), component);
               components.add(component);
            }
         }
      }

      if (hideAccentColor) {
         sortGuiModule(components);
      }

      return components;
   }

   private static void sortGuiModule(List<SettingComponent> components) {
      Collections.sort(components, new Comparator<SettingComponent>() {
         public int compare(SettingComponent a, SettingComponent b) {
            return Integer.compare(this.rank(a), this.rank(b));
         }

         private int rank(SettingComponent component) {
            int index = ValueAdapter.GUI_MODULE_ORDER.indexOf(component.getName());
            return index == -1 ? ValueAdapter.GUI_MODULE_ORDER.size() : index;
         }
      });
   }

   private static boolean isMeowtilsGuiModule(Object owner) {
      try {
         return owner != null && owner == Module.get(GUI.class);
      } catch (Throwable var2) {
         return false;
      }
   }

   public static void applyRowHeight(SettingComponent component, float rowHeight) {
      if (component instanceof ModeComponent) {
         ModeComponent mode = (ModeComponent)component;
         mode.realHeight = rowHeight * mode.normalCount;
      } else if (component instanceof ExpandComponent) {
         ExpandComponent expand = (ExpandComponent)component;
         expand.realHeight = rowHeight * expand.normalCount;
      } else if (component instanceof ColorComponent) {
         ((ColorComponent)component).realHeight = rowHeight;
      }
   }
}
