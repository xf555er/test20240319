package net.jsign.bouncycastle.math.ec;

import net.jsign.bouncycastle.math.field.FiniteField;
import net.jsign.bouncycastle.math.field.PolynomialExtensionField;

public class ECAlgorithms {
   public static boolean isF2mCurve(ECCurve var0) {
      return isF2mField(var0.getField());
   }

   public static boolean isF2mField(FiniteField var0) {
      return var0.getDimension() > 1 && var0.getCharacteristic().equals(ECConstants.TWO) && var0 instanceof PolynomialExtensionField;
   }

   public static boolean isFpCurve(ECCurve var0) {
      return isFpField(var0.getField());
   }

   public static boolean isFpField(FiniteField var0) {
      return var0.getDimension() == 1;
   }

   public static void montgomeryTrick(ECFieldElement[] var0, int var1, int var2, ECFieldElement var3) {
      ECFieldElement[] var4 = new ECFieldElement[var2];
      var4[0] = var0[var1];
      int var5 = 0;

      while(true) {
         ++var5;
         if (var5 >= var2) {
            --var5;
            if (var3 != null) {
               var4[var5] = var4[var5].multiply(var3);
            }

            ECFieldElement var6;
            ECFieldElement var8;
            for(var6 = var4[var5].invert(); var5 > 0; var6 = var6.multiply(var8)) {
               int var7 = var1 + var5--;
               var8 = var0[var7];
               var0[var7] = var4[var5].multiply(var6);
            }

            var0[var1] = var6;
            return;
         }

         var4[var5] = var4[var5 - 1].multiply(var0[var1 + var5]);
      }
   }
}
