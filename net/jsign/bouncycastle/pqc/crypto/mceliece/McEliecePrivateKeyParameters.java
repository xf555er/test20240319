package net.jsign.bouncycastle.pqc.crypto.mceliece;

import net.jsign.bouncycastle.pqc.math.linearalgebra.GF2Matrix;
import net.jsign.bouncycastle.pqc.math.linearalgebra.GF2mField;
import net.jsign.bouncycastle.pqc.math.linearalgebra.GoppaCode;
import net.jsign.bouncycastle.pqc.math.linearalgebra.Permutation;
import net.jsign.bouncycastle.pqc.math.linearalgebra.PolynomialGF2mSmallM;
import net.jsign.bouncycastle.pqc.math.linearalgebra.PolynomialRingGF2m;

public class McEliecePrivateKeyParameters extends McElieceKeyParameters {
   private int n;
   private int k;
   private GF2mField field;
   private PolynomialGF2mSmallM goppaPoly;
   private GF2Matrix sInv;
   private Permutation p1;
   private Permutation p2;
   private GF2Matrix h;
   private PolynomialGF2mSmallM[] qInv;

   public McEliecePrivateKeyParameters(int var1, int var2, GF2mField var3, PolynomialGF2mSmallM var4, Permutation var5, Permutation var6, GF2Matrix var7) {
      super(true, (McElieceParameters)null);
      this.k = var2;
      this.n = var1;
      this.field = var3;
      this.goppaPoly = var4;
      this.sInv = var7;
      this.p1 = var5;
      this.p2 = var6;
      this.h = GoppaCode.createCanonicalCheckMatrix(var3, var4);
      PolynomialRingGF2m var8 = new PolynomialRingGF2m(var3, var4);
      this.qInv = var8.getSquareRootMatrix();
   }

   public int getN() {
      return this.n;
   }

   public int getK() {
      return this.k;
   }

   public GF2mField getField() {
      return this.field;
   }

   public PolynomialGF2mSmallM getGoppaPoly() {
      return this.goppaPoly;
   }

   public GF2Matrix getSInv() {
      return this.sInv;
   }

   public Permutation getP1() {
      return this.p1;
   }

   public Permutation getP2() {
      return this.p2;
   }
}
