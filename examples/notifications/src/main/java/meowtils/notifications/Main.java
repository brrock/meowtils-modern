package meowtils.notifications;

import meowtils.notifications.tenacity.NotificationHook;
import wtf.tatp.meowtils.extension.Extension;

public class Main {
   public static void init() {
      Extension.registerModule(new NotificationsExtension());
      Extension.registerEvent(new NotificationHook());
   }
}
