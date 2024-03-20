package net.jsign.bouncycastle.asn1.pkcs;

import net.jsign.bouncycastle.asn1.ASN1Encodable;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class KeyDerivationFunc extends ASN1Object {
   private AlgorithmIdentifier algId;

   public KeyDerivationFunc(ASN1ObjectIdentifier var1, ASN1Encodable var2) {
      this.algId = new AlgorithmIdentifier(var1, var2);
   }

   private KeyDerivationFunc(ASN1Sequence var1) {
      this.algId = AlgorithmIdentifier.getInstance(var1);
   }

   public static KeyDerivationFunc getInstance(Object var0) {
      if (var0 instanceof KeyDerivationFunc) {
         return (KeyDerivationFunc)var0;
      } else {
         return var0 != null ? new KeyDerivationFunc(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   public ASN1Encodable getParameters() {
      return this.algId.getParameters();
   }

   public ASN1Primitive toASN1Primitive() {
      return this.algId.toASN1Primitive();
   }
}
