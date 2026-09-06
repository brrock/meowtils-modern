package meowtils.notifications.vape;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import meowtils.notifications.NotificationsExtension;
import meowtils.notifications.tenacity.font.Fonts;
import wtf.tatp.meowtils.extension.render.ScaledResolution;

public final class VapeRenderer {
   private static final int CAPACITY = 20;
   private static final double GAP = 1.5;
   private static final double BOTTOM_MARGIN = 14.5;
   private static final CopyOnWriteArrayList<VapeNotification> notifications = new CopyOnWriteArrayList<>();
   private static long lastRenderTime;
   private static final long MAX_FRAME_MILLIS = 50L;

   private VapeRenderer() {
   }

   public static CopyOnWriteArrayList<VapeNotification> getNotifications() {
      return notifications;
   }

   public static void clear() {
      notifications.clear();
   }

   private static double offset() {
      NotificationsExtension settings = NotificationsExtension.get();
      return settings == null ? 0.0 : settings.offset;
   }

   public static void post(VapeType type, String title, String message, long durationMillis) {
      if (notifications.isEmpty()) {
         lastRenderTime = 0L;
      }

      VapeNotification notification = new VapeNotification(type, title, message, durationMillis);
      double initialY = notification.getHeight() + 16.0 + offset();

      for (VapeNotification queued : notifications) {
         initialY += queued.getHeight() + 1.5;
      }

      notification.setCurrentY(-initialY);
      notification.setCurrentX(0.0);
      notification.setTargetX(-notification.getWidth());
      if (notifications.size() >= 20 && !notifications.isEmpty()) {
         notifications.remove(0);
      }

      notifications.add(notification);
   }

   public static void render(ScaledResolution sr) {
      if (Fonts.ready() && !notifications.isEmpty()) {
         float anchorX = sr.func_78326_a();
         float anchorY = sr.func_78328_b();
         int scale = sr.func_78325_e();
         long now = System.currentTimeMillis();
         long elapsed = lastRenderTime == 0L ? 16L : Math.min(now - lastRenderTime, 50L);
         lastRenderTime = now;
         double nextTargetY = -(14.5 + offset());
         List<VapeNotification> expired = new ArrayList<>();

         for (VapeNotification notification : notifications) {
            nextTargetY -= notification.getHeight() + 1.5;
            notification.setTargetY(nextTargetY);
            int horizontalStep = (int)(Math.abs(notification.getTargetX() - notification.getCurrentX()) * 0.3);
            int verticalStep = (int)(Math.abs(notification.getTargetY() - notification.getCurrentY()) * 0.3);
            notification.setCurrentX(interpolateToward(notification.getTargetX(), notification.getCurrentX(), elapsed, horizontalStep));
            notification.setCurrentY(interpolateToward(notification.getTargetY(), notification.getCurrentY(), elapsed, verticalStep));
            notification.render(anchorX, anchorY, scale);
            if (notification.shouldRemove()) {
               expired.add(notification);
            }
         }

         notifications.removeAll(expired);
      } else {
         lastRenderTime = 0L;
      }
   }

   public static double interpolateToward(double target, double current, long elapsedMillis, double maxStep) {
      if (target == current) {
         return target;
      } else {
         double scaledStep = Math.max(maxStep * Math.max(1L, elapsedMillis) / 16.666666666666668, 0.1);
         return current + clamp(target - current, -scaledStep, scaledStep);
      }
   }

   private static double clamp(double value, double min, double max) {
      return value < min ? min : (value > max ? max : value);
   }
}
