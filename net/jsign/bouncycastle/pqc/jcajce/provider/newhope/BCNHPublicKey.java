package net.jsign.bouncycastle.pqc.jcajce.provider.newhope;

import java.io.IOException;
import net.jsign.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import net.jsign.bouncycastle.pqc.crypto.newhope.NHPublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.util.PublicKeyFactory;
import net.jsign.bouncycastle.pqc.crypto.util.SubjectPublicKeyInfoFactory;
import net.jsign.bouncycastle.pqc.jcajce.interfaces.NHPublicKey;
import net.jsign.bouncycastle.util.Arrays;

public class BCNHPublicKey implements NHPublicKey {
   private transient NHPublicKeyParameters params;

   public BCNHPublicKey(SubjectPublicKeyInfo var1) throws IOException {
      this.init(var1);
   }

   private void init(SubjectPublicKeyInfo var1) throws IOException {
      this.params = (NHPublicKeyParameters)PublicKeyFactory.createKey(var1);
   }

   public boolean equals(Object var1) {
      if (var1 != null && var1 instanceof BCNHPublicKey) {
         BCNHPublicKey var2 = (BCNHPublicKey)var1;
         return Arrays.areEqual(this.params.getPubData(), var2.params.getPubData());
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.params.getPubData());
   }

   public final String getAlgorithm() {
      return "NH";
   }

   public byte[] getEncoded() {
      try {
         SubjectPublicKeyInfo var1 = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(this.params);
         return var1.getEncoded();
      } catch (IOException var2) {
         return null;
      }
   }

   public String getFormat() {
      return "X.509";
   }
}
