package net.jsign.bouncycastle.pqc.math.linearalgebra;

import java.math.BigInteger;
import java.security.SecureRandom;

public final class IntegerFunctions {
   private static final BigInteger ZERO = BigInteger.valueOf(0L);
   private static final BigInteger ONE = BigInteger.valueOf(1L);
   private static final BigInteger TWO = BigInteger.valueOf(2L);
   private static final BigInteger FOUR = BigInteger.valueOf(4L);
   private static final int[] SMALL_PRIMES = new int[]{3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41};
   private static SecureRandom sr = null;
   private static final int[] jacobiTable = new int[]{0, 1, 0, -1, 0, -1, 0, 1};

   public static int ceilLog256(int var0) {
      if (var0 == 0) {
         return 1;
      } else {
         int var1;
         if (var0 < 0) {
            var1 = -var0;
         } else {
            var1 = var0;
         }

         int var2;
         for(var2 = 0; var1 > 0; var1 >>>= 8) {
            ++var2;
         }

         return var2;
      }
   }
}
