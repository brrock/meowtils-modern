package meowtils.tenacitygui.tenacity.search;

import java.util.List;

public final class FuzzySearch {
   private FuzzySearch() {
   }

   public static int extractOne(String query, List<String> choices) {
      int best = 0;

      for (String choice : choices) {
         int score = weightedRatio(query, choice);
         if (score > best) {
            best = score;
         }
      }

      return best;
   }

   public static int weightedRatio(String a, String b) {
      if (a != null && b != null) {
         a = a.toLowerCase();
         b = b.toLowerCase();
         return !a.isEmpty() && !b.isEmpty() ? Math.max(ratio(a, b), partialRatio(a, b)) : 0;
      } else {
         return 0;
      }
   }

   public static int ratio(String a, String b) {
      int distance = levenshtein(a, b);
      int max = Math.max(a.length(), b.length());
      return max == 0 ? 100 : Math.round(100.0F * (max - distance) / max);
   }

   public static int partialRatio(String a, String b) {
      String shorter = a.length() <= b.length() ? a : b;
      String longer = a.length() <= b.length() ? b : a;
      int len = shorter.length();
      if (len == 0) {
         return 0;
      } else {
         int best = 0;

         for (int i = 0; i + len <= longer.length(); i++) {
            int score = ratio(shorter, longer.substring(i, i + len));
            if (score > best) {
               best = score;
            }

            if (best == 100) {
               break;
            }
         }

         return best;
      }
   }

   private static int levenshtein(String a, String b) {
      int[] previous = new int[b.length() + 1];
      int[] current = new int[b.length() + 1];
      int j = 0;

      while (j <= b.length()) {
         previous[j] = j++;
      }

      for (int i = 1; i <= a.length(); i++) {
         current[0] = i;

         for (int jx = 1; jx <= b.length(); jx++) {
            int cost = a.charAt(i - 1) == b.charAt(jx - 1) ? 0 : 1;
            current[jx] = Math.min(Math.min(current[jx - 1] + 1, previous[jx] + 1), previous[jx - 1] + cost);
         }

         int[] swap = previous;
         previous = current;
         current = swap;
      }

      return previous[b.length()];
   }
}
