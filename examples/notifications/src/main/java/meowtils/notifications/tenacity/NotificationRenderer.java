package meowtils.notifications.tenacity;

import meowtils.notifications.NotificationsExtension;
import meowtils.notifications.tenacity.anim.Animation;
import meowtils.notifications.tenacity.anim.Direction;
import meowtils.notifications.tenacity.anim.impl.DecelerateAnimation;
import meowtils.notifications.tenacity.font.Fonts;
import meowtils.notifications.vape.VapeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import wtf.tatp.meowtils.extension.render.ScaledResolution;
import wtf.tatp.meowtils.extension.render.GlStateManager;

public final class NotificationRenderer {
   private static final Minecraft mc = Minecraft.getInstance();
   private static final Animation chatAnimation = new DecelerateAnimation(175, 1.0, Direction.BACKWARDS);
   private static int frameToken;
   private static int lastDrawnFrame = -1;

   private NotificationRenderer() {
   }

   public static void markFrame() {
      frameToken++;
   }

   public static void render() {
      NotificationsExtension settings = NotificationsExtension.get();
      if (settings != null) {
         if (lastDrawnFrame != frameToken) {
            lastDrawnFrame = frameToken;
            Fonts.init();
            if (Fonts.ready()) {
               chatAnimation.setDirection(mc.gui.screen() instanceof ChatScreen ? Direction.FORWARDS : Direction.BACKWARDS);
               boolean vape = settings.isStyle("Vape V4");
               if (!NotificationManager.getNotifications().isEmpty() || vape && !VapeRenderer.getNotifications().isEmpty()) {
                  ScaledResolution sr = new ScaledResolution(mc);
                  float chatOffset = 15.0F * chatAnimation.getOutput().floatValue();
                  GlStateManager.func_179094_E();
                  GlStateManager.func_179147_l();
                  GlStateManager.func_179140_f();
                  GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);

                  try {
                     if (settings.isStyle("Vape V4")) {
                        VapeRenderer.render(sr);
                     } else {
                        draw(settings, sr, chatOffset);
                     }
                  } finally {
                     GlStateManager.func_179084_k();
                     GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
                     GlStateManager.func_179121_F();
                  }
               }
            }
         }
      }
   }

   private static void draw(NotificationsExtension settings, ScaledResolution sr, float chatOffset) {
      float yOffset = 0.0F;

      for (Notification notification : NotificationManager.getNotifications()) {
         Animation animation = notification.getAnimation();
         animation.setDirection(notification.getTimerUtil().hasTimeElapsed((long)notification.getTime()) ? Direction.BACKWARDS : Direction.FORWARDS);
         if (animation.finished(Direction.BACKWARDS)) {
            NotificationManager.getNotifications().remove(notification);
         } else {
            float progress = animation.getOutput().floatValue();
            animation.setDuration(250);
            int actualOffset = 8;
            int notificationHeight = 28;
            int notificationWidth = notification.getMeasuredWidth();
            float x = sr.func_78326_a() - (notificationWidth + 5) * progress;
            float y = sr.func_78328_b() - (yOffset + 18.0F + settings.offset + notificationHeight + chatOffset);
            notification.drawDefault(x, y, notificationWidth, notificationHeight, progress);
            yOffset += (notificationHeight + actualOffset) * progress;
         }
      }
   }
}
