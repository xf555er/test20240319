package net.jsign.bouncycastle.asn1.x509;

import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.ASN1TaggedObject;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.asn1.DERTaggedObject;

public class V2Form extends ASN1Object {
   GeneralNames issuerName;
   IssuerSerial baseCertificateID;
   ObjectDigestInfo objectDigestInfo;

   public static V2Form getInstance(ASN1TaggedObject var0, boolean var1) {
      return getInstance(ASN1Sequence.getInstance(var0, var1));
   }

   public static V2Form getInstance(Object var0) {
      if (var0 instanceof V2Form) {
         return (V2Form)var0;
      } else {
         return var0 != null ? new V2Form(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   /** @deprecated */
   public V2Form(ASN1Sequence var1) {
      if (var1.size() > 3) {
         throw new IllegalArgumentException("Bad sequence size: " + var1.size());
      } else {
         int var2 = 0;
         if (!(var1.getObjectAt(0) instanceof ASN1TaggedObject)) {
            ++var2;
            this.issuerName = GeneralNames.getInstance(var1.getObjectAt(0));
         }

         for(int var3 = var2; var3 != var1.size(); ++var3) {
            ASN1TaggedObject var4 = ASN1TaggedObject.getInstance(var1.getObjectAt(var3));
            if (var4.getTagNo() == 0) {
               this.baseCertificateID = IssuerSerial.getInstance(var4, false);
            } else {
               if (var4.getTagNo() != 1) {
                  throw new IllegalArgumentException("Bad tag number: " + var4.getTagNo());
               }

               this.objectDigestInfo = ObjectDigestInfo.getInstance(var4, false);
            }
         }

      }
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector(3);
      if (this.issuerName != null) {
         var1.add(this.issuerName);
      }

      if (this.baseCertificateID != null) {
         var1.add(new DERTaggedObject(false, 0, this.baseCertificateID));
      }

      if (this.objectDigestInfo != null) {
         var1.add(new DERTaggedObject(false, 1, this.objectDigestInfo));
      }

      return new DERSequence(var1);
   }
}
