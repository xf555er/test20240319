package net.jsign.bouncycastle.asn1.x509;

import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.DEROctetString;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.util.Arrays;

public class DigestInfo extends ASN1Object {
   private byte[] digest;
   private AlgorithmIdentifier algId;

   public DigestInfo(AlgorithmIdentifier var1, byte[] var2) {
      this.digest = Arrays.clone(var2);
      this.algId = var1;
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector(2);
      var1.add(this.algId);
      var1.add(new DEROctetString(this.digest));
      return new DERSequence(var1);
   }
}
