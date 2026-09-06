package meowtils.tenacitygui.tenacity.util;

import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.Direction;
import meowtils.tenacitygui.tenacity.anim.impl.SmoothStepAnimation;
import wtf.tatp.meowtils.extension.render.Mouse;

public class Scroll {
   private float maxScroll = Float.MAX_VALUE;
   private float minScroll = 0.0F;
   private float rawScroll = 0.0F;
   private float scroll;
   private Animation scrollAnimation = new SmoothStepAnimation(0, 0.0, Direction.BACKWARDS);

   public void onScroll(int ms) {
      this.scroll = this.rawScroll - this.scrollAnimation.getOutput().floatValue();
      this.rawScroll = this.rawScroll + Mouse.getDWheel() / 4.0F;
      this.rawScroll = Math.max(Math.min(this.minScroll, this.rawScroll), -this.maxScroll);
      this.scrollAnimation = new SmoothStepAnimation(ms, this.rawScroll - this.scroll, Direction.BACKWARDS);
   }

   public boolean isScrollAnimationDone() {
      return this.scrollAnimation.isDone();
   }

   public float getScroll() {
      this.scroll = this.rawScroll - this.scrollAnimation.getOutput().floatValue();
      return this.scroll;
   }

   public float getMaxScroll() {
      return this.maxScroll;
   }

   public void setMaxScroll(float maxScroll) {
      this.maxScroll = maxScroll;
   }

   public float getMinScroll() {
      return this.minScroll;
   }

   public void setMinScroll(float minScroll) {
      this.minScroll = minScroll;
   }

   public float getRawScroll() {
      return this.rawScroll;
   }

   public void setRawScroll(float rawScroll) {
      this.rawScroll = rawScroll;
   }

   public Animation getScrollAnimation() {
      return this.scrollAnimation;
   }
}
