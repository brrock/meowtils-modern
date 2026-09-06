package wtf.tatp.meowtils.module.hypixel;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
public final class AutoWho extends Module {
 private int ticks=-1;
 public AutoWho(){super("AutoWho",Category.Hypixel);tag(ModuleTag.LEGIT);tooltip("Automatically runs /who after a game starts.");}
 @EventTarget public void onChat(ChatReceivedEvent e){if(e.isOverlay())return;if(e.getText().equals("The game starts in 1 second!"))ticks=60;}
 @EventTarget(priority=EventPriority.LOWEST) public void hide(ChatReceivedEvent e){if(!e.isOverlay()&&Boolean.parseBoolean(String.valueOf(settingsStorage().getOrDefault("hide",false)))&&(e.getText().contains("ONLINE:")||e.getText().startsWith("Team #")||e.getText().startsWith("Mode:")))e.setCancelled(true);}
 @EventTarget public void tick(ClientTickEvent e){if(e.getPhase()!=ClientTickEvent.Phase.POST||mc.player==null||mc.level==null||ticks<0)return;if(--ticks==0){wtf.tatp.meowtils.Meowtils.sendCleanMessage("/who");ticks=-1;}}
}
