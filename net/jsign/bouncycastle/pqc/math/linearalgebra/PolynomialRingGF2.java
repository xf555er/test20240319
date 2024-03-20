package net.jsign.bouncycastle.pqc.math.linearalgebra;

public final class PolynomialRingGF2 {
   public static int modMultiply(int var0, int var1, int var2) {
      int var3 = 0;
      int var4 = remainder(var0, var2);
      int var5 = remainder(var1, var2);
      if (var5 != 0) {
         int var6 = 1 << degree(var2);

         while(var4 != 0) {
            byte var7 = (byte)(var4 & 1);
            if (var7 == 1) {
               var3 ^= var5;
            }

            var4 >>>= 1;
            var5 <<= 1;
            if (var5 >= var6) {
               var5 ^= var2;
            }
         }
      }

      return var3;
   }

   public static int degree(int var0) {
      int var1;
      for(var1 = -1; var0 != 0; var0 >>>= 1) {
         ++var1;
      }

      return var1;
   }

   public static int remainder(int var0, int var1) {
      int var2 = var0;
      if (var1 == 0) {
         System.err.println("Error: to be divided by 0");
         return 0;
      } else {
         while(degree(var2) >= degree(var1)) {
            var2 ^= var1 << degree(var2) - degree(var1);
         }

         return var2;
      }
   }

   public static int gcd(int var0, int var1) {
      int var2 = var0;

      int var4;
      for(int var3 = var1; var3 != 0; var3 = var4) {
         var4 = remainder(var2, var3);
         var2 = var3;
      }

      return var2;
   }

   public static boolean isIrreducible(int var0) {
      if (var0 == 0) {
         return false;
      } else {
         int var1 = degree(var0) >>> 1;
         int var2 = 2;

         for(int var3 = 0; var3 < var1; ++var3) {
            var2 = modMultiply(var2, var2, var0);
            if (gcd(var2 ^ 2, var0) != 1) {
               return false;
            }
         }

         return true;
      }
   }
}
