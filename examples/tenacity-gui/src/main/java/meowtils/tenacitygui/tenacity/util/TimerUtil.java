package meowtils.tenacitygui.tenacity.util;

public class TimerUtil {
   private long time = System.currentTimeMillis();

   public boolean hasTimeElapsed(long time) {
      return System.currentTimeMillis() - this.time >= time;
   }

   public boolean hasTimeElapsed(long time, boolean reset) {
      if (this.hasTimeElapsed(time)) {
         if (reset) {
            this.reset();
         }

         return true;
      } else {
         return false;
      }
   }

   public long getTime() {
      return System.currentTimeMillis() - this.time;
   }

   public void setTime(long time) {
      this.time = time;
   }

   public void reset() {
      this.time = System.currentTimeMillis();
   }
}
