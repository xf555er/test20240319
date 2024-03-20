package net.jsign.bouncycastle.pqc.jcajce.provider.mceliece;

import java.io.IOException;
import java.security.PrivateKey;
import net.jsign.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.pqc.asn1.McElieceCCA2PrivateKey;
import net.jsign.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import net.jsign.bouncycastle.pqc.crypto.mceliece.McElieceCCA2PrivateKeyParameters;
import net.jsign.bouncycastle.pqc.math.linearalgebra.GF2Matrix;
import net.jsign.bouncycastle.pqc.math.linearalgebra.GF2mField;
import net.jsign.bouncycastle.pqc.math.linearalgebra.Permutation;
import net.jsign.bouncycastle.pqc.math.linearalgebra.PolynomialGF2mSmallM;

public class BCMcElieceCCA2PrivateKey implements PrivateKey {
   private McElieceCCA2PrivateKeyParameters params;

   public BCMcElieceCCA2PrivateKey(McElieceCCA2PrivateKeyParameters var1) {
      this.params = var1;
   }

   public String getAlgorithm() {
      return "McEliece-CCA2";
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

   public Permutation getP() {
      return this.params.getP();
   }

   public GF2Matrix getH() {
      return this.params.getH();
   }

   public boolean equals(Object var1) {
      if (var1 != null && var1 instanceof BCMcElieceCCA2PrivateKey) {
         BCMcElieceCCA2PrivateKey var2 = (BCMcElieceCCA2PrivateKey)var1;
         return this.getN() == var2.getN() && this.getK() == var2.getK() && this.getField().equals(var2.getField()) && this.getGoppaPoly().equals(var2.getGoppaPoly()) && this.getP().equals(var2.getP()) && this.getH().equals(var2.getH());
      } else {
         return false;
      }
   }

   public int hashCode() {
      int var1 = this.params.getK();
      var1 = var1 * 37 + this.params.getN();
      var1 = var1 * 37 + this.params.getField().hashCode();
      var1 = var1 * 37 + this.params.getGoppaPoly().hashCode();
      var1 = var1 * 37 + this.params.getP().hashCode();
      return var1 * 37 + this.params.getH().hashCode();
   }

   public byte[] getEncoded() {
      try {
         McElieceCCA2PrivateKey var2 = new McElieceCCA2PrivateKey(this.getN(), this.getK(), this.getField(), this.getGoppaPoly(), this.getP(), Utils.getDigAlgId(this.params.getDigest()));
         AlgorithmIdentifier var3 = new AlgorithmIdentifier(PQCObjectIdentifiers.mcElieceCca2);
         PrivateKeyInfo var1 = new PrivateKeyInfo(var3, var2);
         return var1.getEncoded();
      } catch (IOException var4) {
         return null;
      }
   }

   public String getFormat() {
      return "PKCS#8";
   }
}
