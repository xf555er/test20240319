package net.jsign.bouncycastle.pqc.jcajce.provider.xmss;

import java.io.IOException;
import java.security.PrivateKey;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1Set;
import net.jsign.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import net.jsign.bouncycastle.pqc.asn1.XMSSMTKeyParams;
import net.jsign.bouncycastle.pqc.crypto.util.PrivateKeyFactory;
import net.jsign.bouncycastle.pqc.crypto.util.PrivateKeyInfoFactory;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSMTPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.jcajce.interfaces.XMSSMTPrivateKey;
import net.jsign.bouncycastle.util.Arrays;

public class BCXMSSMTPrivateKey implements PrivateKey, XMSSMTPrivateKey {
   private transient ASN1ObjectIdentifier treeDigest;
   private transient XMSSMTPrivateKeyParameters keyParams;
   private transient ASN1Set attributes;

   public BCXMSSMTPrivateKey(PrivateKeyInfo var1) throws IOException {
      this.init(var1);
   }

   private void init(PrivateKeyInfo var1) throws IOException {
      this.attributes = var1.getAttributes();
      XMSSMTKeyParams var2 = XMSSMTKeyParams.getInstance(var1.getPrivateKeyAlgorithm().getParameters());
      this.treeDigest = var2.getTreeDigest().getAlgorithm();
      this.keyParams = (XMSSMTPrivateKeyParameters)PrivateKeyFactory.createKey(var1);
   }

   public String getAlgorithm() {
      return "XMSSMT";
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
      } else if (!(var1 instanceof BCXMSSMTPrivateKey)) {
         return false;
      } else {
         BCXMSSMTPrivateKey var2 = (BCXMSSMTPrivateKey)var1;
         return this.treeDigest.equals(var2.treeDigest) && Arrays.areEqual(this.keyParams.toByteArray(), var2.keyParams.toByteArray());
      }
   }

   public int hashCode() {
      return this.treeDigest.hashCode() + 37 * Arrays.hashCode(this.keyParams.toByteArray());
   }
}
