package net.jsign.bouncycastle.pqc.jcajce.provider.xmss;

import java.io.IOException;
import java.security.PublicKey;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import net.jsign.bouncycastle.pqc.crypto.util.PublicKeyFactory;
import net.jsign.bouncycastle.pqc.crypto.util.SubjectPublicKeyInfoFactory;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSMTPublicKeyParameters;
import net.jsign.bouncycastle.util.Arrays;

public class BCXMSSMTPublicKey implements PublicKey {
   private transient ASN1ObjectIdentifier treeDigest;
   private transient XMSSMTPublicKeyParameters keyParams;

   public BCXMSSMTPublicKey(SubjectPublicKeyInfo var1) throws IOException {
      this.init(var1);
   }

   private void init(SubjectPublicKeyInfo var1) throws IOException {
      this.keyParams = (XMSSMTPublicKeyParameters)PublicKeyFactory.createKey(var1);
      this.treeDigest = DigestUtil.getDigestOID(this.keyParams.getTreeDigest());
   }

   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (!(var1 instanceof BCXMSSMTPublicKey)) {
         return false;
      } else {
         BCXMSSMTPublicKey var2 = (BCXMSSMTPublicKey)var1;
         return this.treeDigest.equals(var2.treeDigest) && Arrays.areEqual(this.keyParams.toByteArray(), var2.keyParams.toByteArray());
      }
   }

   public int hashCode() {
      return this.treeDigest.hashCode() + 37 * Arrays.hashCode(this.keyParams.toByteArray());
   }

   public final String getAlgorithm() {
      return "XMSSMT";
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
}
