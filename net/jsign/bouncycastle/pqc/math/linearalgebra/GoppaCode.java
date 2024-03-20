package net.jsign.bouncycastle.pqc.math.linearalgebra;

public final class GoppaCode {
   public static GF2Matrix createCanonicalCheckMatrix(GF2mField var0, PolynomialGF2mSmallM var1) {
      int var2 = var0.getDegree();
      int var3 = 1 << var2;
      int var4 = var1.getDegree();
      int[][] var5 = new int[var4][var3];
      int[][] var6 = new int[var4][var3];

      int var7;
      for(var7 = 0; var7 < var3; ++var7) {
         var6[0][var7] = var0.inverse(var1.evaluateAt(var7));
      }

      int var8;
      for(var7 = 1; var7 < var4; ++var7) {
         for(var8 = 0; var8 < var3; ++var8) {
            var6[var7][var8] = var0.mult(var6[var7 - 1][var8], var8);
         }
      }

      int var9;
      for(var7 = 0; var7 < var4; ++var7) {
         for(var8 = 0; var8 < var3; ++var8) {
            for(var9 = 0; var9 <= var7; ++var9) {
               var5[var7][var8] = var0.add(var5[var7][var8], var0.mult(var6[var9][var8], var1.getCoefficient(var4 + var9 - var7)));
            }
         }
      }

      int[][] var16 = new int[var4 * var2][var3 + 31 >>> 5];

      for(var8 = 0; var8 < var3; ++var8) {
         var9 = var8 >>> 5;
         int var10 = 1 << (var8 & 31);

         for(int var11 = 0; var11 < var4; ++var11) {
            int var12 = var5[var11][var8];

            for(int var13 = 0; var13 < var2; ++var13) {
               int var14 = var12 >>> var13 & 1;
               if (var14 != 0) {
                  int var15 = (var11 + 1) * var2 - var13 - 1;
                  var16[var15][var9] ^= var10;
               }
            }
         }
      }

      return new GF2Matrix(var3, var16);
   }
}
