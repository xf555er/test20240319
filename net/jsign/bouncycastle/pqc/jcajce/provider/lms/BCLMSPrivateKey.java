package net.jsign.bouncycastle.pqc.jcajce.provider.lms;

import java.io.IOException;
import java.security.PrivateKey;
import net.jsign.bouncycastle.asn1.ASN1Set;
import net.jsign.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import net.jsign.bouncycastle.pqc.crypto.lms.LMSKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.util.PrivateKeyFactory;
import net.jsign.bouncycastle.pqc.crypto.util.PrivateKeyInfoFactory;
import net.jsign.bouncycastle.pqc.jcajce.interfaces.LMSPrivateKey;
import net.jsign.bouncycastle.util.Arrays;

public class BCLMSPrivateKey implements PrivateKey, LMSPrivateKey {
   private transient LMSKeyParameters keyParams;
   private transient ASN1Set attributes;

   public BCLMSPrivateKey(PrivateKeyInfo var1) throws IOException {
      this.init(var1);
   }

   private void init(PrivateKeyInfo var1) throws IOException {
      this.attributes = var1.getAttributes();
      this.keyParams = (LMSKeyParameters)PrivateKeyFactory.createKey(var1);
   }

   public String getAlgorithm() {
      return "LMS";
   }

   public String getFormat() {
      return "PKCS#8";
   }

   public byte[] getEncoded() {
      try {
         PrivateKeyInfo var1 = PrivateKeyInfoFactory.createPrivateKeyInfo(this.keyParams, this.attributes);
         return var1.getEncoded();
      } catch (IOException var2) {
         return null;
      }
   }

   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (var1 instanceof BCLMSPrivateKey) {
         BCLMSPrivateKey var2 = (BCLMSPrivateKey)var1;

         try {
            return Arrays.areEqual(this.keyParams.getEncoded(), var2.keyParams.getEncoded());
         } catch (IOException var4) {
            throw new IllegalStateException("unable to perform equals");
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      try {
         return Arrays.hashCode(this.keyParams.getEncoded());
      } catch (IOException var2) {
         throw new IllegalStateException("unable to calculate hashCode");
      }
   }
}
