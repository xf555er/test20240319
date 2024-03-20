package net.jsign.bouncycastle.jcajce;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.List;
import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import net.jsign.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class CompositePrivateKey implements PrivateKey {
   private final List keys;

   public List getPrivateKeys() {
      return this.keys;
   }

   public String getAlgorithm() {
      return "Composite";
   }

   public String getFormat() {
      return "PKCS#8";
   }

   public byte[] getEncoded() {
      ASN1EncodableVector var1 = new ASN1EncodableVector();

      for(int var2 = 0; var2 != this.keys.size(); ++var2) {
         var1.add(PrivateKeyInfo.getInstance(((PrivateKey)this.keys.get(var2)).getEncoded()));
      }

      try {
         return (new PrivateKeyInfo(new AlgorithmIdentifier(MiscObjectIdentifiers.id_alg_composite), new DERSequence(var1))).getEncoded("DER");
      } catch (IOException var3) {
         throw new IllegalStateException("unable to encode composite key: " + var3.getMessage());
      }
   }

   public int hashCode() {
      return this.keys.hashCode();
   }

   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else {
         return var1 instanceof CompositePrivateKey ? this.keys.equals(((CompositePrivateKey)var1).keys) : false;
      }
   }
}
