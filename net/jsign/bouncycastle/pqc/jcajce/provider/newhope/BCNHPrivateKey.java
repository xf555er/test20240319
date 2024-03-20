package net.jsign.bouncycastle.pqc.jcajce.provider.newhope;

import java.io.IOException;
import net.jsign.bouncycastle.asn1.ASN1Set;
import net.jsign.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import net.jsign.bouncycastle.pqc.crypto.newhope.NHPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.util.PrivateKeyFactory;
import net.jsign.bouncycastle.pqc.crypto.util.PrivateKeyInfoFactory;
import net.jsign.bouncycastle.pqc.jcajce.interfaces.NHPrivateKey;
import net.jsign.bouncycastle.util.Arrays;

public class BCNHPrivateKey implements NHPrivateKey {
   private transient NHPrivateKeyParameters params;
   private transient ASN1Set attributes;

   public BCNHPrivateKey(PrivateKeyInfo var1) throws IOException {
      this.init(var1);
   }

   private void init(PrivateKeyInfo var1) throws IOException {
      this.attributes = var1.getAttributes();
      this.params = (NHPrivateKeyParameters)PrivateKeyFactory.createKey(var1);
   }

   public boolean equals(Object var1) {
      if (!(var1 instanceof BCNHPrivateKey)) {
         return false;
      } else {
         BCNHPrivateKey var2 = (BCNHPrivateKey)var1;
         return Arrays.areEqual(this.params.getSecData(), var2.params.getSecData());
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.params.getSecData());
   }

   public final String getAlgorithm() {
      return "NH";
   }

   public byte[] getEncoded() {
      try {
         PrivateKeyInfo var1 = PrivateKeyInfoFactory.createPrivateKeyInfo(this.params, this.attributes);
         return var1.getEncoded();
      } catch (IOException var2) {
         return null;
      }
   }

   public String getFormat() {
      return "PKCS#8";
   }
}
