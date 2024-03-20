package net.jsign.bouncycastle.pqc.math.linearalgebra;

import net.jsign.bouncycastle.util.Arrays;

public class Permutation {
   private int[] perm;

   public Permutation(byte[] var1) {
      if (var1.length <= 4) {
         throw new IllegalArgumentException("invalid encoding");
      } else {
         int var2 = LittleEndianConversions.OS2IP(var1, 0);
         int var3 = IntegerFunctions.ceilLog256(var2 - 1);
         if (var1.length != 4 + var2 * var3) {
            throw new IllegalArgumentException("invalid encoding");
         } else {
            this.perm = new int[var2];

            for(int var4 = 0; var4 < var2; ++var4) {
               this.perm[var4] = LittleEndianConversions.OS2IP(var1, 4 + var4 * var3, var3);
            }

            if (!this.isPermutation(this.perm)) {
               throw new IllegalArgumentException("invalid encoding");
            }
         }
      }
   }

   public byte[] getEncoded() {
      int var1 = this.perm.length;
      int var2 = IntegerFunctions.ceilLog256(var1 - 1);
      byte[] var3 = new byte[4 + var1 * var2];
      LittleEndianConversions.I2OSP(var1, var3, 0);

      for(int var4 = 0; var4 < var1; ++var4) {
         LittleEndianConversions.I2OSP(this.perm[var4], var3, 4 + var4 * var2, var2);
      }

      return var3;
   }

   public boolean equals(Object var1) {
      if (!(var1 instanceof Permutation)) {
         return false;
      } else {
         Permutation var2 = (Permutation)var1;
         return IntUtils.equals(this.perm, var2.perm);
      }
   }

   public String toString() {
      String var1 = "[" + this.perm[0];

      for(int var2 = 1; var2 < this.perm.length; ++var2) {
         var1 = var1 + ", " + this.perm[var2];
      }

      var1 = var1 + "]";
      return var1;
   }

   public int hashCode() {
      return Arrays.hashCode(this.perm);
   }

   private boolean isPermutation(int[] var1) {
      int var2 = var1.length;
      boolean[] var3 = new boolean[var2];

      for(int var4 = 0; var4 < var2; ++var4) {
         if (var1[var4] < 0 || var1[var4] >= var2 || var3[var1[var4]]) {
            return false;
         }

         var3[var1[var4]] = true;
      }

      return true;
   }
}
