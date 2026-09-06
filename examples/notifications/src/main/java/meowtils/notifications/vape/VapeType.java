package meowtils.notifications.vape;

import java.awt.Color;

public enum VapeType {
   INFO(new Color(-1, true), "noti_info"),
   WARNING(new Color(-1277652, true), "noti_warning"),
   ALERT(new Color(-380360, true), "noti_alert");

   private final Color color;
   private final String iconResource;

   private VapeType(Color color, String iconResource) {
      this.color = color;
      this.iconResource = iconResource;
   }

   public Color getColor() {
      return this.color;
   }

   public String getIconResource() {
      return this.iconResource;
   }
}
