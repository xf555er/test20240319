package net.jsign.bouncycastle.pqc.math.linearalgebra;

public final class IntUtils {
   public static boolean equals(int[] var0, int[] var1) {
      if (var0.length != var1.length) {
         return false;
      } else {
         boolean var2 = true;

         for(int var3 = var0.length - 1; var3 >= 0; --var3) {
            var2 &= var0[var3] == var1[var3];
         }

         return var2;
      }
   }

   public static int[] clone(int[] var0) {
      int[] var1 = new int[var0.length];
      System.arraycopy(var0, 0, var1, 0, var0.length);
      return var1;
   }
}
