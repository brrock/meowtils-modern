package meowtils.tenacitygui.tenacity.anim.impl;

import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.Direction;

public class EaseOutSine extends Animation {
   public EaseOutSine(int ms, double endPoint) {
      super(ms, endPoint);
   }

   public EaseOutSine(int ms, double endPoint, Direction direction) {
      super(ms, endPoint, direction);
   }

   @Override
   protected boolean correctOutput() {
      return true;
   }

   @Override
   protected double getEquation(double x) {
      return Math.sin(x * (Math.PI / 2));
   }
}
