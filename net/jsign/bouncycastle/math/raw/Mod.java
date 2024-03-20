package net.jsign.bouncycastle.math.raw;

import net.jsign.bouncycastle.util.Integers;

public abstract class Mod {
   public static int inverse32(int var0) {
      int var1 = var0 * (2 - var0 * var0);
      var1 *= 2 - var0 * var1;
      var1 *= 2 - var0 * var1;
      var1 *= 2 - var0 * var1;
      return var1;
   }

   public static int modOddInverse(int[] var0, int[] var1, int[] var2) {
      int var3 = var0.length;
      int var4 = (var3 << 5) - Integers.numberOfLeadingZeros(var0[var3 - 1]);
      int var5 = (var4 + 29) / 30;
      int[] var6 = new int[4];
      int[] var7 = new int[var5];
      int[] var8 = new int[var5];
      int[] var9 = new int[var5];
      int[] var10 = new int[var5];
      int[] var11 = new int[var5];
      var8[0] = 1;
      encode30(var4, var1, 0, var10, 0);
      encode30(var4, var0, 0, var11, 0);
      System.arraycopy(var11, 0, var9, 0, var5);
      int var12 = -1;
      int var13 = inverse32(var11[0]);
      int var14 = getMaximumDivsteps(var4);

      int var15;
      for(var15 = 0; var15 < var14; var15 += 30) {
         var12 = divsteps30(var12, var9[0], var10[0], var6);
         updateDE30(var5, var7, var8, var6, var13, var11);
         updateFG30(var5, var9, var10, var6);
      }

      var15 = var9[var5 - 1] >> 31;
      cnegate30(var5, var15, var9);
      cnormalize30(var5, var15, var7, var11);
      decode30(var4, var7, 0, var2, 0);
      return Nat.equalTo(var5, var9, 1) & Nat.equalToZero(var5, var10);
   }

   private static void cnegate30(int var0, int var1, int[] var2) {
      int var3 = 0;
      int var4 = var0 - 1;

      for(int var5 = 0; var5 < var4; ++var5) {
         var3 += (var2[var5] ^ var1) - var1;
         var2[var5] = var3 & 1073741823;
         var3 >>= 30;
      }

      var3 += (var2[var4] ^ var1) - var1;
      var2[var4] = var3;
   }

   private static void cnormalize30(int var0, int var1, int[] var2, int[] var3) {
      int var4 = var0 - 1;
      int var5 = 0;
      int var6 = var2[var4] >> 31;

      int var7;
      int var8;
      for(var7 = 0; var7 < var4; ++var7) {
         var8 = var2[var7] + (var3[var7] & var6);
         var8 = (var8 ^ var1) - var1;
         var5 += var8;
         var2[var7] = var5 & 1073741823;
         var5 >>= 30;
      }

      var7 = var2[var4] + (var3[var4] & var6);
      var7 = (var7 ^ var1) - var1;
      var5 += var7;
      var2[var4] = var5;
      var5 = 0;
      var6 = var2[var4] >> 31;

      for(var7 = 0; var7 < var4; ++var7) {
         var8 = var2[var7] + (var3[var7] & var6);
         var5 += var8;
         var2[var7] = var5 & 1073741823;
         var5 >>= 30;
      }

      var7 = var2[var4] + (var3[var4] & var6);
      var5 += var7;
      var2[var4] = var5;
   }

   private static void decode30(int var0, int[] var1, int var2, int[] var3, int var4) {
      int var5 = 0;

      for(long var6 = 0L; var0 > 0; var0 -= 32) {
         while(var5 < Math.min(32, var0)) {
            var6 |= (long)var1[var2++] << var5;
            var5 += 30;
         }

         var3[var4++] = (int)var6;
         var6 >>>= 32;
         var5 -= 32;
      }

   }

   private static int divsteps30(int var0, int var1, int var2, int[] var3) {
      int var4 = 1;
      int var5 = 0;
      int var6 = 0;
      int var7 = 1;
      int var8 = var1;
      int var9 = var2;

      for(int var10 = 0; var10 < 30; ++var10) {
         int var11 = var0 >> 31;
         int var12 = -(var9 & 1);
         int var13 = (var8 ^ var11) - var11;
         int var14 = (var4 ^ var11) - var11;
         int var15 = (var5 ^ var11) - var11;
         var9 += var13 & var12;
         var6 += var14 & var12;
         var7 += var15 & var12;
         var11 &= var12;
         var0 = (var0 ^ var11) - (var11 + 1);
         var8 += var9 & var11;
         var4 += var6 & var11;
         var5 += var7 & var11;
         var9 >>= 1;
         var4 <<= 1;
         var5 <<= 1;
      }

      var3[0] = var4;
      var3[1] = var5;
      var3[2] = var6;
      var3[3] = var7;
      return var0;
   }

   private static void encode30(int var0, int[] var1, int var2, int[] var3, int var4) {
      int var5 = 0;

      for(long var6 = 0L; var0 > 0; var0 -= 30) {
         if (var5 < Math.min(30, var0)) {
            var6 |= ((long)var1[var2++] & 4294967295L) << var5;
            var5 += 32;
         }

         var3[var4++] = (int)var6 & 1073741823;
         var6 >>>= 30;
         var5 -= 30;
      }

   }

   private static int getMaximumDivsteps(int var0) {
      return (49 * var0 + (var0 < 46 ? 80 : 47)) / 17;
   }

   private static void updateDE30(int var0, int[] var1, int[] var2, int[] var3, int var4, int[] var5) {
      int var6 = var3[0];
      int var7 = var3[1];
      int var8 = var3[2];
      int var9 = var3[3];
      int var16 = var1[var0 - 1] >> 31;
      int var17 = var2[var0 - 1] >> 31;
      int var13 = (var6 & var16) + (var7 & var17);
      int var14 = (var8 & var16) + (var9 & var17);
      int var15 = var5[0];
      int var10 = var1[0];
      int var11 = var2[0];
      long var18 = (long)var6 * (long)var10 + (long)var7 * (long)var11;
      long var20 = (long)var8 * (long)var10 + (long)var9 * (long)var11;
      var13 -= var4 * (int)var18 + var13 & 1073741823;
      var14 -= var4 * (int)var20 + var14 & 1073741823;
      var18 += (long)var15 * (long)var13;
      var20 += (long)var15 * (long)var14;
      var18 >>= 30;
      var20 >>= 30;

      for(int var12 = 1; var12 < var0; ++var12) {
         var15 = var5[var12];
         var10 = var1[var12];
         var11 = var2[var12];
         var18 += (long)var6 * (long)var10 + (long)var7 * (long)var11 + (long)var15 * (long)var13;
         var20 += (long)var8 * (long)var10 + (long)var9 * (long)var11 + (long)var15 * (long)var14;
         var1[var12 - 1] = (int)var18 & 1073741823;
         var18 >>= 30;
         var2[var12 - 1] = (int)var20 & 1073741823;
         var20 >>= 30;
      }

      var1[var0 - 1] = (int)var18;
      var2[var0 - 1] = (int)var20;
   }

   private static void updateFG30(int var0, int[] var1, int[] var2, int[] var3) {
      int var4 = var3[0];
      int var5 = var3[1];
      int var6 = var3[2];
      int var7 = var3[3];
      int var8 = var1[0];
      int var9 = var2[0];
      long var11 = (long)var4 * (long)var8 + (long)var5 * (long)var9;
      long var13 = (long)var6 * (long)var8 + (long)var7 * (long)var9;
      var11 >>= 30;
      var13 >>= 30;

      for(int var10 = 1; var10 < var0; ++var10) {
         var8 = var1[var10];
         var9 = var2[var10];
         var11 += (long)var4 * (long)var8 + (long)var5 * (long)var9;
         var13 += (long)var6 * (long)var8 + (long)var7 * (long)var9;
         var1[var10 - 1] = (int)var11 & 1073741823;
         var11 >>= 30;
         var2[var10 - 1] = (int)var13 & 1073741823;
         var13 >>= 30;
      }

      var1[var0 - 1] = (int)var11;
      var2[var0 - 1] = (int)var13;
   }
}
