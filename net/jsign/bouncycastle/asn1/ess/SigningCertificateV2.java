package net.jsign.bouncycastle.asn1.ess;

import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.DERSequence;

public class SigningCertificateV2 extends ASN1Object {
   ASN1Sequence certs;
   ASN1Sequence policies;

   public static SigningCertificateV2 getInstance(Object var0) {
      if (var0 != null && !(var0 instanceof SigningCertificateV2)) {
         return var0 instanceof ASN1Sequence ? new SigningCertificateV2((ASN1Sequence)var0) : null;
      } else {
         return (SigningCertificateV2)var0;
      }
   }

   private SigningCertificateV2(ASN1Sequence var1) {
      if (var1.size() >= 1 && var1.size() <= 2) {
         this.certs = ASN1Sequence.getInstance(var1.getObjectAt(0));
         if (var1.size() > 1) {
            this.policies = ASN1Sequence.getInstance(var1.getObjectAt(1));
         }

      } else {
         throw new IllegalArgumentException("Bad sequence size: " + var1.size());
      }
   }

   public ESSCertIDv2[] getCerts() {
      ESSCertIDv2[] var1 = new ESSCertIDv2[this.certs.size()];

      for(int var2 = 0; var2 != this.certs.size(); ++var2) {
         var1[var2] = ESSCertIDv2.getInstance(this.certs.getObjectAt(var2));
      }

      return var1;
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector(2);
      var1.add(this.certs);
      if (this.policies != null) {
         var1.add(this.policies);
      }

      return new DERSequence(var1);
   }
}
