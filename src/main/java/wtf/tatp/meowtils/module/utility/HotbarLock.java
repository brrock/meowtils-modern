package wtf.tatp.meowtils.module.utility;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.*;
public final class HotbarLock extends Module {
 public final ModeValue mode=new ModeValue("Mode",java.util.List.of("Manual","Swords"),"mode",this);
 public final ToggleValue showLocked=new ToggleValue("Show locked slots","showLocked",this); private final boolean[] slots=new boolean[9];
 public HotbarLock(){super("HotbarLock",Category.Utility);tag(ModuleTag.LEGIT);tooltip("Lock slots in your hotbar.");addMode(mode);addToggle(showLocked);addExpand(new ExpandValue("Slots",e->{for(int i=0;i<9;i++)e.addCheck(new CheckValue("Slot "+(i+1),"slot"+(i+1),this));},this));java.util.Arrays.fill(slots,true);}
 public static boolean blocksDrop(){var m=get(HotbarLock.class);var c=net.minecraft.client.Minecraft.getInstance();if(m==null||!m.getState()||c.gui.screen()!=null||c.player==null)return false;if(m.mode.is("Swords"))return wtf.tatp.meowtils.util.ItemIds.isSword(c.player.getMainHandItem());int s=c.player.getInventory().getSelectedSlot();return s>=0&&s<9&&Boolean.parseBoolean(String.valueOf(m.settingsStorage().getOrDefault("slot"+(s+1),true)));}
}
