package meowtils.notifications.tenacity;

import java.awt.Color;
import meowtils.notifications.tenacity.anim.Animation;
import meowtils.notifications.tenacity.anim.impl.DecelerateAnimation;
import meowtils.notifications.tenacity.font.CustomFont;
import meowtils.notifications.tenacity.font.Fonts;
import meowtils.notifications.tenacity.render.ColorUtil;
import meowtils.notifications.tenacity.render.RoundedUtil;
import meowtils.notifications.tenacity.util.TimerUtil;

public class Notification {
   private final NotificationType notificationType;
   private final String title;
   private final String description;
   private final float time;
   private final TimerUtil timerUtil;
   private final Animation animation;
   private int measuredWidth = -1;

   public Notification(NotificationType type, String title, String description) {
      this(type, title, description, NotificationManager.getToggleTime());
   }

   public Notification(NotificationType type, String title, String description, float time) {
      this.title = title;
      this.description = description;
      this.time = (float)((long)(time * 1000.0F));
      this.timerUtil = new TimerUtil();
      this.notificationType = type;
      this.animation = new DecelerateAnimation(250, 1.0);
   }

   public NotificationType getNotificationType() {
      return this.notificationType;
   }

   public String getTitle() {
      return this.title;
   }

   public String getDescription() {
      return this.description;
   }

   public float getTime() {
      return this.time;
   }

   public TimerUtil getTimerUtil() {
      return this.timerUtil;
   }

   public int getMeasuredWidth() {
      if (this.measuredWidth < 0) {
         this.measuredWidth = (int)Math.max(Fonts.tenacityBoldFont22.getStringWidth(this.title), Fonts.tenacityFont18.getStringWidth(this.description)) + 35;
      }

      return this.measuredWidth;
   }

   public Animation getAnimation() {
      return this.animation;
   }

   public void drawDefault(float x, float y, float width, float height, float alpha) {
      x = Pixels.snap(x);
      y = Pixels.snap(y);
      Color color = ColorUtil.applyOpacity(ColorUtil.interpolateColorC(Color.BLACK, this.getNotificationType().getColor(), 0.65F), 0.7F * alpha);
      RoundedUtil.drawRound(x, y, width, height, 4.0F, color);
      Color notificationColor = ColorUtil.applyOpacity(this.getNotificationType().getColor(), alpha);
      Color textColor = ColorUtil.applyOpacity(Color.WHITE, alpha);
      CustomFont icon = Fonts.iconFont35;
      CustomFont titleFont = Fonts.tenacityBoldFont22;
      CustomFont descFont = Fonts.tenacityFont18;
      String glyph = this.getNotificationType().getIcon();
      float iconX = Pixels.snap(x + 5.0F);
      float iconY = Pixels.snap(y + Pixels.offset(icon.getMiddleOfBox(height) + 1.0F));
      icon.drawString(glyph, iconX, iconY, notificationColor);
      float textX = Pixels.snap(x + 10.0F + Pixels.offset(icon.getStringWidth(glyph)));
      titleFont.drawString(this.getTitle(), textX, Pixels.snap(y + 4.0F), textColor);
      descFont.drawString(this.getDescription(), textX, Pixels.snap(y + 7.0F + titleFont.getHeight()), textColor);
   }
}
