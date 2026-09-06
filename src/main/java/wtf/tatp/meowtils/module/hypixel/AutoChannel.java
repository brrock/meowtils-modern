package wtf.tatp.meowtils.module.hypixel;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.gui.Module;
public final class AutoChannel extends Module {
 public AutoChannel(){super("AutoChannel",Category.Hypixel);tag(ModuleTag.LEGIT);tooltip("Automatically switches chat channel when joining/leaving a party.");}
 public static void swapToAll(){var m=get(AutoChannel.class);if(m!=null&&m.getState())Meowtils.sendCleanMessage("/chat all");}
 public static void swapToParty(){var m=get(AutoChannel.class);if(m!=null&&m.getState())Meowtils.sendCleanMessage("/chat party");}
}
