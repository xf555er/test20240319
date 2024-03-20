package net.jsign.bouncycastle.math.ec;

import java.math.BigInteger;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.BigIntegers;

public abstract class ECFieldElement implements ECConstants {
   public abstract BigInteger toBigInteger();

   public abstract int getFieldSize();

   public abstract ECFieldElement add(ECFieldElement var1);

   public abstract ECFieldElement multiply(ECFieldElement var1);

   public abstract ECFieldElement divide(ECFieldElement var1);

   public abstract ECFieldElement negate();

   public abstract ECFieldElement square();

   public abstract ECFieldElement invert();

   public int bitLength() {
      return this.toBigInteger().bitLength();
   }

   public boolean isOne() {
      return this.bitLength() == 1;
   }

   public boolean isZero() {
      return 0 == this.toBigInteger().signum();
   }

   public String toString() {
      return this.toBigInteger().toString(16);
   }

   public abstract static class AbstractF2m extends ECFieldElement {
   }

   public abstract static class AbstractFp extends ECFieldElement {
   }

   public static class F2m extends AbstractF2m {
      private int representation;
      private int m;
      private int[] ks;
      LongArray x;

      /** @deprecated */
      public F2m(int var1, int var2, int var3, int var4, BigInteger var5) {
         if (var5 != null && var5.signum() >= 0 && var5.bitLength() <= var1) {
            if (var3 == 0 && var4 == 0) {
               this.representation = 2;
               this.ks = new int[]{var2};
            } else {
               if (var3 >= var4) {
                  throw new IllegalArgumentException("k2 must be smaller than k3");
               }

               if (var3 <= 0) {
                  throw new IllegalArgumentException("k2 must be larger than 0");
               }

               this.representation = 3;
               this.ks = new int[]{var2, var3, var4};
            }

            this.m = var1;
            this.x = new LongArray(var5);
         } else {
            throw new IllegalArgumentException("x value invalid in F2m field element");
         }
      }

      F2m(int var1, int[] var2, LongArray var3) {
         this.m = var1;
         this.representation = var2.length == 1 ? 2 : 3;
         this.ks = var2;
         this.x = var3;
      }

      public int bitLength() {
         return this.x.degree();
      }

      public boolean isOne() {
         return this.x.isOne();
      }

      public boolean isZero() {
         return this.x.isZero();
      }

      public BigInteger toBigInteger() {
         return this.x.toBigInteger();
      }

      public int getFieldSize() {
         return this.m;
      }

      public ECFieldElement add(ECFieldElement var1) {
         LongArray var2 = (LongArray)this.x.clone();
         F2m var3 = (F2m)var1;
         var2.addShiftedByWords(var3.x, 0);
         return new F2m(this.m, this.ks, var2);
      }

      public ECFieldElement multiply(ECFieldElement var1) {
         return new F2m(this.m, this.ks, this.x.modMultiply(((F2m)var1).x, this.m, this.ks));
      }

      public ECFieldElement divide(ECFieldElement var1) {
         ECFieldElement var2 = var1.invert();
         return this.multiply(var2);
      }

      public ECFieldElement negate() {
         return this;
      }

      public ECFieldElement square() {
         return new F2m(this.m, this.ks, this.x.modSquare(this.m, this.ks));
      }

      public ECFieldElement invert() {
         return new F2m(this.m, this.ks, this.x.modInverse(this.m, this.ks));
      }

      public boolean equals(Object var1) {
         if (var1 == this) {
            return true;
         } else if (!(var1 instanceof F2m)) {
            return false;
         } else {
            F2m var2 = (F2m)var1;
            return this.m == var2.m && this.representation == var2.representation && Arrays.areEqual(this.ks, var2.ks) && this.x.equals(var2.x);
         }
      }

      public int hashCode() {
         return this.x.hashCode() ^ this.m ^ Arrays.hashCode(this.ks);
      }
   }

   public static class Fp extends AbstractFp {
      BigInteger q;
      BigInteger r;
      BigInteger x;

      static BigInteger calculateResidue(BigInteger var0) {
         int var1 = var0.bitLength();
         if (var1 >= 96) {
            BigInteger var2 = var0.shiftRight(var1 - 64);
            if (var2.longValue() == -1L) {
               return ONE.shiftLeft(var1).subtract(var0);
            }
         }

         return null;
      }

      Fp(BigInteger var1, BigInteger var2, BigInteger var3) {
         if (var3 != null && var3.signum() >= 0 && var3.compareTo(var1) < 0) {
            this.q = var1;
            this.r = var2;
            this.x = var3;
         } else {
            throw new IllegalArgumentException("x value invalid in Fp field element");
         }
      }

      public BigInteger toBigInteger() {
         return this.x;
      }

      public int getFieldSize() {
         return this.q.bitLength();
      }

      public ECFieldElement add(ECFieldElement var1) {
         return new Fp(this.q, this.r, this.modAdd(this.x, var1.toBigInteger()));
      }

      public ECFieldElement multiply(ECFieldElement var1) {
         return new Fp(this.q, this.r, this.modMult(this.x, var1.toBigInteger()));
      }

      public ECFieldElement divide(ECFieldElement var1) {
         return new Fp(this.q, this.r, this.modMult(this.x, this.modInverse(var1.toBigInteger())));
      }

      public ECFieldElement negate() {
         return this.x.signum() == 0 ? this : new Fp(this.q, this.r, this.q.subtract(this.x));
      }

      public ECFieldElement square() {
         return new Fp(this.q, this.r, this.modMult(this.x, this.x));
      }

      public ECFieldElement invert() {
         return new Fp(this.q, this.r, this.modInverse(this.x));
      }

      protected BigInteger modAdd(BigInteger var1, BigInteger var2) {
         BigInteger var3 = var1.add(var2);
         if (var3.compareTo(this.q) >= 0) {
            var3 = var3.subtract(this.q);
         }

         return var3;
      }

      protected BigInteger modInverse(BigInteger var1) {
         return BigIntegers.modOddInverse(this.q, var1);
      }

      protected BigInteger modMult(BigInteger var1, BigInteger var2) {
         return this.modReduce(var1.multiply(var2));
      }

      protected BigInteger modReduce(BigInteger var1) {
         if (this.r != null) {
            boolean var2 = var1.signum() < 0;
            if (var2) {
               var1 = var1.abs();
            }

            int var3 = this.q.bitLength();

            BigInteger var5;
            BigInteger var6;
            for(boolean var4 = this.r.equals(ECConstants.ONE); var1.bitLength() > var3 + 1; var1 = var5.add(var6)) {
               var5 = var1.shiftRight(var3);
               var6 = var1.subtract(var5.shiftLeft(var3));
               if (!var4) {
                  var5 = var5.multiply(this.r);
               }
            }

            while(var1.compareTo(this.q) >= 0) {
               var1 = var1.subtract(this.q);
            }

            if (var2 && var1.signum() != 0) {
               var1 = this.q.subtract(var1);
            }
         } else {
            var1 = var1.mod(this.q);
         }

         return var1;
      }

      public boolean equals(Object var1) {
         if (var1 == this) {
            return true;
         } else if (!(var1 instanceof Fp)) {
            return false;
         } else {
            Fp var2 = (Fp)var1;
            return this.q.equals(var2.q) && this.x.equals(var2.x);
         }
      }

      public int hashCode() {
         return this.q.hashCode() ^ this.x.hashCode();
      }
   }
}
