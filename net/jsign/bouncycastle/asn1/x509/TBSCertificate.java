package net.jsign.bouncycastle.asn1.x509;

import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Integer;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.ASN1TaggedObject;
import net.jsign.bouncycastle.asn1.DERBitString;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.asn1.DERTaggedObject;
import net.jsign.bouncycastle.asn1.x500.X500Name;
import net.jsign.bouncycastle.util.Properties;

public class TBSCertificate extends ASN1Object {
   ASN1Sequence seq;
   ASN1Integer version;
   ASN1Integer serialNumber;
   AlgorithmIdentifier signature;
   X500Name issuer;
   Time startDate;
   Time endDate;
   X500Name subject;
   SubjectPublicKeyInfo subjectPublicKeyInfo;
   DERBitString issuerUniqueId;
   DERBitString subjectUniqueId;
   Extensions extensions;

   public static TBSCertificate getInstance(Object var0) {
      if (var0 instanceof TBSCertificate) {
         return (TBSCertificate)var0;
      } else {
         return var0 != null ? new TBSCertificate(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   private TBSCertificate(ASN1Sequence var1) {
      byte var2 = 0;
      this.seq = var1;
      if (var1.getObjectAt(0) instanceof ASN1TaggedObject) {
         this.version = ASN1Integer.getInstance((ASN1TaggedObject)var1.getObjectAt(0), true);
      } else {
         var2 = -1;
         this.version = new ASN1Integer(0L);
      }

      boolean var3 = false;
      boolean var4 = false;
      if (this.version.hasValue(0)) {
         var3 = true;
      } else if (this.version.hasValue(1)) {
         var4 = true;
      } else if (!this.version.hasValue(2)) {
         throw new IllegalArgumentException("version number not recognised");
      }

      this.serialNumber = ASN1Integer.getInstance(var1.getObjectAt(var2 + 1));
      this.signature = AlgorithmIdentifier.getInstance(var1.getObjectAt(var2 + 2));
      this.issuer = X500Name.getInstance(var1.getObjectAt(var2 + 3));
      ASN1Sequence var5 = (ASN1Sequence)var1.getObjectAt(var2 + 4);
      this.startDate = Time.getInstance(var5.getObjectAt(0));
      this.endDate = Time.getInstance(var5.getObjectAt(1));
      this.subject = X500Name.getInstance(var1.getObjectAt(var2 + 5));
      this.subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(var1.getObjectAt(var2 + 6));
      int var6 = var1.size() - (var2 + 6) - 1;
      if (var6 != 0 && var3) {
         throw new IllegalArgumentException("version 1 certificate contains extra data");
      } else {
         for(; var6 > 0; --var6) {
            ASN1TaggedObject var7 = (ASN1TaggedObject)var1.getObjectAt(var2 + 6 + var6);
            switch (var7.getTagNo()) {
               case 1:
                  this.issuerUniqueId = DERBitString.getInstance(var7, false);
                  break;
               case 2:
                  this.subjectUniqueId = DERBitString.getInstance(var7, false);
                  break;
               case 3:
                  if (var4) {
                     throw new IllegalArgumentException("version 2 certificate cannot contain extensions");
                  }

                  this.extensions = Extensions.getInstance(ASN1Sequence.getInstance(var7, true));
                  break;
               default:
                  throw new IllegalArgumentException("Unknown tag encountered in structure: " + var7.getTagNo());
            }
         }

      }
   }

   public ASN1Integer getSerialNumber() {
      return this.serialNumber;
   }

   public X500Name getIssuer() {
      return this.issuer;
   }

   public Time getStartDate() {
      return this.startDate;
   }

   public Time getEndDate() {
      return this.endDate;
   }

   public X500Name getSubject() {
      return this.subject;
   }

   public SubjectPublicKeyInfo getSubjectPublicKeyInfo() {
      return this.subjectPublicKeyInfo;
   }

   public Extensions getExtensions() {
      return this.extensions;
   }

   public ASN1Primitive toASN1Primitive() {
      if (Properties.getPropertyValue("net.jsign.bouncycastle.x509.allow_non-der_tbscert") != null) {
         if (Properties.isOverrideSet("net.jsign.bouncycastle.x509.allow_non-der_tbscert")) {
            return this.seq;
         } else {
            ASN1EncodableVector var1 = new ASN1EncodableVector();
            if (!this.version.hasValue(0)) {
               var1.add(new DERTaggedObject(true, 0, this.version));
            }

            var1.add(this.serialNumber);
            var1.add(this.signature);
            var1.add(this.issuer);
            ASN1EncodableVector var2 = new ASN1EncodableVector(2);
            var2.add(this.startDate);
            var2.add(this.endDate);
            var1.add(new DERSequence(var2));
            if (this.subject != null) {
               var1.add(this.subject);
            } else {
               var1.add(new DERSequence());
            }

            var1.add(this.subjectPublicKeyInfo);
            if (this.issuerUniqueId != null) {
               var1.add(new DERTaggedObject(false, 1, this.issuerUniqueId));
            }

            if (this.subjectUniqueId != null) {
               var1.add(new DERTaggedObject(false, 2, this.subjectUniqueId));
            }

            if (this.extensions != null) {
               var1.add(new DERTaggedObject(true, 3, this.extensions));
            }

            return new DERSequence(var1);
         }
      } else {
         return this.seq;
      }
   }
}
