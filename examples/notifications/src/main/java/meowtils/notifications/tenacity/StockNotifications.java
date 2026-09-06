package meowtils.notifications.tenacity;
import java.util.concurrent.ConcurrentLinkedQueue;
import wtf.tatp.meowtils.event.NotificationEvent;
import wtf.tatp.meowtils.manager.NotificationManager;
public final class StockNotifications {
 private static final ConcurrentLinkedQueue<Captured> pending=new ConcurrentLinkedQueue<>();
 public static void capture(NotificationEvent event){pending.add(new Captured(event.getTitle(),event.getMessage(),event.getType().name(),event.getDuration()));event.setCancelled(true);}
 public static Captured consume(){return pending.poll();}
 public static void clear(){pending.clear();}
 public static boolean available(){return true;}
 public static boolean show(String title,String message,String type,long duration){NotificationManager.show(title,message,NotificationManager.Type.valueOf(type),duration);return true;}
 public static final class Captured {
  public final String title,message,type;public final long displayTime;
  Captured(String title,String message,String type,long duration){this.title=title;this.message=message;this.type=type;displayTime=duration;}
 }
}
