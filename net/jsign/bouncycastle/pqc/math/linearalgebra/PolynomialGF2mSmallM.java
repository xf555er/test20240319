package net.jsign.bouncycastle.pqc.math.linearalgebra;

public class PolynomialGF2mSmallM {
   private GF2mField field;
   private int degree;
   private int[] coefficients;

   public PolynomialGF2mSmallM(GF2mField var1, int var2) {
      this.field = var1;
      this.degree = var2;
      this.coefficients = new int[var2 + 1];
      this.coefficients[var2] = 1;
   }

   public PolynomialGF2mSmallM(GF2mField var1, int[] var2) {
      this.field = var1;
      this.coefficients = normalForm(var2);
      this.computeDegree();
   }

   public PolynomialGF2mSmallM(GF2mField var1, byte[] var2) {
      this.field = var1;
      int var3 = 8;

      int var4;
      for(var4 = 1; var1.getDegree() > var3; var3 += 8) {
         ++var4;
      }

      if (var2.length % var4 != 0) {
         throw new IllegalArgumentException(" Error: byte array is not encoded polynomial over given finite field GF2m");
      } else {
         this.coefficients = new int[var2.length / var4];
         var4 = 0;

         for(int var5 = 0; var5 < this.coefficients.length; ++var5) {
            for(int var6 = 0; var6 < var3; var6 += 8) {
               int[] var10000 = this.coefficients;
               var10000[var5] ^= (var2[var4++] & 255) << var6;
            }

            if (!this.field.isElementOfThisField(this.coefficients[var5])) {
               throw new IllegalArgumentException(" Error: byte array is not encoded polynomial over given finite field GF2m");
            }
         }

         if (this.coefficients.length != 1 && this.coefficients[this.coefficients.length - 1] == 0) {
            throw new IllegalArgumentException(" Error: byte array is not encoded polynomial over given finite field GF2m");
         } else {
            this.computeDegree();
         }
      }
   }

   public PolynomialGF2mSmallM(PolynomialGF2mSmallM var1) {
      this.field = var1.field;
      this.degree = var1.degree;
      this.coefficients = IntUtils.clone(var1.coefficients);
   }

   public int getDegree() {
      int var1 = this.coefficients.length - 1;
      return this.coefficients[var1] == 0 ? -1 : var1;
   }

   private static int headCoefficient(int[] var0) {
      int var1 = computeDegree(var0);
      return var1 == -1 ? 0 : var0[var1];
   }

   public int getCoefficient(int var1) {
      return var1 >= 0 && var1 <= this.degree ? this.coefficients[var1] : 0;
   }

   public byte[] getEncoded() {
      int var1 = 8;

      int var2;
      for(var2 = 1; this.field.getDegree() > var1; var1 += 8) {
         ++var2;
      }

      byte[] var3 = new byte[this.coefficients.length * var2];
      var2 = 0;

      for(int var4 = 0; var4 < this.coefficients.length; ++var4) {
         for(int var5 = 0; var5 < var1; var5 += 8) {
            var3[var2++] = (byte)(this.coefficients[var4] >>> var5);
         }
      }

      return var3;
   }

   public int evaluateAt(int var1) {
      int var2 = this.coefficients[this.degree];

      for(int var3 = this.degree - 1; var3 >= 0; --var3) {
         var2 = this.field.mult(var2, var1) ^ this.coefficients[var3];
      }

      return var2;
   }

   public void addToThis(PolynomialGF2mSmallM var1) {
      this.coefficients = this.add(this.coefficients, var1.coefficients);
      this.computeDegree();
   }

   private int[] add(int[] var1, int[] var2) {
      int[] var3;
      int[] var4;
      if (var1.length < var2.length) {
         var3 = new int[var2.length];
         System.arraycopy(var2, 0, var3, 0, var2.length);
         var4 = var1;
      } else {
         var3 = new int[var1.length];
         System.arraycopy(var1, 0, var3, 0, var1.length);
         var4 = var2;
      }

      for(int var5 = var4.length - 1; var5 >= 0; --var5) {
         var3[var5] = this.field.add(var3[var5], var4[var5]);
      }

      return var3;
   }

   public PolynomialGF2mSmallM multWithElement(int var1) {
      if (!this.field.isElementOfThisField(var1)) {
         throw new ArithmeticException("Not an element of the finite field this polynomial is defined over.");
      } else {
         int[] var2 = this.multWithElement(this.coefficients, var1);
         return new PolynomialGF2mSmallM(this.field, var2);
      }
   }

   public void multThisWithElement(int var1) {
      if (!this.field.isElementOfThisField(var1)) {
         throw new ArithmeticException("Not an element of the finite field this polynomial is defined over.");
      } else {
         this.coefficients = this.multWithElement(this.coefficients, var1);
         this.computeDegree();
      }
   }

   private int[] multWithElement(int[] var1, int var2) {
      int var3 = computeDegree(var1);
      if (var3 != -1 && var2 != 0) {
         if (var2 == 1) {
            return IntUtils.clone(var1);
         } else {
            int[] var4 = new int[var3 + 1];

            for(int var5 = var3; var5 >= 0; --var5) {
               var4[var5] = this.field.mult(var1[var5], var2);
            }

            return var4;
         }
      } else {
         return new int[1];
      }
   }

   private static int[] multWithMonomial(int[] var0, int var1) {
      int var2 = computeDegree(var0);
      if (var2 == -1) {
         return new int[1];
      } else {
         int[] var3 = new int[var2 + var1 + 1];
         System.arraycopy(var0, 0, var3, var1, var2 + 1);
         return var3;
      }
   }

   public PolynomialGF2mSmallM mod(PolynomialGF2mSmallM var1) {
      int[] var2 = this.mod(this.coefficients, var1.coefficients);
      return new PolynomialGF2mSmallM(this.field, var2);
   }

   private int[] mod(int[] var1, int[] var2) {
      int var3 = computeDegree(var2);
      if (var3 == -1) {
         throw new ArithmeticException("Division by zero");
      } else {
         int[] var4 = new int[var1.length];
         int var5 = headCoefficient(var2);
         var5 = this.field.inverse(var5);
         System.arraycopy(var1, 0, var4, 0, var4.length);

         while(var3 <= computeDegree(var4)) {
            int var7 = this.field.mult(headCoefficient(var4), var5);
            int[] var6 = multWithMonomial(var2, computeDegree(var4) - var3);
            var6 = this.multWithElement(var6, var7);
            var4 = this.add(var6, var4);
         }

         return var4;
      }
   }

   public boolean equals(Object var1) {
      if (var1 != null && var1 instanceof PolynomialGF2mSmallM) {
         PolynomialGF2mSmallM var2 = (PolynomialGF2mSmallM)var1;
         return this.field.equals(var2.field) && this.degree == var2.degree && isEqual(this.coefficients, var2.coefficients);
      } else {
         return false;
      }
   }

   private static boolean isEqual(int[] var0, int[] var1) {
      int var2 = computeDegree(var0);
      int var3 = computeDegree(var1);
      if (var2 != var3) {
         return false;
      } else {
         for(int var4 = 0; var4 <= var2; ++var4) {
            if (var0[var4] != var1[var4]) {
               return false;
            }
         }

         return true;
      }
   }

   public int hashCode() {
      int var1 = this.field.hashCode();

      for(int var2 = 0; var2 < this.coefficients.length; ++var2) {
         var1 = var1 * 31 + this.coefficients[var2];
      }

      return var1;
   }

   public String toString() {
      String var1 = " Polynomial over " + this.field.toString() + ": \n";

      for(int var2 = 0; var2 < this.coefficients.length; ++var2) {
         var1 = var1 + this.field.elementToStr(this.coefficients[var2]) + "Y^" + var2 + "+";
      }

      var1 = var1 + ";";
      return var1;
   }

   private void computeDegree() {
      for(this.degree = this.coefficients.length - 1; this.degree >= 0 && this.coefficients[this.degree] == 0; --this.degree) {
      }

   }

   private static int computeDegree(int[] var0) {
      int var1;
      for(var1 = var0.length - 1; var1 >= 0 && var0[var1] == 0; --var1) {
      }

      return var1;
   }

   private static int[] normalForm(int[] var0) {
      int var1 = computeDegree(var0);
      if (var1 == -1) {
         return new int[1];
      } else if (var0.length == var1 + 1) {
         return IntUtils.clone(var0);
      } else {
         int[] var2 = new int[var1 + 1];
         System.arraycopy(var0, 0, var2, 0, var1 + 1);
         return var2;
      }
   }
}
