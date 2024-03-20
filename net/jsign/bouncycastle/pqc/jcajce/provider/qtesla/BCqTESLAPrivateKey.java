package net.jsign.bouncycastle.pqc.jcajce.provider.qtesla;

import java.io.IOException;
import java.security.PrivateKey;
import net.jsign.bouncycastle.asn1.ASN1Set;
import net.jsign.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import net.jsign.bouncycastle.pqc.crypto.qtesla.QTESLAPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.qtesla.QTESLASecurityCategory;
import net.jsign.bouncycastle.pqc.crypto.util.PrivateKeyFactory;
import net.jsign.bouncycastle.pqc.crypto.util.PrivateKeyInfoFactory;
import net.jsign.bouncycastle.util.Arrays;

public class BCqTESLAPrivateKey implements PrivateKey {
   private transient QTESLAPrivateKeyParameters keyParams;
   private transient ASN1Set attributes;

   public BCqTESLAPrivateKey(PrivateKeyInfo var1) throws IOException {
      this.init(var1);
   }

   private void init(PrivateKeyInfo var1) throws IOException {
      this.attributes = var1.getAttributes();
      this.keyParams = (QTESLAPrivateKeyParameters)PrivateKeyFactory.createKey(var1);
   }

   public final String getAlgorithm() {
      return QTESLASecurityCategory.getName(this.keyParams.getSecurityCategory());
   }

   public String getFormat() {
      return "PKCS#8";
   }

   public byte[] getEncoded() {
      try {
         PrivateKeyInfo var1 = PrivateKeyInfoFactory.createPrivateKeyInfo(this.keyParams, this.attributes);
         return var1.getEncoded();
      } catch (IOException var3) {
         return null;
      }
   }

   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (!(var1 instanceof BCqTESLAPrivateKey)) {
         return false;
      } else {
         BCqTESLAPrivateKey var2 = (BCqTESLAPrivateKey)var1;
         return this.keyParams.getSecurityCategory() == var2.keyParams.getSecurityCategory() && Arrays.areEqual(this.keyParams.getSecret(), var2.keyParams.getSecret());
      }
   }

   public int hashCode() {
      return this.keyParams.getSecurityCategory() + 37 * Arrays.hashCode(this.keyParams.getSecret());
   }
}
