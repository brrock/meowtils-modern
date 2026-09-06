package meowtils.tenacitygui.tenacity.util;

public class Drag {
   private float x;
   private float y;
   private float initialX;
   private float initialY;
   private float startX;
   private float startY;
   private boolean dragging;

   public Drag(float initialXVal, float initialYVal) {
      this.initialX = initialXVal;
      this.initialY = initialYVal;
      this.x = initialXVal;
      this.y = initialYVal;
   }

   public final void onDraw(int mouseX, int mouseY) {
      if (this.dragging) {
         this.x = mouseX - this.startX;
         this.y = mouseY - this.startY;
      }
   }

   public final void onClick(int mouseX, int mouseY, int button, boolean canDrag) {
      if (button == 0 && canDrag) {
         this.dragging = true;
         this.startX = (int)(mouseX - this.x);
         this.startY = (int)(mouseY - this.y);
      }
   }

   public final void onRelease(int button) {
      if (button == 0) {
         this.dragging = false;
      }
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public void setX(float x) {
      this.x = x;
   }

   public void setY(float y) {
      this.y = y;
   }

   public float getInitialX() {
      return this.initialX;
   }

   public float getInitialY() {
      return this.initialY;
   }

   public boolean isDragging() {
      return this.dragging;
   }
}
