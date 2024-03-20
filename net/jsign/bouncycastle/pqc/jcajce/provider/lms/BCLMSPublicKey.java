package net.jsign.bouncycastle.pqc.jcajce.provider.lms;

import java.io.IOException;
import java.security.PublicKey;
import net.jsign.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import net.jsign.bouncycastle.pqc.crypto.lms.LMSKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.util.PublicKeyFactory;
import net.jsign.bouncycastle.pqc.crypto.util.SubjectPublicKeyInfoFactory;
import net.jsign.bouncycastle.pqc.jcajce.interfaces.LMSKey;
import net.jsign.bouncycastle.util.Arrays;

public class BCLMSPublicKey implements PublicKey, LMSKey {
   private transient LMSKeyParameters keyParams;

   public BCLMSPublicKey(SubjectPublicKeyInfo var1) throws IOException {
      this.init(var1);
   }

   private void init(SubjectPublicKeyInfo var1) throws IOException {
      this.keyParams = (LMSKeyParameters)PublicKeyFactory.createKey(var1);
   }

   public final String getAlgorithm() {
      return "LMS";
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
      } else if (var1 instanceof BCLMSPublicKey) {
         BCLMSPublicKey var2 = (BCLMSPublicKey)var1;

         try {
            return Arrays.areEqual(this.keyParams.getEncoded(), var2.keyParams.getEncoded());
         } catch (IOException var4) {
            return false;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      try {
         return Arrays.hashCode(this.keyParams.getEncoded());
      } catch (IOException var2) {
         return -1;
      }
   }
}
