package net.jsign.bouncycastle.jcajce;

import java.io.IOException;
import java.security.PublicKey;
import java.util.List;
import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

public class CompositePublicKey implements PublicKey {
   private final List keys;

   public List getPublicKeys() {
      return this.keys;
   }

   public String getAlgorithm() {
      return "Composite";
   }

   public String getFormat() {
      return "X.509";
   }

   public byte[] getEncoded() {
      ASN1EncodableVector var1 = new ASN1EncodableVector();

      for(int var2 = 0; var2 != this.keys.size(); ++var2) {
         var1.add(SubjectPublicKeyInfo.getInstance(((PublicKey)this.keys.get(var2)).getEncoded()));
      }

      try {
         return (new SubjectPublicKeyInfo(new AlgorithmIdentifier(MiscObjectIdentifiers.id_alg_composite), new DERSequence(var1))).getEncoded("DER");
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
         return var1 instanceof CompositePublicKey ? this.keys.equals(((CompositePublicKey)var1).keys) : false;
      }
   }
}
