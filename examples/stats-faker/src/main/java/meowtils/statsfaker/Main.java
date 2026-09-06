package meowtils.statsfaker;

import wtf.tatp.meowtils.extension.Extension;

public class Main {
   public static void init() {
      StatsFakerModule module = new StatsFakerModule();
      Extension.registerModule(module);
   }
}
