package net.jsign.bouncycastle.pqc.jcajce.provider.rainbow;

import java.security.PublicKey;
import net.jsign.bouncycastle.asn1.DERNull;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import net.jsign.bouncycastle.pqc.asn1.RainbowPublicKey;
import net.jsign.bouncycastle.pqc.crypto.rainbow.util.RainbowUtil;
import net.jsign.bouncycastle.pqc.jcajce.provider.util.KeyUtil;
import net.jsign.bouncycastle.pqc.jcajce.spec.RainbowPublicKeySpec;
import net.jsign.bouncycastle.util.Arrays;

public class BCRainbowPublicKey implements PublicKey {
   private short[][] coeffquadratic;
   private short[][] coeffsingular;
   private short[] coeffscalar;
   private int docLength;

   public BCRainbowPublicKey(int var1, short[][] var2, short[][] var3, short[] var4) {
      this.docLength = var1;
      this.coeffquadratic = var2;
      this.coeffsingular = var3;
      this.coeffscalar = var4;
   }

   public BCRainbowPublicKey(RainbowPublicKeySpec var1) {
      this(var1.getDocLength(), var1.getCoeffQuadratic(), var1.getCoeffSingular(), var1.getCoeffScalar());
   }

   public int getDocLength() {
      return this.docLength;
   }

   public short[][] getCoeffQuadratic() {
      return this.coeffquadratic;
   }

   public short[][] getCoeffSingular() {
      short[][] var1 = new short[this.coeffsingular.length][];

      for(int var2 = 0; var2 != this.coeffsingular.length; ++var2) {
         var1[var2] = Arrays.clone(this.coeffsingular[var2]);
      }

      return var1;
   }

   public short[] getCoeffScalar() {
      return Arrays.clone(this.coeffscalar);
   }

   public boolean equals(Object var1) {
      if (var1 != null && var1 instanceof BCRainbowPublicKey) {
         BCRainbowPublicKey var2 = (BCRainbowPublicKey)var1;
         return this.docLength == var2.getDocLength() && RainbowUtil.equals(this.coeffquadratic, var2.getCoeffQuadratic()) && RainbowUtil.equals(this.coeffsingular, var2.getCoeffSingular()) && RainbowUtil.equals(this.coeffscalar, var2.getCoeffScalar());
      } else {
         return false;
      }
   }

   public int hashCode() {
      int var1 = this.docLength;
      var1 = var1 * 37 + Arrays.hashCode(this.coeffquadratic);
      var1 = var1 * 37 + Arrays.hashCode(this.coeffsingular);
      var1 = var1 * 37 + Arrays.hashCode(this.coeffscalar);
      return var1;
   }

   public final String getAlgorithm() {
      return "Rainbow";
   }

   public String getFormat() {
      return "X.509";
   }

   public byte[] getEncoded() {
      RainbowPublicKey var1 = new RainbowPublicKey(this.docLength, this.coeffquadratic, this.coeffsingular, this.coeffscalar);
      AlgorithmIdentifier var2 = new AlgorithmIdentifier(PQCObjectIdentifiers.rainbow, DERNull.INSTANCE);
      return KeyUtil.getEncodedSubjectPublicKeyInfo(var2, var1);
   }
}
