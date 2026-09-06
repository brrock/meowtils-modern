package meowtils.notifications.tenacity;

import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {
   private static float toggleTime = 2.0F;
   private static final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();

   public static CopyOnWriteArrayList<Notification> getNotifications() {
      return notifications;
   }

   public static float getToggleTime() {
      return toggleTime;
   }

   public static void setToggleTime(float time) {
      toggleTime = time;
   }

   public static void post(NotificationType type, String title, String description) {
      notifications.add(new Notification(type, title, description));
   }

   public static void post(NotificationType type, String title, String description, float time) {
      notifications.add(new Notification(type, title, description, time));
   }

   public static void clear() {
      notifications.clear();
   }
}
