package meowtils.notifications.tenacity.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class MathUtils {
   public static final DecimalFormat DF_0 = new DecimalFormat("0");
   public static final DecimalFormat DF_1 = new DecimalFormat("0.0");
   public static final DecimalFormat DF_2 = new DecimalFormat("0.00");
   public static final DecimalFormat DF_1D = new DecimalFormat("0.#");
   public static final DecimalFormat DF_2D = new DecimalFormat("0.##");

   public static double lerp(double old, double newVal, double amount) {
      return (1.0 - amount) * old + amount * newVal;
   }

   public static Double interpolate(double oldValue, double newValue, double interpolationValue) {
      return oldValue + (newValue - oldValue) * interpolationValue;
   }

   public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
      return interpolate(oldValue, newValue, (float)interpolationValue).floatValue();
   }

   public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
      return interpolate(oldValue, newValue, (float)interpolationValue).intValue();
   }

   public static double roundToHalf(double d) {
      return Math.round(d * 2.0) / 2.0;
   }

   public static double round(double value, int places) {
      if (places < 0) {
         throw new IllegalArgumentException();
      } else {
         BigDecimal bd = new BigDecimal(value);
         bd = bd.setScale(places, RoundingMode.HALF_UP);
         return bd.doubleValue();
      }
   }

   public static int getNumberOfDecimalPlace(double value) {
      BigDecimal bigDecimal = new BigDecimal(value);
      return Math.max(0, bigDecimal.stripTrailingZeros().scale());
   }
}
