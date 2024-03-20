package net.jsign.bouncycastle.pqc.jcajce.provider.xmss;

import java.io.IOException;
import java.security.PublicKey;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import net.jsign.bouncycastle.pqc.crypto.util.PublicKeyFactory;
import net.jsign.bouncycastle.pqc.crypto.util.SubjectPublicKeyInfoFactory;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSPublicKeyParameters;
import net.jsign.bouncycastle.util.Arrays;

public class BCXMSSPublicKey implements PublicKey {
   private transient XMSSPublicKeyParameters keyParams;
   private transient ASN1ObjectIdentifier treeDigest;

   public BCXMSSPublicKey(SubjectPublicKeyInfo var1) throws IOException {
      this.init(var1);
   }

   private void init(SubjectPublicKeyInfo var1) throws IOException {
      this.keyParams = (XMSSPublicKeyParameters)PublicKeyFactory.createKey(var1);
      this.treeDigest = DigestUtil.getDigestOID(this.keyParams.getTreeDigest());
   }

   public final String getAlgorithm() {
      return "XMSS";
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
      } else if (var1 instanceof BCXMSSPublicKey) {
         BCXMSSPublicKey var2 = (BCXMSSPublicKey)var1;

         try {
            return this.treeDigest.equals(var2.treeDigest) && Arrays.areEqual(this.keyParams.getEncoded(), var2.keyParams.getEncoded());
         } catch (IOException var4) {
            return false;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      try {
         return this.treeDigest.hashCode() + 37 * Arrays.hashCode(this.keyParams.getEncoded());
      } catch (IOException var2) {
         return this.treeDigest.hashCode();
      }
   }
}
