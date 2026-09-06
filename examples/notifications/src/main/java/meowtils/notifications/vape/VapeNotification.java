package meowtils.notifications.vape;

import java.awt.Color;
import meowtils.notifications.tenacity.font.CustomFont;
import meowtils.notifications.tenacity.font.Fonts;
import meowtils.notifications.tenacity.render.RenderUtil;
import meowtils.notifications.tenacity.render.TextureUtil;

public class VapeNotification {
   private static final float HEIGHT = 37.5F;
   private static final float MIN_WIDTH = 133.0F;
   private static final float WIDTH_PADDING = 40.0F;
   private static final float ICON_SIZE = 30.0F;
   private static final float ICON_X = -3.0F;
   private static final float ICON_Y = -4.5F;
   private static final float SHADOW = 0.5F;
   private static final float BASELINE = 3.0F;
   private static final float TITLE_X = 23.0F;
   private static final float TITLE_Y = 11.0F;
   private static final float BODY_X = 23.0F;
   private static final float BODY_Y = 24.5F;
   private static final float RADIUS = 3.5F;
   private static final int SPRITE_SIZE = 15;
   private static final int[] SPRITE_INSET = new int[]{7, 7, 6, 6};
   private static final float SPRITE_SCALE = 0.5F;
   private static final int BORDER_RGB = 3355184;
   private static final int BORDER_LUMA_THRESHOLD = 50;
   private static final Color BODY_COLOR = new Color(170, 170, 170);
   private static final Color TEXT_SHADOW = new Color(0, 0, 0, 128);
   private static final char SECTION = '§';
   private static final char CR = '\r';
   private static final char LF = '\n';
   private final VapeType type;
   private final String title;
   private final String message;
   private final String plainMessage;
   private final double width;
   private final long durationMillis;
   private static int panelTexture = -1;
   private static int frameTexture = -1;
   private final VapeBlur blur = new VapeBlur();
   private double currentX;
   private double currentY;
   private double targetX;
   private double targetY;
   private boolean started;
   private long expiresAt = Long.MAX_VALUE;

   public VapeNotification(VapeType type, String title, String message, long durationMillis) {
      this.type = type;
      this.title = title == null ? "" : title;
      this.message = strip(message == null ? "" : message, false);
      this.plainMessage = strip(this.message, true);
      this.durationMillis = Math.max(durationMillis, 1L);
      this.width = Math.max(bodyFont().getStringWidth(this.plainMessage) + 40.0F, 133.0F);
   }

   private static String strip(String text, boolean codes) {
      StringBuilder out = new StringBuilder(text.length());

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (c == 167) {
            if (codes) {
               i++;
            } else {
               out.append(c);
            }
         } else if (c != '\r' && c != '\n') {
            out.append(c);
         }
      }

      return out.toString();
   }

   private static CustomFont titleFont() {
      return Fonts.proximaBold14;
   }

   private static CustomFont bodyFont() {
      return Fonts.proxima14;
   }

   public double getHeight() {
      return 37.5;
   }

   public double getWidth() {
      return this.width;
   }

   public double getRemainingProgress() {
      return Math.max(Math.min((double)(this.expiresAt - System.currentTimeMillis()) / this.durationMillis, 1.0), 0.0);
   }

   public void dismiss() {
      this.targetX = 5.0;
   }

   public boolean shouldRemove() {
      return this.currentX >= 1.0;
   }

   public void render(float anchorX, float anchorY, int scale) {
      if (!this.started) {
         this.started = true;
         this.expiresAt = System.currentTimeMillis() + this.durationMillis;
      }

      float x = (float)(this.currentX + anchorX);
      float y = (float)(this.currentY + anchorY);
      float width = (float)this.width;
      this.blur.render(x, y, width + 3.5F, 37.5F, 10.0F, 3.5F, scale);
      if (panelTexture == -1) {
         panelTexture = TextureUtil.getBorderRecolored("notification", 3355184, 50);
      }

      TextureUtil.drawNineSlice(panelTexture, x, y, width + 3.5F, 37.5F, 15, 15, SPRITE_INSET, 0.5F);
      TextureUtil.drawImage(this.type.getIconResource(), x + -3.0F + 0.5F, y + -4.5F + 0.5F, 30.0F, 30.0F, TEXT_SHADOW);
      TextureUtil.drawImage(this.type.getIconResource(), x + -3.0F, y + -4.5F, 30.0F, 30.0F, Color.WHITE);
      Color titleColor = this.type == VapeType.ALERT ? this.type.getColor() : Color.WHITE;
      titleFont().drawString(this.title, x + 23.0F + 0.5F, y + 11.0F + 0.5F, TEXT_SHADOW);
      titleFont().drawString(this.title, x + 23.0F, y + 11.0F, titleColor);
      bodyFont().drawString(this.plainMessage, x + 23.0F + 0.5F, y + 24.5F + 0.5F, TEXT_SHADOW);
      bodyFont().drawString(this.message, x + 23.0F, y + 24.5F, BODY_COLOR);
      double progress = this.getRemainingProgress();
      if (progress > 0.0) {
         RenderUtil.drawRect2(x + 1.5F, y + 37.5F - 2.0F, (width - 6.5F) * progress, 0.5, this.type.getColor().getRGB());
         if (frameTexture == -1) {
            frameTexture = TextureUtil.getBorderRecolored("notification", 3355184, 50, true);
         }

         TextureUtil.drawNineSlice(frameTexture, x, y, width + 3.5F, 37.5F, 15, 15, SPRITE_INSET, 0.5F);
      } else {
         this.dismiss();
      }
   }

   public double getCurrentX() {
      return this.currentX;
   }

   public void setCurrentX(double currentX) {
      this.currentX = currentX;
   }

   public double getCurrentY() {
      return this.currentY;
   }

   public void setCurrentY(double currentY) {
      this.currentY = currentY;
   }

   public double getTargetX() {
      return this.targetX;
   }

   public void setTargetX(double targetX) {
      this.targetX = targetX;
   }

   public double getTargetY() {
      return this.targetY;
   }

   public void setTargetY(double targetY) {
      this.targetY = targetY;
   }
}
