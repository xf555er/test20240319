package net.jsign.bouncycastle.asn1.x509;

import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.ASN1TaggedObject;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.asn1.DERTaggedObject;

public class Holder extends ASN1Object {
   IssuerSerial baseCertificateID;
   GeneralNames entityName;
   ObjectDigestInfo objectDigestInfo;
   private int version = 1;

   public static Holder getInstance(Object var0) {
      if (var0 instanceof Holder) {
         return (Holder)var0;
      } else if (var0 instanceof ASN1TaggedObject) {
         return new Holder(ASN1TaggedObject.getInstance(var0));
      } else {
         return var0 != null ? new Holder(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   private Holder(ASN1TaggedObject var1) {
      switch (var1.getTagNo()) {
         case 0:
            this.baseCertificateID = IssuerSerial.getInstance(var1, true);
            break;
         case 1:
            this.entityName = GeneralNames.getInstance(var1, true);
            break;
         default:
            throw new IllegalArgumentException("unknown tag in Holder");
      }

      this.version = 0;
   }

   private Holder(ASN1Sequence var1) {
      if (var1.size() > 3) {
         throw new IllegalArgumentException("Bad sequence size: " + var1.size());
      } else {
         for(int var2 = 0; var2 != var1.size(); ++var2) {
            ASN1TaggedObject var3 = ASN1TaggedObject.getInstance(var1.getObjectAt(var2));
            switch (var3.getTagNo()) {
               case 0:
                  this.baseCertificateID = IssuerSerial.getInstance(var3, false);
                  break;
               case 1:
                  this.entityName = GeneralNames.getInstance(var3, false);
                  break;
               case 2:
                  this.objectDigestInfo = ObjectDigestInfo.getInstance(var3, false);
                  break;
               default:
                  throw new IllegalArgumentException("unknown tag in Holder");
            }
         }

         this.version = 1;
      }
   }

   public ASN1Primitive toASN1Primitive() {
      if (this.version == 1) {
         ASN1EncodableVector var1 = new ASN1EncodableVector(3);
         if (this.baseCertificateID != null) {
            var1.add(new DERTaggedObject(false, 0, this.baseCertificateID));
         }

         if (this.entityName != null) {
            var1.add(new DERTaggedObject(false, 1, this.entityName));
         }

         if (this.objectDigestInfo != null) {
            var1.add(new DERTaggedObject(false, 2, this.objectDigestInfo));
         }

         return new DERSequence(var1);
      } else {
         return this.entityName != null ? new DERTaggedObject(true, 1, this.entityName) : new DERTaggedObject(true, 0, this.baseCertificateID);
      }
   }
}
