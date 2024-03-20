package net.jsign.bouncycastle.math.ec;

import java.math.BigInteger;
import java.security.SecureRandom;
import net.jsign.bouncycastle.math.ec.endo.ECEndomorphism;
import net.jsign.bouncycastle.math.field.FiniteField;
import net.jsign.bouncycastle.math.field.FiniteFields;
import net.jsign.bouncycastle.util.BigIntegers;
import net.jsign.bouncycastle.util.Integers;

public abstract class ECCurve {
   protected FiniteField field;
   protected ECFieldElement a;
   protected ECFieldElement b;
   protected BigInteger order;
   protected BigInteger cofactor;
   protected int coord = 0;
   protected ECEndomorphism endomorphism = null;
   protected ECMultiplier multiplier = null;

   protected ECCurve(FiniteField var1) {
      this.field = var1;
   }

   public abstract int getFieldSize();

   public abstract ECFieldElement fromBigInteger(BigInteger var1);

   public abstract ECFieldElement randomFieldElementMult(SecureRandom var1);

   public ECPoint createPoint(BigInteger var1, BigInteger var2) {
      return this.createRawPoint(this.fromBigInteger(var1), this.fromBigInteger(var2));
   }

   protected abstract ECPoint createRawPoint(ECFieldElement var1, ECFieldElement var2);

   public ECPoint importPoint(ECPoint var1) {
      if (this == var1.getCurve()) {
         return var1;
      } else if (var1.isInfinity()) {
         return this.getInfinity();
      } else {
         var1 = var1.normalize();
         return this.createPoint(var1.getXCoord().toBigInteger(), var1.getYCoord().toBigInteger());
      }
   }

   public void normalizeAll(ECPoint[] var1) {
      this.normalizeAll(var1, 0, var1.length, (ECFieldElement)null);
   }

   public void normalizeAll(ECPoint[] var1, int var2, int var3, ECFieldElement var4) {
      this.checkPoints(var1, var2, var3);
      switch (this.getCoordinateSystem()) {
         case 0:
         case 5:
            if (var4 != null) {
               throw new IllegalArgumentException("'iso' not valid for affine coordinates");
            }

            return;
         default:
            ECFieldElement[] var5 = new ECFieldElement[var3];
            int[] var6 = new int[var3];
            int var7 = 0;
            int var8 = 0;

            for(; var8 < var3; ++var8) {
               ECPoint var9 = var1[var2 + var8];
               if (null != var9 && (var4 != null || !var9.isNormalized())) {
                  var5[var7] = var9.getZCoord(0);
                  var6[var7++] = var2 + var8;
               }
            }

            if (var7 != 0) {
               ECAlgorithms.montgomeryTrick(var5, 0, var7, var4);

               for(var8 = 0; var8 < var7; ++var8) {
                  int var10 = var6[var8];
                  var1[var10] = var1[var10].normalize(var5[var8]);
               }

            }
      }
   }

   public abstract ECPoint getInfinity();

   public FiniteField getField() {
      return this.field;
   }

   public ECFieldElement getA() {
      return this.a;
   }

   public ECFieldElement getB() {
      return this.b;
   }

   public int getCoordinateSystem() {
      return this.coord;
   }

   protected void checkPoints(ECPoint[] var1, int var2, int var3) {
      if (var1 == null) {
         throw new IllegalArgumentException("'points' cannot be null");
      } else if (var2 >= 0 && var3 >= 0 && var2 <= var1.length - var3) {
         for(int var4 = 0; var4 < var3; ++var4) {
            ECPoint var5 = var1[var2 + var4];
            if (null != var5 && this != var5.getCurve()) {
               throw new IllegalArgumentException("'points' entries must be null or on this curve");
            }
         }

      } else {
         throw new IllegalArgumentException("invalid range specified for 'points'");
      }
   }

   public boolean equals(ECCurve var1) {
      return this == var1 || null != var1 && this.getField().equals(var1.getField()) && this.getA().toBigInteger().equals(var1.getA().toBigInteger()) && this.getB().toBigInteger().equals(var1.getB().toBigInteger());
   }

   public boolean equals(Object var1) {
      return this == var1 || var1 instanceof ECCurve && this.equals((ECCurve)var1);
   }

   public int hashCode() {
      return this.getField().hashCode() ^ Integers.rotateLeft(this.getA().toBigInteger().hashCode(), 8) ^ Integers.rotateLeft(this.getB().toBigInteger().hashCode(), 16);
   }

   public abstract static class AbstractF2m extends ECCurve {
      private BigInteger[] si = null;

      private static FiniteField buildField(int var0, int var1, int var2, int var3) {
         if (var1 == 0) {
            throw new IllegalArgumentException("k1 must be > 0");
         } else if (var2 == 0) {
            if (var3 != 0) {
               throw new IllegalArgumentException("k3 must be 0 if k2 == 0");
            } else {
               return FiniteFields.getBinaryExtensionField(new int[]{0, var1, var0});
            }
         } else if (var2 <= var1) {
            throw new IllegalArgumentException("k2 must be > k1");
         } else if (var3 <= var2) {
            throw new IllegalArgumentException("k3 must be > k2");
         } else {
            return FiniteFields.getBinaryExtensionField(new int[]{0, var1, var2, var3, var0});
         }
      }

      protected AbstractF2m(int var1, int var2, int var3, int var4) {
         super(buildField(var1, var2, var3, var4));
      }

      public ECPoint createPoint(BigInteger var1, BigInteger var2) {
         ECFieldElement var3 = this.fromBigInteger(var1);
         ECFieldElement var4 = this.fromBigInteger(var2);
         int var5 = this.getCoordinateSystem();
         switch (var5) {
            case 5:
            case 6:
               if (var3.isZero()) {
                  if (!var4.square().equals(this.getB())) {
                     throw new IllegalArgumentException();
                  }
               } else {
                  var4 = var4.divide(var3).add(var3);
               }
            default:
               return this.createRawPoint(var3, var4);
         }
      }

      public ECFieldElement randomFieldElementMult(SecureRandom var1) {
         int var2 = this.getFieldSize();
         ECFieldElement var3 = this.fromBigInteger(implRandomFieldElementMult(var1, var2));
         ECFieldElement var4 = this.fromBigInteger(implRandomFieldElementMult(var1, var2));
         return var3.multiply(var4);
      }

      private static BigInteger implRandomFieldElementMult(SecureRandom var0, int var1) {
         BigInteger var2;
         do {
            var2 = BigIntegers.createRandomBigInteger(var1, var0);
         } while(var2.signum() <= 0);

         return var2;
      }
   }

   public abstract static class AbstractFp extends ECCurve {
      protected AbstractFp(BigInteger var1) {
         super(FiniteFields.getPrimeField(var1));
      }

      public ECFieldElement randomFieldElementMult(SecureRandom var1) {
         BigInteger var2 = this.getField().getCharacteristic();
         ECFieldElement var3 = this.fromBigInteger(implRandomFieldElementMult(var1, var2));
         ECFieldElement var4 = this.fromBigInteger(implRandomFieldElementMult(var1, var2));
         return var3.multiply(var4);
      }

      private static BigInteger implRandomFieldElementMult(SecureRandom var0, BigInteger var1) {
         BigInteger var2;
         do {
            var2 = BigIntegers.createRandomBigInteger(var1.bitLength(), var0);
         } while(var2.signum() <= 0 || var2.compareTo(var1) >= 0);

         return var2;
      }
   }

   public static class F2m extends AbstractF2m {
      private int m;
      private int k1;
      private int k2;
      private int k3;
      private ECPoint.F2m infinity;

      public F2m(int var1, int var2, int var3, int var4, BigInteger var5, BigInteger var6, BigInteger var7, BigInteger var8) {
         super(var1, var2, var3, var4);
         this.m = var1;
         this.k1 = var2;
         this.k2 = var3;
         this.k3 = var4;
         this.order = var7;
         this.cofactor = var8;
         this.infinity = new ECPoint.F2m(this, (ECFieldElement)null, (ECFieldElement)null);
         this.a = this.fromBigInteger(var5);
         this.b = this.fromBigInteger(var6);
         this.coord = 6;
      }

      public int getFieldSize() {
         return this.m;
      }

      public ECFieldElement fromBigInteger(BigInteger var1) {
         return new ECFieldElement.F2m(this.m, this.k1, this.k2, this.k3, var1);
      }

      protected ECPoint createRawPoint(ECFieldElement var1, ECFieldElement var2) {
         return new ECPoint.F2m(this, var1, var2);
      }

      public ECPoint getInfinity() {
         return this.infinity;
      }
   }

   public static class Fp extends AbstractFp {
      BigInteger q;
      BigInteger r;
      ECPoint.Fp infinity;

      public Fp(BigInteger var1, BigInteger var2, BigInteger var3, BigInteger var4, BigInteger var5) {
         super(var1);
         this.q = var1;
         this.r = ECFieldElement.Fp.calculateResidue(var1);
         this.infinity = new ECPoint.Fp(this, (ECFieldElement)null, (ECFieldElement)null);
         this.a = this.fromBigInteger(var2);
         this.b = this.fromBigInteger(var3);
         this.order = var4;
         this.cofactor = var5;
         this.coord = 4;
      }

      public int getFieldSize() {
         return this.q.bitLength();
      }

      public ECFieldElement fromBigInteger(BigInteger var1) {
         return new ECFieldElement.Fp(this.q, this.r, var1);
      }

      protected ECPoint createRawPoint(ECFieldElement var1, ECFieldElement var2) {
         return new ECPoint.Fp(this, var1, var2);
      }

      public ECPoint importPoint(ECPoint var1) {
         if (this != var1.getCurve() && this.getCoordinateSystem() == 2 && !var1.isInfinity()) {
            switch (var1.getCurve().getCoordinateSystem()) {
               case 2:
               case 3:
               case 4:
                  return new ECPoint.Fp(this, this.fromBigInteger(var1.x.toBigInteger()), this.fromBigInteger(var1.y.toBigInteger()), new ECFieldElement[]{this.fromBigInteger(var1.zs[0].toBigInteger())});
            }
         }

         return super.importPoint(var1);
      }

      public ECPoint getInfinity() {
         return this.infinity;
      }
   }
}
