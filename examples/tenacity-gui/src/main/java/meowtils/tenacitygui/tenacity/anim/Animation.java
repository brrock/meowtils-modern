package meowtils.tenacitygui.tenacity.anim;

import meowtils.tenacitygui.tenacity.util.TimerUtil;

public abstract class Animation {
   public TimerUtil timerUtil = new TimerUtil();
   protected int duration;
   protected double endPoint;
   protected Direction direction;

   public Animation(int ms, double endPoint) {
      this(ms, endPoint, Direction.FORWARDS);
   }

   public Animation(int ms, double endPoint, Direction direction) {
      this.duration = ms;
      this.endPoint = endPoint;
      this.direction = direction;
   }

   public boolean finished(Direction direction) {
      return this.isDone() && this.direction.equals(direction);
   }

   public double getLinearOutput() {
      return 1.0 - (double)this.timerUtil.getTime() / this.duration * this.endPoint;
   }

   public double getEndPoint() {
      return this.endPoint;
   }

   public void setEndPoint(double endPoint) {
      this.endPoint = endPoint;
   }

   public void reset() {
      this.timerUtil.reset();
   }

   public boolean isDone() {
      return this.timerUtil.hasTimeElapsed(this.duration);
   }

   public void changeDirection() {
      this.setDirection(this.direction.opposite());
   }

   public Direction getDirection() {
      return this.direction;
   }

   public Animation setDirection(Direction direction) {
      if (this.direction != direction) {
         this.direction = direction;
         this.timerUtil.setTime(System.currentTimeMillis() - (this.duration - Math.min((long)this.duration, this.timerUtil.getTime())));
      }

      return this;
   }

   public void setDuration(int duration) {
      this.duration = duration;
   }

   protected boolean correctOutput() {
      return false;
   }

   public Double getOutput() {
      if (this.direction.forwards()) {
         return this.isDone() ? this.endPoint : this.getEquation((double)this.timerUtil.getTime() / this.duration) * this.endPoint;
      } else if (this.isDone()) {
         return 0.0;
      } else if (this.correctOutput()) {
         double revTime = Math.min((long)this.duration, Math.max(0L, this.duration - this.timerUtil.getTime()));
         return this.getEquation(revTime / this.duration) * this.endPoint;
      } else {
         return (1.0 - this.getEquation((double)this.timerUtil.getTime() / this.duration)) * this.endPoint;
      }
   }

   protected abstract double getEquation(double var1);
}
