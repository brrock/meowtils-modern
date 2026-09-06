package meowtils.notifications.tenacity.anim.impl;

import meowtils.notifications.tenacity.anim.Animation;
import meowtils.notifications.tenacity.anim.Direction;

public class DecelerateAnimation extends Animation {
   public DecelerateAnimation(int ms, double endPoint) {
      super(ms, endPoint);
   }

   public DecelerateAnimation(int ms, double endPoint, Direction direction) {
      super(ms, endPoint, direction);
   }

   @Override
   protected double getEquation(double x) {
      return 1.0 - (x - 1.0) * (x - 1.0);
   }

   @Override
   protected boolean correctOutput() {
      return true;
   }
}
