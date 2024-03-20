package net.jsign.bouncycastle.pqc.jcajce.provider.qtesla;

import java.io.IOException;
import java.security.PublicKey;
import net.jsign.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import net.jsign.bouncycastle.pqc.crypto.qtesla.QTESLAPublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.qtesla.QTESLASecurityCategory;
import net.jsign.bouncycastle.pqc.crypto.util.PublicKeyFactory;
import net.jsign.bouncycastle.pqc.crypto.util.SubjectPublicKeyInfoFactory;
import net.jsign.bouncycastle.util.Arrays;

public class BCqTESLAPublicKey implements PublicKey {
   private transient QTESLAPublicKeyParameters keyParams;

   public BCqTESLAPublicKey(SubjectPublicKeyInfo var1) throws IOException {
      this.init(var1);
   }

   private void init(SubjectPublicKeyInfo var1) throws IOException {
      this.keyParams = (QTESLAPublicKeyParameters)PublicKeyFactory.createKey(var1);
   }

   public final String getAlgorithm() {
      return QTESLASecurityCategory.getName(this.keyParams.getSecurityCategory());
   }

   public byte[] getEncoded() {
      try {
         SubjectPublicKeyInfo var1 = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(this.keyParams);
         return var1.getEncoded();
      } catch (IOException var2) {
         return null;
      }
   }

   public String getFormat() {
      return "X.509";
   }

   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (!(var1 instanceof BCqTESLAPublicKey)) {
         return false;
      } else {
         BCqTESLAPublicKey var2 = (BCqTESLAPublicKey)var1;
         return this.keyParams.getSecurityCategory() == var2.keyParams.getSecurityCategory() && Arrays.areEqual(this.keyParams.getPublicData(), var2.keyParams.getPublicData());
      }
   }

   public int hashCode() {
      return this.keyParams.getSecurityCategory() + 37 * Arrays.hashCode(this.keyParams.getPublicData());
   }
}
