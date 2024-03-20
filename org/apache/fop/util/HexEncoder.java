package org.apache.fop.util;

public final class HexEncoder {
   private HexEncoder() {
   }

   public static String encode(int n, int width) {
      char[] digits = new char[width];

      for(int i = width - 1; i >= 0; --i) {
         int digit = n & 15;
         digits[i] = (char)(digit < 10 ? 48 + digit : 65 + digit - 10);
         n >>= 4;
      }

      return new String(digits);
   }

   public static String encode(int c) {
      return CharUtilities.isBmpCodePoint(c) ? encode(c, 4) : encode(c, 6);
   }
}
