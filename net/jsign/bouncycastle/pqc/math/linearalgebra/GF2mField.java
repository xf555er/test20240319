package net.jsign.bouncycastle.pqc.math.linearalgebra;

public class GF2mField {
   private int degree = 0;
   private int polynomial;

   public GF2mField(byte[] var1) {
      if (var1.length != 4) {
         throw new IllegalArgumentException("byte array is not an encoded finite field");
      } else {
         this.polynomial = LittleEndianConversions.OS2IP(var1);
         if (!PolynomialRingGF2.isIrreducible(this.polynomial)) {
            throw new IllegalArgumentException("byte array is not an encoded finite field");
         } else {
            this.degree = PolynomialRingGF2.degree(this.polynomial);
         }
      }
   }

   public int getDegree() {
      return this.degree;
   }

   public byte[] getEncoded() {
      return LittleEndianConversions.I2OSP(this.polynomial);
   }

   public int add(int var1, int var2) {
      return var1 ^ var2;
   }

   public int mult(int var1, int var2) {
      return PolynomialRingGF2.modMultiply(var1, var2, this.polynomial);
   }

   public int exp(int var1, int var2) {
      if (var2 == 0) {
         return 1;
      } else if (var1 == 0) {
         return 0;
      } else if (var1 == 1) {
         return 1;
      } else {
         int var3 = 1;
         if (var2 < 0) {
            var1 = this.inverse(var1);
            var2 = -var2;
         }

         while(var2 != 0) {
            if ((var2 & 1) == 1) {
               var3 = this.mult(var3, var1);
            }

            var1 = this.mult(var1, var1);
            var2 >>>= 1;
         }

         return var3;
      }
   }

   public int inverse(int var1) {
      int var2 = (1 << this.degree) - 2;
      return this.exp(var1, var2);
   }

   public boolean isElementOfThisField(int var1) {
      if (this.degree == 31) {
         return var1 >= 0;
      } else {
         return var1 >= 0 && var1 < 1 << this.degree;
      }
   }

   public String elementToStr(int var1) {
      String var2 = "";

      for(int var3 = 0; var3 < this.degree; ++var3) {
         if (((byte)var1 & 1) == 0) {
            var2 = "0" + var2;
         } else {
            var2 = "1" + var2;
         }

         var1 >>>= 1;
      }

      return var2;
   }

   public boolean equals(Object var1) {
      if (var1 != null && var1 instanceof GF2mField) {
         GF2mField var2 = (GF2mField)var1;
         return this.degree == var2.degree && this.polynomial == var2.polynomial;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.polynomial;
   }

   public String toString() {
      String var1 = "Finite Field GF(2^" + this.degree + ") = GF(2)[X]/<" + polyToString(this.polynomial) + "> ";
      return var1;
   }

   private static String polyToString(int var0) {
      String var1 = "";
      if (var0 == 0) {
         var1 = "0";
      } else {
         byte var2 = (byte)(var0 & 1);
         if (var2 == 1) {
            var1 = "1";
         }

         var0 >>>= 1;

         for(int var3 = 1; var0 != 0; ++var3) {
            var2 = (byte)(var0 & 1);
            if (var2 == 1) {
               var1 = var1 + "+x^" + var3;
            }

            var0 >>>= 1;
         }
      }

      return var1;
   }
}
