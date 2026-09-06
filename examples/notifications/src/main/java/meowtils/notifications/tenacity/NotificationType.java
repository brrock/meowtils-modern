package meowtils.notifications.tenacity;

import java.awt.Color;

public enum NotificationType {
   SUCCESS(new Color(20, 250, 90), "o"),
   DISABLE(new Color(255, 30, 30), "p"),
   INFO(Color.WHITE, "m"),
   WARNING(Color.YELLOW, "r");

   private final Color color;
   private final String icon;

   private NotificationType(Color color, String icon) {
      this.color = color;
      this.icon = icon;
   }

   public Color getColor() {
      return this.color;
   }

   public String getIcon() {
      return this.icon;
   }
}
