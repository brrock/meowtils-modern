package meowtils.tenacitygui.tenacity.util;

import java.util.Objects;

public final class MutablePair<A, B> {
   private A first;
   private B second;

   private MutablePair(A first, B second) {
      this.first = first;
      this.second = second;
   }

   public static <A, B> MutablePair<A, B> of(A a, B b) {
      return new MutablePair<>(a, b);
   }

   public A getFirst() {
      return this.first;
   }

   public B getSecond() {
      return this.second;
   }

   public void setFirst(A first) {
      this.first = first;
   }

   public void setSecond(B second) {
      this.second = second;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.first, this.second);
   }

   @Override
   public boolean equals(Object that) {
      if (this == that) {
         return true;
      } else if (!(that instanceof MutablePair)) {
         return false;
      } else {
         MutablePair<?, ?> other = (MutablePair<?, ?>)that;
         return Objects.equals(this.first, other.first) && Objects.equals(this.second, other.second);
      }
   }
}
