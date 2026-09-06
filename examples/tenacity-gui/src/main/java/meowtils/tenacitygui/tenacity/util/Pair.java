package meowtils.tenacitygui.tenacity.util;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public final class Pair<A, B> {
   private final A first;
   private final B second;

   private Pair(A first, B second) {
      this.first = first;
      this.second = second;
   }

   public static <A, B> Pair<A, B> of(A a, B b) {
      return new Pair<>(a, b);
   }

   public static <A> Pair<A, A> of(A a) {
      return new Pair<>(a, a);
   }

   public A getFirst() {
      return this.first;
   }

   public B getSecond() {
      return this.second;
   }

   public <R> R apply(BiFunction<? super A, ? super B, ? extends R> func) {
      return (R)func.apply(this.first, this.second);
   }

   public void use(BiConsumer<? super A, ? super B> func) {
      func.accept(this.first, this.second);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.first, this.second);
   }

   @Override
   public boolean equals(Object that) {
      if (this == that) {
         return true;
      } else if (!(that instanceof Pair)) {
         return false;
      } else {
         Pair<?, ?> other = (Pair<?, ?>)that;
         return Objects.equals(this.first, other.first) && Objects.equals(this.second, other.second);
      }
   }
}
