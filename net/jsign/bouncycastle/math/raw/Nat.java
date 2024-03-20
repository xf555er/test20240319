package net.jsign.bouncycastle.math.raw;

import java.math.BigInteger;
import net.jsign.bouncycastle.util.Pack;

public abstract class Nat {
   public static int[] create(int var0) {
      return new int[var0];
   }

   public static int equalTo(int var0, int[] var1, int var2) {
      int var3 = var1[0] ^ var2;

      for(int var4 = 1; var4 < var0; ++var4) {
         var3 |= var1[var4];
      }

      var3 = var3 >>> 1 | var3 & 1;
      return var3 - 1 >> 31;
   }

   public static int equalToZero(int var0, int[] var1) {
      int var2 = 0;

      for(int var3 = 0; var3 < var0; ++var3) {
         var2 |= var1[var3];
      }

      var2 = var2 >>> 1 | var2 & 1;
      return var2 - 1 >> 31;
   }

   public static int[] fromBigInteger(int var0, BigInteger var1) {
      if (var1.signum() >= 0 && var1.bitLength() <= var0) {
         int var2 = var0 + 31 >> 5;
         int[] var3 = create(var2);

         for(int var4 = 0; var4 < var2; ++var4) {
            var3[var4] = var1.intValue();
            var1 = var1.shiftRight(32);
         }

         return var3;
      } else {
         throw new IllegalArgumentException();
      }
   }

   public static BigInteger toBigInteger(int var0, int[] var1) {
      byte[] var2 = new byte[var0 << 2];

      for(int var3 = 0; var3 < var0; ++var3) {
         int var4 = var1[var3];
         if (var4 != 0) {
            Pack.intToBigEndian(var4, var2, var0 - 1 - var3 << 2);
         }
      }

      return new BigInteger(1, var2);
   }
}
