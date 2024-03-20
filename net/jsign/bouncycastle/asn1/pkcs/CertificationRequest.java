package net.jsign.bouncycastle.asn1.pkcs;

import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.DERBitString;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class CertificationRequest extends ASN1Object {
   protected CertificationRequestInfo reqInfo = null;
   protected AlgorithmIdentifier sigAlgId = null;
   protected DERBitString sigBits = null;

   public static CertificationRequest getInstance(Object var0) {
      if (var0 instanceof CertificationRequest) {
         return (CertificationRequest)var0;
      } else {
         return var0 != null ? new CertificationRequest(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   protected CertificationRequest() {
   }

   /** @deprecated */
   public CertificationRequest(ASN1Sequence var1) {
      this.reqInfo = CertificationRequestInfo.getInstance(var1.getObjectAt(0));
      this.sigAlgId = AlgorithmIdentifier.getInstance(var1.getObjectAt(1));
      this.sigBits = (DERBitString)var1.getObjectAt(2);
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector(3);
      var1.add(this.reqInfo);
      var1.add(this.sigAlgId);
      var1.add(this.sigBits);
      return new DERSequence(var1);
   }
}
