package net.jsign.bouncycastle.pqc.jcajce.provider.xmss;

import java.io.IOException;
import java.security.PrivateKey;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1Set;
import net.jsign.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import net.jsign.bouncycastle.pqc.asn1.XMSSKeyParams;
import net.jsign.bouncycastle.pqc.crypto.util.PrivateKeyFactory;
import net.jsign.bouncycastle.pqc.crypto.util.PrivateKeyInfoFactory;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.jcajce.interfaces.XMSSPrivateKey;
import net.jsign.bouncycastle.util.Arrays;

public class BCXMSSPrivateKey implements PrivateKey, XMSSPrivateKey {
   private transient XMSSPrivateKeyParameters keyParams;
   private transient ASN1ObjectIdentifier treeDigest;
   private transient ASN1Set attributes;

   public BCXMSSPrivateKey(PrivateKeyInfo var1) throws IOException {
      this.init(var1);
   }

   private void init(PrivateKeyInfo var1) throws IOException {
      this.attributes = var1.getAttributes();
      XMSSKeyParams var2 = XMSSKeyParams.getInstance(var1.getPrivateKeyAlgorithm().getParameters());
      this.treeDigest = var2.getTreeDigest().getAlgorithm();
      this.keyParams = (XMSSPrivateKeyParameters)PrivateKeyFactory.createKey(var1);
   }

   public String getAlgorithm() {
      return "XMSS";
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
      } else if (!(var1 instanceof BCXMSSPrivateKey)) {
         return false;
      } else {
         BCXMSSPrivateKey var2 = (BCXMSSPrivateKey)var1;
         return this.treeDigest.equals(var2.treeDigest) && Arrays.areEqual(this.keyParams.toByteArray(), var2.keyParams.toByteArray());
      }
   }

   public int hashCode() {
      return this.treeDigest.hashCode() + 37 * Arrays.hashCode(this.keyParams.toByteArray());
   }
}
