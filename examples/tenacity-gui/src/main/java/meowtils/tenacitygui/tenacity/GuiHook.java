package meowtils.tenacitygui.tenacity;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.event.RenderTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.ClickGuiScreen;
import wtf.tatp.meowtils.gui.ModuleManager;
public final class GuiHook implements AutoCloseable {
 @Override public void close(){TenacityConfig.forceSave();restore();TenacityGuiModule.removeAppearanceSettings();}
 private TenacityClickGUI screen;
 private int moduleCount=-1;
 @EventTarget public void onRenderTick(RenderTickEvent event){
  if(event.getPhase()!=RenderTickEvent.Phase.PRE)return;
  var settings=TenacityGuiModule.get();if(settings==null)return;
  TenacityConfig.load();TenacityConfig.save();
  int count=ModuleManager.getModules().size();if(count!=moduleCount){moduleCount=count;TenacityClickGUI.invalidate();}
  var mc=Minecraft.getInstance();
  if(settings.getState() && mc.gui.screen() instanceof ClickGuiScreen){if(screen==null)screen=new TenacityClickGUI();mc.gui.setScreen(screen);}
  else if(!settings.getState() && mc.gui.screen() instanceof TenacityClickGUI){TenacityClickGUI.restoreGuiScale();mc.gui.setScreen(new ClickGuiScreen());}
 }
 public static void restore(){TenacityClickGUI.restoreGuiScale();var mc=Minecraft.getInstance();if(mc.gui.screen() instanceof TenacityClickGUI)mc.gui.setScreen(null);}
}
