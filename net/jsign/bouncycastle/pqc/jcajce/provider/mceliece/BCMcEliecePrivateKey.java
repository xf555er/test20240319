package net.jsign.bouncycastle.pqc.jcajce.provider.mceliece;

import java.io.IOException;
import java.security.PrivateKey;
import net.jsign.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.pqc.asn1.McEliecePrivateKey;
import net.jsign.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import net.jsign.bouncycastle.pqc.crypto.mceliece.McEliecePrivateKeyParameters;
import net.jsign.bouncycastle.pqc.math.linearalgebra.GF2Matrix;
import net.jsign.bouncycastle.pqc.math.linearalgebra.GF2mField;
import net.jsign.bouncycastle.pqc.math.linearalgebra.Permutation;
import net.jsign.bouncycastle.pqc.math.linearalgebra.PolynomialGF2mSmallM;

public class BCMcEliecePrivateKey implements PrivateKey {
   private McEliecePrivateKeyParameters params;

   public BCMcEliecePrivateKey(McEliecePrivateKeyParameters var1) {
      this.params = var1;
   }

   public String getAlgorithm() {
      return "McEliece";
   }

   public int getN() {
      return this.params.getN();
   }

   public int getK() {
      return this.params.getK();
   }

   public GF2mField getField() {
      return this.params.getField();
   }

   public PolynomialGF2mSmallM getGoppaPoly() {
      return this.params.getGoppaPoly();
   }

   public GF2Matrix getSInv() {
      return this.params.getSInv();
   }

   public Permutation getP1() {
      return this.params.getP1();
   }

   public Permutation getP2() {
      return this.params.getP2();
   }

   public boolean equals(Object var1) {
      if (!(var1 instanceof BCMcEliecePrivateKey)) {
         return false;
      } else {
         BCMcEliecePrivateKey var2 = (BCMcEliecePrivateKey)var1;
         return this.getN() == var2.getN() && this.getK() == var2.getK() && this.getField().equals(var2.getField()) && this.getGoppaPoly().equals(var2.getGoppaPoly()) && this.getSInv().equals(var2.getSInv()) && this.getP1().equals(var2.getP1()) && this.getP2().equals(var2.getP2());
      }
   }

   public int hashCode() {
      int var1 = this.params.getK();
      var1 = var1 * 37 + this.params.getN();
      var1 = var1 * 37 + this.params.getField().hashCode();
      var1 = var1 * 37 + this.params.getGoppaPoly().hashCode();
      var1 = var1 * 37 + this.params.getP1().hashCode();
      var1 = var1 * 37 + this.params.getP2().hashCode();
      return var1 * 37 + this.params.getSInv().hashCode();
   }

   public byte[] getEncoded() {
      McEliecePrivateKey var1 = new McEliecePrivateKey(this.params.getN(), this.params.getK(), this.params.getField(), this.params.getGoppaPoly(), this.params.getP1(), this.params.getP2(), this.params.getSInv());

      PrivateKeyInfo var2;
      try {
         AlgorithmIdentifier var3 = new AlgorithmIdentifier(PQCObjectIdentifiers.mcEliece);
         var2 = new PrivateKeyInfo(var3, var1);
      } catch (IOException var5) {
         return null;
      }

      try {
         byte[] var6 = var2.getEncoded();
         return var6;
      } catch (IOException var4) {
         return null;
      }
   }

   public String getFormat() {
      return "PKCS#8";
   }
}
