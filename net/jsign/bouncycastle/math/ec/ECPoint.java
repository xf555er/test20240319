package net.jsign.bouncycastle.math.ec;

import java.security.SecureRandom;
import java.util.Hashtable;
import net.jsign.bouncycastle.crypto.CryptoServicesRegistrar;

public abstract class ECPoint {
   protected static final ECFieldElement[] EMPTY_ZS = new ECFieldElement[0];
   protected ECCurve curve;
   protected ECFieldElement x;
   protected ECFieldElement y;
   protected ECFieldElement[] zs;
   protected Hashtable preCompTable;

   protected static ECFieldElement[] getInitialZCoords(ECCurve var0) {
      int var1 = null == var0 ? 0 : var0.getCoordinateSystem();
      switch (var1) {
         case 0:
         case 5:
            return EMPTY_ZS;
         default:
            ECFieldElement var2 = var0.fromBigInteger(ECConstants.ONE);
            switch (var1) {
               case 1:
               case 2:
               case 6:
                  return new ECFieldElement[]{var2};
               case 3:
                  return new ECFieldElement[]{var2, var2, var2};
               case 4:
                  return new ECFieldElement[]{var2, var0.getA()};
               case 5:
               default:
                  throw new IllegalArgumentException("unknown coordinate system");
            }
      }
   }

   protected ECPoint(ECCurve var1, ECFieldElement var2, ECFieldElement var3) {
      this(var1, var2, var3, getInitialZCoords(var1));
   }

   protected ECPoint(ECCurve var1, ECFieldElement var2, ECFieldElement var3, ECFieldElement[] var4) {
      this.preCompTable = null;
      this.curve = var1;
      this.x = var2;
      this.y = var3;
      this.zs = var4;
   }

   public ECCurve getCurve() {
      return this.curve;
   }

   protected int getCurveCoordinateSystem() {
      return null == this.curve ? 0 : this.curve.getCoordinateSystem();
   }

   public ECFieldElement getXCoord() {
      return this.x;
   }

   public ECFieldElement getYCoord() {
      return this.y;
   }

   public ECFieldElement getZCoord(int var1) {
      return var1 >= 0 && var1 < this.zs.length ? this.zs[var1] : null;
   }

   public final ECFieldElement getRawXCoord() {
      return this.x;
   }

   public final ECFieldElement getRawYCoord() {
      return this.y;
   }

   public boolean isNormalized() {
      int var1 = this.getCurveCoordinateSystem();
      return var1 == 0 || var1 == 5 || this.isInfinity() || this.zs[0].isOne();
   }

   public ECPoint normalize() {
      if (this.isInfinity()) {
         return this;
      } else {
         switch (this.getCurveCoordinateSystem()) {
            case 0:
            case 5:
               return this;
            default:
               ECFieldElement var1 = this.getZCoord(0);
               if (var1.isOne()) {
                  return this;
               } else if (null == this.curve) {
                  throw new IllegalStateException("Detached points must be in affine coordinates");
               } else {
                  SecureRandom var2 = CryptoServicesRegistrar.getSecureRandom();
                  ECFieldElement var3 = this.curve.randomFieldElementMult(var2);
                  ECFieldElement var4 = var1.multiply(var3).invert().multiply(var3);
                  return this.normalize(var4);
               }
         }
      }
   }

   ECPoint normalize(ECFieldElement var1) {
      switch (this.getCurveCoordinateSystem()) {
         case 1:
         case 6:
            return this.createScaledPoint(var1, var1);
         case 2:
         case 3:
         case 4:
            ECFieldElement var2 = var1.square();
            ECFieldElement var3 = var2.multiply(var1);
            return this.createScaledPoint(var2, var3);
         case 5:
         default:
            throw new IllegalStateException("not a projective coordinate system");
      }
   }

   protected ECPoint createScaledPoint(ECFieldElement var1, ECFieldElement var2) {
      return this.getCurve().createRawPoint(this.getRawXCoord().multiply(var1), this.getRawYCoord().multiply(var2));
   }

   public boolean isInfinity() {
      return this.x == null || this.y == null || this.zs.length > 0 && this.zs[0].isZero();
   }

   public boolean equals(ECPoint var1) {
      if (null == var1) {
         return false;
      } else {
         ECCurve var2 = this.getCurve();
         ECCurve var3 = var1.getCurve();
         boolean var4 = null == var2;
         boolean var5 = null == var3;
         boolean var6 = this.isInfinity();
         boolean var7 = var1.isInfinity();
         if (!var6 && !var7) {
            ECPoint var8 = this;
            ECPoint var9 = var1;
            if (!var4 || !var5) {
               if (var4) {
                  var9 = var1.normalize();
               } else if (var5) {
                  var8 = this.normalize();
               } else {
                  if (!var2.equals(var3)) {
                     return false;
                  }

                  ECPoint[] var10 = new ECPoint[]{this, var2.importPoint(var1)};
                  var2.normalizeAll(var10);
                  var8 = var10[0];
                  var9 = var10[1];
               }
            }

            return var8.getXCoord().equals(var9.getXCoord()) && var8.getYCoord().equals(var9.getYCoord());
         } else {
            return var6 && var7 && (var4 || var5 || var2.equals(var3));
         }
      }
   }

   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else {
         return !(var1 instanceof ECPoint) ? false : this.equals((ECPoint)var1);
      }
   }

   public int hashCode() {
      ECCurve var1 = this.getCurve();
      int var2 = null == var1 ? 0 : ~var1.hashCode();
      if (!this.isInfinity()) {
         ECPoint var3 = this.normalize();
         var2 ^= var3.getXCoord().hashCode() * 17;
         var2 ^= var3.getYCoord().hashCode() * 257;
      }

      return var2;
   }

   public String toString() {
      if (this.isInfinity()) {
         return "INF";
      } else {
         StringBuffer var1 = new StringBuffer();
         var1.append('(');
         var1.append(this.getRawXCoord());
         var1.append(',');
         var1.append(this.getRawYCoord());

         for(int var2 = 0; var2 < this.zs.length; ++var2) {
            var1.append(',');
            var1.append(this.zs[var2]);
         }

         var1.append(')');
         return var1.toString();
      }
   }

   public abstract static class AbstractF2m extends ECPoint {
      protected AbstractF2m(ECCurve var1, ECFieldElement var2, ECFieldElement var3) {
         super(var1, var2, var3);
      }
   }

   public abstract static class AbstractFp extends ECPoint {
      protected AbstractFp(ECCurve var1, ECFieldElement var2, ECFieldElement var3) {
         super(var1, var2, var3);
      }

      protected AbstractFp(ECCurve var1, ECFieldElement var2, ECFieldElement var3, ECFieldElement[] var4) {
         super(var1, var2, var3, var4);
      }
   }

   public static class F2m extends AbstractF2m {
      F2m(ECCurve var1, ECFieldElement var2, ECFieldElement var3) {
         super(var1, var2, var3);
      }

      public ECFieldElement getYCoord() {
         int var1 = this.getCurveCoordinateSystem();
         switch (var1) {
            case 5:
            case 6:
               ECFieldElement var2 = this.x;
               ECFieldElement var3 = this.y;
               if (!this.isInfinity() && !var2.isZero()) {
                  ECFieldElement var4 = var3.add(var2).multiply(var2);
                  if (6 == var1) {
                     ECFieldElement var5 = this.zs[0];
                     if (!var5.isOne()) {
                        var4 = var4.divide(var5);
                     }
                  }

                  return var4;
               }

               return var3;
            default:
               return this.y;
         }
      }
   }

   public static class Fp extends AbstractFp {
      Fp(ECCurve var1, ECFieldElement var2, ECFieldElement var3) {
         super(var1, var2, var3);
      }

      Fp(ECCurve var1, ECFieldElement var2, ECFieldElement var3, ECFieldElement[] var4) {
         super(var1, var2, var3, var4);
      }

      public ECFieldElement getZCoord(int var1) {
         return var1 == 1 && 4 == this.getCurveCoordinateSystem() ? this.getJacobianModifiedW() : super.getZCoord(var1);
      }

      protected ECFieldElement calculateJacobianModifiedW(ECFieldElement var1, ECFieldElement var2) {
         ECFieldElement var3 = this.getCurve().getA();
         if (!var3.isZero() && !var1.isOne()) {
            if (var2 == null) {
               var2 = var1.square();
            }

            ECFieldElement var4 = var2.square();
            ECFieldElement var5 = var3.negate();
            if (var5.bitLength() < var3.bitLength()) {
               var4 = var4.multiply(var5).negate();
            } else {
               var4 = var4.multiply(var3);
            }

            return var4;
         } else {
            return var3;
         }
      }

      protected ECFieldElement getJacobianModifiedW() {
         ECFieldElement var1 = this.zs[1];
         if (var1 == null) {
            this.zs[1] = var1 = this.calculateJacobianModifiedW(this.zs[0], (ECFieldElement)null);
         }

         return var1;
      }
   }
}
