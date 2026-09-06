package meowtils.tenacitygui;

import meowtils.tenacitygui.tenacity.GuiHook;
import meowtils.tenacitygui.tenacity.TenacityGuiModule;
import wtf.tatp.meowtils.extension.Extension;

public class Main {
   public static void init() {
      Extension.registerModule(new TenacityGuiModule());
      Extension.registerEvent(new GuiHook());
   }
}
