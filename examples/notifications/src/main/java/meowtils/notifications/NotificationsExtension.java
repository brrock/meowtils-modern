package meowtils.notifications;

import java.util.Arrays;
import java.util.List;
import meowtils.notifications.tenacity.NotificationHook;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.extension.Extension;

public class NotificationsExtension extends Extension {
   public static final String TENACITY_5 = "Tenacity 5.0";
   public static final String VAPE_V4 = "Vape V4";
   private static final List<String> STYLES = Arrays.asList("Tenacity 5.0", "Vape V4");
   private static NotificationsExtension instance;
   @Config
   public boolean enabled = true;
   @Config
   public int key = 0;
   @Config
   public String style = "Tenacity 5.0";
   @Config
   public float offset = 0.0F;

   public NotificationsExtension() {
      super("Notifications+", "Mega");
      instance = this;
      this.info("Replaces Meowtils notifications with custom ones.");
      this.mode("Style", STYLES, "style");
      this.slider("Y offset", 0.0, 100.0, 1.0, "px", "offset", float.class);
      this.button("Test notifications", 5.0F, new Runnable() {
         @Override
         public void run() {
            NotificationHook.queueTests();
         }
      });
   }

   public static NotificationsExtension get() {
      return instance;
   }

   public String style() {
      if (!STYLES.contains(this.style)) {
         this.style = "Tenacity 5.0";
      }

      return this.style;
   }

   public boolean isStyle(String name) {
      return name.equals(this.style());
   }
}
